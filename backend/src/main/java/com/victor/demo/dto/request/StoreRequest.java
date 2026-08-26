package com.victor.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StoreRequest(
        @NotBlank(message = "El nombre de la tienda es obligatorio") String name,
        String description
) {}
