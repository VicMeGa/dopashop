package com.victor.demo.dto.response;

import java.time.LocalDateTime;

public record StoreResponse(
        Long id,
        String name,
        String description,
        String ownerName,
        LocalDateTime createdAt
) {}
