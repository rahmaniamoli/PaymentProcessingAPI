package com.mora.paymentprocessingapi.dto;

public record AuthResponse(
        String accessToken,
        String tokenType
) {}