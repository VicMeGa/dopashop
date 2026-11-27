package com.victor.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 254, message = "El email no puede superar los 254 caracteres") String email,
        @NotBlank(message = "La contraseña es obligatoria") String password
) {}