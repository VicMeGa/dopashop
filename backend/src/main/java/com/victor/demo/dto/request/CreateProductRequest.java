package com.victor.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(
        @NotBlank String title,
        String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotNull @Min(0) Integer stock,
        @NotNull Long categoryId,
        List<String> imageUrls
) {}
