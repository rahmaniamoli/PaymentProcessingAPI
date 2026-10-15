package com.mora.paymentprocessingapi.service;

import com.mora.paymentprocessingapi.config.RetryPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    @Test
    void shouldAllowRetryBeforeMaximumAttempts() {
        assertTrue(RetryPolicy.canRetry(1));
        assertTrue(RetryPolicy.canRetry(2));
    }

    @Test
    void shouldNotAllowRetryAtMaximumAttempts() {
        assertFalse(RetryPolicy.canRetry(3));
    }

    @Test
    void shouldCalculateExponentialBackoff() {
        assertEquals(1, RetryPolicy.calculateBackoffSeconds(1));
        assertEquals(2, RetryPolicy.calculateBackoffSeconds(2));
        assertEquals(4, RetryPolicy.calculateBackoffSeconds(3));
    }
}
