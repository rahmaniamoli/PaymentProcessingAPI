package com.mora.paymentprocessingapi.provider;

import com.mora.paymentprocessingapi.domain.Payment;

public interface PaymentProvider {

    PaymentProviderResult processPayment(Payment payment);
}
