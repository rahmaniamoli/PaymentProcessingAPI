package com.mora.paymentprocessingapi.repository;

import com.mora.paymentprocessingapi.domain.Customer;
import com.mora.paymentprocessingapi.domain.Payment;
import com.mora.paymentprocessingapi.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByIdAndCustomer(UUID id, Customer customer);

    boolean existsByIdAndCustomer(UUID id, Customer customer);

    List<Payment> findByStatusAndNextRetryAtLessThanEqual(PaymentStatus status, Instant time);
}
