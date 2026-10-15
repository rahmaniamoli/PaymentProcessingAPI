package com.mora.paymentprocessingapi.dto;

import com.mora.paymentprocessingapi.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID id, UUID customerId, BigDecimal amount, String currency, PaymentStatus status, Instant createdAt, Instant updatedAt) {
}
