package com.victor.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 200, message = "La calle no puede superar los 200 caracteres") String street,
        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 200, message = "La ciudad no puede superar los 200 caracteres") String city,
        @NotBlank(message = "El estado/provincia es obligatorio")
        @Size(max = 200, message = "El estado/provincia no puede superar los 200 caracteres") String state,
        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "[A-Za-z0-9- ]{1,20}",
                message = "El código postal solo puede contener letras, números, espacios y guiones (máx. 20)") String zipCode,
        @NotBlank(message = "El país es obligatorio")
        @Size(max = 200, message = "El país no puede superar los 200 caracteres") String country,
        boolean isDefault
) {}