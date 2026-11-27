package com.victor.demo.dto.response;

import com.victor.demo.entity.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentResponse(
        Long id,
        ShipmentStatus status,
        String trackingCode,
        LocalDateTime updatedAt
) {}
