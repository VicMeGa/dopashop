package com.victor.demo.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        String title,
        BigDecimal price,
        String description,
        Integer stock,
        List<String> images,
        CategoryResponse category) {
}
