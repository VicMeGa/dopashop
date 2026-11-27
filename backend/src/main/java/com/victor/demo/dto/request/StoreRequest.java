package com.victor.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreRequest(
        @NotBlank(message = "El nombre de la tienda es obligatorio")
        @Size(max = 120, message = "El nombre de la tienda no puede superar los 120 caracteres") String name,
        @Size(max = 4000, message = "La descripción no puede superar los 4000 caracteres") String description
) {}