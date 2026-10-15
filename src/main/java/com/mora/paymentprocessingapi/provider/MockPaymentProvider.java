package com.mora.paymentprocessingapi.provider;

import com.mora.paymentprocessingapi.domain.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    private static final Logger log =
            LoggerFactory.getLogger(MockPaymentProvider.class);

    @Override
    public PaymentProviderResult processPayment(Payment payment) {
        log.info("Mock provider processing paymentId={}, amount={}, currency={}",
                payment.getId(), payment.getAmount(), payment.getCurrency());

        // Simulate a temporary provider failure.
        if (Math.random() < 0.3) {
            log.warn("Mock provider failed paymentId={}", payment.getId());

            return PaymentProviderResult.failure("Simulated temporary provider failure");
        }

        String reference = "MOCK-" + UUID.randomUUID();

        log.info("Mock provider succeeded paymentId={}, providerReference={}",
                payment.getId(), reference);

        return PaymentProviderResult.success(reference);
    }
}