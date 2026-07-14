package com.victor.demo.dto.response;

public record PaymentMethodResponse(
        Long id,
        String alias,
        String cardLastFour,
        String cardBrand
) {}
