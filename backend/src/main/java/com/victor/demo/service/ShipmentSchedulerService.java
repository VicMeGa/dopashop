package com.victor.demo.service;

import com.victor.demo.entity.Shipment;
import com.victor.demo.entity.ShipmentStatus;
import com.victor.demo.repository.ShipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShipmentSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentSchedulerService.class);

    private final ShipmentRepository shipmentRepository;
    private final long pendingToPreparingMs;
    private final long preparingToShippedMs;
    private final long shippedToDeliveredMs;

    public ShipmentSchedulerService(ShipmentRepository shipmentRepository,
                                    @Value("${shipment.threshold.pending-to-preparing-ms}") long pendingToPreparingMs,
                                    @Value("${shipment.threshold.preparing-to-shipped-ms}") long preparingToShippedMs,
                                    @Value("${shipment.threshold.shipped-to-delivered-ms}") long shippedToDeliveredMs) {
        this.shipmentRepository = shipmentRepository;
        this.pendingToPreparingMs = pendingToPreparingMs;
        this.preparingToShippedMs = preparingToShippedMs;
        this.shippedToDeliveredMs = shippedToDeliveredMs;
    }

    @Scheduled(fixedRateString = "${shipment.check-interval-ms}")
    @Transactional
    public void checkAndAdvanceShipments() {
        List<Shipment> shipments = shipmentRepository.findByStatusNot(ShipmentStatus.DELIVERED);
        for (Shipment shipment : shipments) {
            ShipmentStatus next = nextStatus(shipment.getStatus());
            if (next == null) continue;

            long elapsedMs = Duration.between(shipment.getUpdatedAt(), LocalDateTime.now()).toMillis();
            if (elapsedMs < thresholdFor(shipment.getStatus())) {
                continue;
            }

            ShipmentStatus previous = shipment.getStatus();
            shipment.setStatus(next);
            shipmentRepository.save(shipment);
            log.info("Shipment {} avanzó de {} a {} (tiempo en estado anterior: {}s)",
                    shipment.getId(), previous, next, elapsedMs / 1000);
        }
    }

    private long thresholdFor(ShipmentStatus status) {
        return switch (status) {
            case PENDING -> pendingToPreparingMs;
            case PREPARING -> preparingToShippedMs;
            case SHIPPED -> shippedToDeliveredMs;
            case DELIVERED -> Long.MAX_VALUE;
        };
    }

    private ShipmentStatus nextStatus(ShipmentStatus status) {
        return switch (status) {
            case PENDING -> ShipmentStatus.PREPARING;
            case PREPARING -> ShipmentStatus.SHIPPED;
            case SHIPPED -> ShipmentStatus.DELIVERED;
            case DELIVERED -> null;
        };
    }
}
