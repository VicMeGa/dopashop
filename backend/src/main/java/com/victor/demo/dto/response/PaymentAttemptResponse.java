package com.victor.demo.dto.response;

public record PaymentAttemptResponse(
        Long transactionId,
        String status,
        String message,
        OrderResponse order
) {}
