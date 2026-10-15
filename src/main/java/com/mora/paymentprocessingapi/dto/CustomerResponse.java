package com.mora.paymentprocessingapi.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(UUID id, String firstName, String lastName, String email, Instant createdAt) {
}
