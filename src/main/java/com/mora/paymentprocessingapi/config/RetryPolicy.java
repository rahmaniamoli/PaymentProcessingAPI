package com.mora.paymentprocessingapi.config;

public class RetryPolicy {

    public static final int MAX_RETRIES = 3;

    private RetryPolicy() {
    }

    public static boolean canRetry(int currentAttemptNumber) {
        return currentAttemptNumber < MAX_RETRIES;
    }

    public static long calculateBackoffSeconds(int attemptNumber) {
        return (long) Math.pow(2, attemptNumber - 1);
    }
}