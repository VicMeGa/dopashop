package com.victor.demo.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProductRequest(
        @Size(max = 200, message = "El título no puede superar los 200 caracteres") String title,
        @Size(max = 4000, message = "La descripción no puede superar los 4000 caracteres") String description,
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El precio es demasiado grande o tiene más de 2 decimales") BigDecimal price,
        @Min(value = 0, message = "El stock no puede ser negativo")
        @Max(value = 1000000, message = "El stock no puede superar 1.000.000") Integer stock,
        Long categoryId,
        @Size(max = 5, message = "Puedes subir hasta 5 imágenes")
        List<@Size(max = 1000, message = "Una URL de imagen es demasiado larga") String> imageUrls
) {}