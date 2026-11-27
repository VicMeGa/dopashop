package com.victor.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "Debes seleccionar una dirección") Long addressId,
        @NotNull(message = "Debes seleccionar un método de pago") Long paymentMethodId,
        @NotEmpty(message = "La orden debe tener al menos un producto")
        @Size(max = 50, message = "La orden no puede tener más de 50 productos distintos")
        @Valid List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            @NotNull(message = "El producto es obligatorio") Long productId,
            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a 0")
            @Max(value = 9999, message = "La cantidad máxima por producto es 9999") Integer quantity
    ) {}
}