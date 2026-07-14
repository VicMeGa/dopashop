package com.victor.demo.controller;

import com.victor.demo.dto.request.PaymentMethodRequest;
import com.victor.demo.dto.response.PaymentMethodResponse;
import com.victor.demo.service.PaymentMethodService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> findAll() {
        return ResponseEntity.ok(paymentMethodService.findAllForCurrentUser());
    }

    @PostMapping
    public ResponseEntity<PaymentMethodResponse> create(@Valid @RequestBody PaymentMethodRequest request) {
        return ResponseEntity.ok(paymentMethodService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
