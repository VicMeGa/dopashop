package com.victor.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentMethodRequest(
        @NotBlank String alias,
        @NotBlank @Size(min = 4, max = 4) String cardLastFour,
        @NotBlank String cardBrand
) {}
