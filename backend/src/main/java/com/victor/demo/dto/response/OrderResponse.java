package com.victor.demo.dto.response;

import com.victor.demo.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal total,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        AddressResponse address,
        PaymentMethodResponse paymentMethod,
        ShipmentResponse shipment,
        String latestPaymentStatus
) {}
