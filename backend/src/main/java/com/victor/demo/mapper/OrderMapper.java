package com.victor.demo.mapper;

import com.victor.demo.dto.response.OrderItemResponse;
import com.victor.demo.dto.response.OrderResponse;
import com.victor.demo.entity.Order;
import com.victor.demo.entity.OrderItem;
import com.victor.demo.entity.PaymentTransaction;

import java.util.List;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();

        String latestPaymentStatus = null;
        if (order.getPaymentTransactions() != null && !order.getPaymentTransactions().isEmpty()) {
            latestPaymentStatus = order.getPaymentTransactions().get(0).getStatus().name();
        }

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                items,
                AddressMapper.toResponse(order.getAddress()),
                PaymentMethodMapper.toResponse(order.getPaymentMethod()),
                ShipmentMapper.toResponse(order.getShipment()),
                latestPaymentStatus
        );
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProductTitle(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
