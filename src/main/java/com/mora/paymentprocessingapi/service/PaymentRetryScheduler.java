package com.mora.paymentprocessingapi.service;

import com.mora.paymentprocessingapi.config.RetryPolicy;
import com.mora.paymentprocessingapi.domain.Payment;
import com.mora.paymentprocessingapi.domain.PaymentAttempt;
import com.mora.paymentprocessingapi.domain.PaymentAttemptStatus;
import com.mora.paymentprocessingapi.domain.PaymentStatus;
import com.mora.paymentprocessingapi.provider.PaymentProvider;
import com.mora.paymentprocessingapi.provider.PaymentProviderResult;
import com.mora.paymentprocessingapi.repository.PaymentAttemptRepository;
import com.mora.paymentprocessingapi.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class PaymentRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PaymentRetryScheduler.class);

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentProvider paymentProvider;
    private final CacheManager cacheManager;

    public PaymentRetryScheduler(PaymentRepository paymentRepository, PaymentAttemptRepository paymentAttemptRepository,
                                 PaymentProvider paymentProvider, CacheManager cacheManager) {
        this.paymentRepository = paymentRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
        this.paymentProvider = paymentProvider;
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processScheduledRetries() {
        Instant now = Instant.now();

        List<Payment> payments = paymentRepository.findByStatusAndNextRetryAtLessThanEqual(PaymentStatus.PENDING, now);

        boolean changed = false;

        for (Payment payment : payments) {
            int currentAttempts = paymentAttemptRepository.countByPaymentId(payment.getId());

            if (!RetryPolicy.canRetry(currentAttempts)) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setNextRetryAt(null);
                changed = true;

                log.warn("Payment permanently failed: paymentId={}", payment.getId());
                continue;
            }

            int attemptNumber = currentAttempts + 1;

            PaymentAttempt attempt = new PaymentAttempt();
            attempt.setPayment(payment);
            attempt.setAttemptNumber(attemptNumber);
            attempt.setStatus(PaymentAttemptStatus.PROCESSING);

            paymentAttemptRepository.save(attempt);

            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setNextRetryAt(null);

            changed = true;

            PaymentProviderResult result;

            try {
                result = paymentProvider.processPayment(payment);
            } catch (RuntimeException ex) {
                log.error("Payment provider threw an exception: paymentId={}, attempt={}",
                        payment.getId(), attemptNumber, ex);

                result = PaymentProviderResult.failure("Payment provider processing error");
            }

            if (result.successful()) {
                attempt.setStatus(PaymentAttemptStatus.SUCCESS);
                attempt.setProviderReference(result.providerReference());
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setNextRetryAt(null);

                log.info("Payment succeeded: paymentId={}, attempt={}", payment.getId(), attemptNumber);
            } else {
                attempt.setStatus(PaymentAttemptStatus.FAILED);
                attempt.setFailureReason(result.failureReason());

                if (RetryPolicy.canRetry(attemptNumber)) {
                    payment.setStatus(PaymentStatus.PENDING);
                    payment.setNextRetryAt(Instant.now().plusSeconds(RetryPolicy.calculateBackoffSeconds(attemptNumber)));
                } else {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setNextRetryAt(null);
                }

                log.warn("Payment attempt failed: paymentId={}, attempt={}, reason={}",
                        payment.getId(), attemptNumber, result.failureReason());
            }

            paymentAttemptRepository.save(attempt);
            paymentRepository.save(payment);
        }

        if (changed) {
            evictPaymentCachesAfterCommit();
        }
    }

    private void evictPaymentCachesAfterCommit() {
        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                evictCache("payments");
                                evictCache("paymentLists");
                            }
                        }
                );
    }

    private void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}