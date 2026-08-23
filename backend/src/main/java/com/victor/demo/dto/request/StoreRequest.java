package com.victor.demo.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StoreRequest(
        @NotBlank String name,
        String description
) {}
