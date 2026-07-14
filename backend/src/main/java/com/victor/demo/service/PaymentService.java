package com.victor.demo.service;

import com.victor.demo.dto.response.OrderResponse;
import com.victor.demo.dto.response.PaymentAttemptResponse;
import com.victor.demo.entity.*;
import com.victor.demo.mapper.OrderMapper;
import com.victor.demo.repository.*;
import com.victor.demo.security.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ProductRepository productRepository;
    private final ShipmentRepository shipmentRepository;
    private final CurrentUserProvider currentUserProvider;

    public PaymentService(OrderRepository orderRepository,
                          PaymentTransactionRepository paymentTransactionRepository,
                          ProductRepository productRepository,
                          ShipmentRepository shipmentRepository,
                          CurrentUserProvider currentUserProvider) {
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.productRepository = productRepository;
        this.shipmentRepository = shipmentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public PaymentAttemptResponse attemptPayment(Long orderId) {
        User user = currentUserProvider.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to current user");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already paid or cancelled");
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Insufficient stock for product: " + product.getTitle()
                                + " (available: " + product.getStock() + ", requested: " + item.getQuantity() + ")");
            }
        }

        boolean approved = new SecureRandom().nextInt(100) < 90;
        PaymentTransactionStatus txStatus = approved ? PaymentTransactionStatus.APPROVED : PaymentTransactionStatus.REJECTED;

        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setStatus(txStatus);
        tx.setAmount(order.getTotal());
        tx.setProcessedAt(LocalDateTime.now());
        paymentTransactionRepository.save(tx);

        String message;
        if (approved) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
            }
            order.setStatus(OrderStatus.CONFIRMED);

            Shipment shipment = new Shipment();
            shipment.setOrder(order);
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setTrackingCode(generateTrackingCode());
            shipmentRepository.save(shipment);
            order.setShipment(shipment);

            message = "Payment approved";
        } else {
            message = "Payment rejected — you can retry";
        }

        orderRepository.save(order);

        OrderResponse orderResponse = OrderMapper.toResponse(order);
        return new PaymentAttemptResponse(tx.getId(), txStatus.name(), message, orderResponse);
    }

    private String generateTrackingCode() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder("TRK-");
        for (byte b : bytes) {
            sb.append(Integer.toHexString((b & 0xFF) + 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }
}
