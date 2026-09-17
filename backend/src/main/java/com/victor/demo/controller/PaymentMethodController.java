package com.victor.demo.controller;

import com.victor.demo.dto.request.PaymentMethodRequest;
import com.victor.demo.dto.response.PaymentMethodResponse;
import com.victor.demo.service.PaymentMethodService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private static final Logger log = LoggerFactory.getLogger(PaymentMethodController.class);

    private final PaymentMethodService paymentMethodService;

    public PaymentMethodController(PaymentMethodService paymentMethodService) {
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> findAll() {
        log.debug("[CONTROLLER] GET /api/payment-methods");
        return ResponseEntity.ok(paymentMethodService.findAllForCurrentUser());
    }

    @PostMapping
    public ResponseEntity<PaymentMethodResponse> create(@Valid @RequestBody PaymentMethodRequest request) {
        log.info("[CONTROLLER] POST /api/payment-methods");
        return ResponseEntity.ok(paymentMethodService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("[CONTROLLER] DELETE /api/payment-methods/{}", id);
        paymentMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
