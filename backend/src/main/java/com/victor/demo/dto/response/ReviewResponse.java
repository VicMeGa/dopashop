package com.victor.demo.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        String authorName
) {}
