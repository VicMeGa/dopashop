package com.victor.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long addressId,
        @NotNull Long paymentMethodId,
        @NotNull @Valid List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull Long productId,
            @NotNull @Positive Integer quantity
    ) {}
}
