package com.mora.paymentprocessingapi.service;

import com.mora.paymentprocessingapi.config.RetryPolicy;
import com.mora.paymentprocessingapi.domain.*;
import com.mora.paymentprocessingapi.dto.CreatePaymentRequest;
import com.mora.paymentprocessingapi.dto.PaymentAttemptResponse;
import com.mora.paymentprocessingapi.dto.PaymentResponse;
import com.mora.paymentprocessingapi.exception.AccessDeniedRequestException;
import com.mora.paymentprocessingapi.exception.ResourceNotFoundException;
import com.mora.paymentprocessingapi.repository.CustomerRepository;
import com.mora.paymentprocessingapi.repository.PaymentAttemptRepository;
import com.mora.paymentprocessingapi.repository.PaymentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    public PaymentService(PaymentRepository paymentRepository, CustomerRepository customerRepository
            , PaymentAttemptRepository paymentAttemptRepository) {
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    @Transactional
    @AuditEvent("PAYMENT_CREATED")
    @CacheEvict(cacheNames = {"payments", "paymentLists"}, allEntries = true)
    public PaymentResponse createPayment(String idempotencyKey, CreatePaymentRequest request) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }

        return paymentRepository.findByIdempotencyKey(idempotencyKey).map(this::toResponse).orElseGet(() -> createNewPayment(idempotencyKey, request));
    }


    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "payments", key = "#p0.toString() + ':' + #p1")
    public PaymentResponse getPayment(UUID id, String email) {
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        Payment payment = paymentRepository.findByIdAndCustomer(id, customer).orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPayments(UUID customerId, String email) {

        if (!customerRepository.existsByIdAndEmail(customerId, email))
            throw new IllegalArgumentException("Customer not found");

        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).toList();
    }

    private PaymentResponse toResponse(Payment payment) {

        return new PaymentResponse(payment.getId(), payment.getCustomer().getId(), payment.getAmount(),
                payment.getCurrency(), payment.getStatus(), payment.getCreatedAt(), payment.getUpdatedAt());
    }

    @Transactional
    @CacheEvict(cacheNames = {"payments", "paymentLists"}, allEntries = true)
    public PaymentAttemptResponse createAttempt(UUID paymentId, String email) {

        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Payment payment = paymentRepository.findByIdAndCustomer(paymentId, customer).orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        int nextAttemptNumber = paymentAttemptRepository.countByPaymentId(paymentId) + 1;

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setAttemptNumber(nextAttemptNumber);
        attempt.setStatus(PaymentAttemptStatus.PROCESSING);

        PaymentAttempt savedAttempt = paymentAttemptRepository.save(attempt);

        return toAttemptResponse(savedAttempt);
    }

    @Transactional
    @AuditEvent("PAYMENT_ATTEMPT_COMPLETED")
    @CacheEvict(cacheNames = {"payments", "paymentLists"}, allEntries = true)
    public PaymentAttemptResponse markAttemptAsSuccess(UUID attemptId, String email) {

        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId).orElseThrow(() -> new IllegalArgumentException("Payment attempt not found"));

        if (!attempt.getPayment().getCustomer().equals(customer))
            throw new AccessDeniedRequestException("Access denied!");

        attempt.setStatus(PaymentAttemptStatus.SUCCESS);

        attempt.getPayment().setStatus(PaymentStatus.SUCCESS);

        return toAttemptResponse(attempt);
    }

    @Transactional
    @AuditEvent("PAYMENT_ATTEMPT_COMPLETED")
    @CacheEvict(cacheNames = {"payments", "paymentLists"}, allEntries = true)
    public PaymentAttemptResponse markAttemptAsFailed(UUID attemptId, String failureReason, String email) {

        PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId).orElseThrow(() -> new IllegalArgumentException("Payment attempt not found"));
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        if (!attempt.getPayment().getCustomer().equals(customer))
            throw new AccessDeniedRequestException("Access denied!");


        attempt.setStatus(PaymentAttemptStatus.FAILED);
        attempt.setFailureReason(failureReason);

        Payment payment = attempt.getPayment();

        int attemptNumber = attempt.getAttemptNumber();

        if (RetryPolicy.canRetry(attemptNumber)) {
            long delaySeconds =
                    RetryPolicy.calculateBackoffSeconds(attemptNumber);

            payment.setStatus(PaymentStatus.PENDING);
            payment.setNextRetryAt(Instant.now().plusSeconds(delaySeconds));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setNextRetryAt(null);
        }

        return toAttemptResponse(attempt);
    }

    @Transactional(readOnly = true)
    public List<PaymentAttemptResponse> getAttempts(UUID paymentId, String email) {

        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!paymentRepository.existsByIdAndCustomer(paymentId, customer))
            throw new IllegalArgumentException("Payment not found");

        if (!paymentRepository.existsById(paymentId)) {
            throw new IllegalArgumentException("Payment not found");
        }

        return paymentAttemptRepository.findByPaymentIdOrderByAttemptNumberAsc(paymentId).stream().map(this::toAttemptResponse).toList();
    }

    @Transactional
    @AuditEvent("PAYMENT_RETRY_REQUESTED")
    @CacheEvict(cacheNames = {"payments", "paymentLists"}, allEntries = true)
    public PaymentAttemptResponse retryPayment(UUID paymentId, String email) {

        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Payment payment = paymentRepository.findByIdAndCustomer(paymentId, customer).orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Successful payment cannot be retried");
        }

        int currentAttempts = paymentAttemptRepository.countByPaymentId(paymentId);

        if (!RetryPolicy.canRetry(currentAttempts)) {
            throw new IllegalStateException("Maximum retry attempts exceeded");
        }

        int nextAttemptNumber = currentAttempts + 1;

        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPayment(payment);
        attempt.setAttemptNumber(nextAttemptNumber);
        attempt.setStatus(PaymentAttemptStatus.PROCESSING);

        payment.setStatus(PaymentStatus.PROCESSING);

        PaymentAttempt savedAttempt = paymentAttemptRepository.save(attempt);

        return toAttemptResponse(savedAttempt);
    }


    private PaymentAttemptResponse toAttemptResponse(PaymentAttempt attempt) {

        return new PaymentAttemptResponse(attempt.getId(), attempt.getPayment().getId(),
                attempt.getAttemptNumber(), attempt.getStatus(), attempt.getProviderReference(), attempt.getFailureReason(),
                attempt.getCreatedAt(), attempt.getUpdatedAt());
    }

    private PaymentResponse createNewPayment(String idempotencyKey, CreatePaymentRequest request) {

        Customer customer = customerRepository.findById(request.customerId()).orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        Payment payment = new Payment();

        payment.setCustomer(customer);
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency().toUpperCase());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setIdempotencyKey(idempotencyKey);

        Payment savedPayment = paymentRepository.save(payment);

        return toResponse(savedPayment);
    }
}
