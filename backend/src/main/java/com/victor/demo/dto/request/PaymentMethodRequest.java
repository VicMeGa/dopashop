package com.victor.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PaymentMethodRequest(
        @NotBlank(message = "El alias es obligatorio")
        @Size(max = 60, message = "El alias no puede superar los 60 caracteres") String alias,
        @NotBlank(message = "Los últimos 4 dígitos de la tarjeta son obligatorios")
        @Pattern(regexp = "\\d{4}", message = "Los últimos 4 dígitos de la tarjeta deben ser numéricos") String cardLastFour,
        @NotBlank(message = "La marca de la tarjeta es obligatoria")
        @Pattern(regexp = "VISA|MASTERCARD|OTHER", message = "La marca de la tarjeta no es válida") String cardBrand
) {}