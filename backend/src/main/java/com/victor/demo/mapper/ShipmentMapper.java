package com.victor.demo.mapper;

import com.victor.demo.dto.response.ShipmentResponse;
import com.victor.demo.entity.Shipment;

public class ShipmentMapper {

    private ShipmentMapper() {}

    public static ShipmentResponse toResponse(Shipment shipment) {
        if (shipment == null) return null;
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getStatus(),
                shipment.getTrackingCode(),
                shipment.getUpdatedAt()
        );
    }
}
