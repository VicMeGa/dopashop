package com.victor.demo.service;

import com.victor.demo.dto.response.OrderResponse;
import com.victor.demo.dto.response.PaymentAttemptResponse;
import com.victor.demo.entity.*;
import com.victor.demo.mapper.OrderMapper;
import com.victor.demo.repository.*;
import com.victor.demo.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ProductRepository productRepository;
    private final ShipmentRepository shipmentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final SecureRandom secureRandom;

    public PaymentService(OrderRepository orderRepository,
                          PaymentTransactionRepository paymentTransactionRepository,
                          ProductRepository productRepository,
                          ShipmentRepository shipmentRepository,
                          CurrentUserProvider currentUserProvider,
                          SecureRandom secureRandom) {
        this.orderRepository = orderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.productRepository = productRepository;
        this.shipmentRepository = shipmentRepository;
        this.currentUserProvider = currentUserProvider;
        this.secureRandom = secureRandom;
    }

    @Transactional
    public PaymentAttemptResponse attemptPayment(Long orderId) {
        User user = currentUserProvider.getCurrentUser();
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        if (!order.getUser().getId().equals(user.getId())) {
            log.warn("Pago: la orden {} no pertenece al userId={}", orderId, user.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La orden no pertenece al usuario actual");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Pago: orden {} no está en estado PENDING (estado={})", orderId, order.getStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La orden ya fue pagada o cancelada");
        }

        boolean approved = secureRandom.nextInt(100) < 90;
        PaymentTransactionStatus txStatus = approved ? PaymentTransactionStatus.APPROVED : PaymentTransactionStatus.REJECTED;

        String message;
        if (approved) {
            List<OrderItem> items = order.getOrderItems().stream()
                    .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                    .toList();
            for (OrderItem item : items) {
                int updated = productRepository.decrementStock(item.getProduct().getId(), item.getQuantity());
                if (updated == 0) {
                    log.warn("Pago: stock insuficiente productId={} solicitado={}",
                            item.getProduct().getId(), item.getQuantity());
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                            "Stock insuficiente para el producto: " + item.getProductTitle());
                }
            }
            order.setStatus(OrderStatus.CONFIRMED);

            Shipment shipment = new Shipment();
            shipment.setOrder(order);
            shipment.setStatus(ShipmentStatus.PENDING);
            shipment.setTrackingCode(generateTrackingCode());
            shipmentRepository.save(shipment);
            order.setShipment(shipment);

            message = "Pago aprobado";
        } else {
            message = "Pago rechazado — puedes reintentarlo";
        }

        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order);
        tx.setStatus(txStatus);
        tx.setAmount(order.getTotal());
        tx.setProcessedAt(LocalDateTime.now());
        paymentTransactionRepository.save(tx);

        orderRepository.save(order);

        if (approved) {
            log.info("Pago aprobado orderId={} txId={} monto={} userId={}",
                    order.getId(), tx.getId(), order.getTotal(), user.getId());
        } else {
            log.warn("Pago rechazado orderId={} txId={} monto={} userId={} (se puede reintentar)",
                    order.getId(), tx.getId(), order.getTotal(), user.getId());
        }

        OrderResponse orderResponse = OrderMapper.toResponse(order);
        return new PaymentAttemptResponse(tx.getId(), txStatus.name(), message, orderResponse);
    }

    private String generateTrackingCode() {
        byte[] bytes = new byte[8];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder("TRK-");
        for (byte b : bytes) {
            sb.append(Integer.toHexString((b & 0xFF) + 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }
}
