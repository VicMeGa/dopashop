package com.victor.demo.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String imageUrl,
        String productTitle,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {}
