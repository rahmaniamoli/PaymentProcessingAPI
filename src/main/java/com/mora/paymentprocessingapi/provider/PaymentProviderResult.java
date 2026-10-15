package com.mora.paymentprocessingapi.provider;


public record PaymentProviderResult(boolean successful, String failureReason, String providerReference) {

    public static PaymentProviderResult success(String reference) {
        return new PaymentProviderResult(true, null, reference);
    }

    public static PaymentProviderResult failure(String reason) {
        return new PaymentProviderResult(false, reason, null);
    }
}
