package com.victor.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductRequest(
        String title,
        String description,
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") BigDecimal price,
        @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
        Long categoryId,
        List<String> imageUrls
) {}
