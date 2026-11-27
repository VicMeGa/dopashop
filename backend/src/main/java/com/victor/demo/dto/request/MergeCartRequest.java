package com.victor.demo.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MergeCartRequest(
        @NotNull(message = "Los ítems son obligatorios")
        @Size(max = 50, message = "El carrito no puede tener más de 50 productos distintos")
        @Valid List<CartItemRequest> items
) {}
