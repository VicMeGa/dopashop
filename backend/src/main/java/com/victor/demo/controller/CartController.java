package com.victor.demo.controller;

import com.victor.demo.dto.request.CartItemRequest;
import com.victor.demo.dto.request.MergeCartRequest;
import com.victor.demo.dto.request.UpdateCartItemRequest;
import com.victor.demo.dto.response.CartResponse;
import com.victor.demo.service.CartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        log.debug("[CONTROLLER] GET /api/cart");
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        log.info("[CONTROLLER] POST /api/cart/items");
        return ResponseEntity.ok(cartService.addItem(request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long productId,
                                                   @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("[CONTROLLER] PUT /api/cart/items/{}", productId);
        return ResponseEntity.ok(cartService.updateQuantity(productId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long productId) {
        log.info("[CONTROLLER] DELETE /api/cart/items/{}", productId);
        cartService.removeItem(productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        log.info("[CONTROLLER] DELETE /api/cart");
        cartService.clear();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> merge(@Valid @RequestBody MergeCartRequest request) {
        log.info("[CONTROLLER] POST /api/cart/merge");
        return ResponseEntity.ok(cartService.merge(request));
    }
}
