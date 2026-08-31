package com.victor.demo.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5") int rating,
        @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres") String comment
) {}