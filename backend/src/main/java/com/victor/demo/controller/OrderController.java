package com.victor.demo.controller;

import com.victor.demo.dto.request.CreateOrderRequest;
import com.victor.demo.dto.response.OrderResponse;
import com.victor.demo.dto.response.PaymentAttemptResponse;
import com.victor.demo.service.OrderService;
import com.victor.demo.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentAttemptResponse> attemptPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.attemptPayment(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<OrderResponse>> findMyOrders() {
        return ResponseEntity.ok(orderService.findAllForCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findByIdForCurrentUser(id));
    }
}
