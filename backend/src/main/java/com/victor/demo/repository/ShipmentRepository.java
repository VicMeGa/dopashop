package com.victor.demo.repository;

import com.victor.demo.entity.Shipment;
import com.victor.demo.entity.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByStatusNot(ShipmentStatus status);
}
