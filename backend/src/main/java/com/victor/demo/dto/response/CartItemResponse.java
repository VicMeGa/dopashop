package com.victor.demo.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        ProductResponse product,
        Integer quantity,
        BigDecimal subtotal,
        boolean available
) {}
