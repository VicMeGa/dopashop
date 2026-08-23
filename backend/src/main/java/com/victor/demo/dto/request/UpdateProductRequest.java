package com.victor.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductRequest(
        String title,
        String description,
        @DecimalMin(value = "0.01") BigDecimal price,
        @Min(0) Integer stock,
        Long categoryId,
        List<String> imageUrls
) {}
