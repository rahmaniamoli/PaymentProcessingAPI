package com.mora.paymentprocessingapi.dto;

import com.mora.paymentprocessingapi.domain.PaymentAttemptStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentAttemptResponse(UUID id, UUID paymentId, int attemptNumber, PaymentAttemptStatus status, String providerReference, String failureReason, Instant createdAt, Instant updatedAt) {
}
