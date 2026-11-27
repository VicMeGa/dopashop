package com.victor.demo.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        Integer itemCount,
        BigDecimal total
) {}
