package com.mora.paymentprocessingapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(@NotNull UUID customerId, @NotNull @DecimalMin(value = "0.01") BigDecimal amount, @NotBlank @Size(min = 3, max = 3) String currency) {
}
