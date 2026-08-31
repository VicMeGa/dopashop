package com.victor.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 254, message = "El email no puede superar los 254 caracteres") String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres") String fullName
) {}