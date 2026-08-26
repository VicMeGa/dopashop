package com.victor.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentMethodRequest(
        @NotBlank(message = "El alias es obligatorio") String alias,
        @NotBlank(message = "Los últimos 4 dígitos de la tarjeta son obligatorios")
        @Size(min = 4, max = 4, message = "Debes ingresar exactamente los últimos 4 dígitos de la tarjeta") String cardLastFour,
        @NotBlank(message = "La marca de la tarjeta es obligatoria") String cardBrand
) {}
