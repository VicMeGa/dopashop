package com.victor.demo.service;

import com.victor.demo.dto.request.CartItemRequest;
import com.victor.demo.dto.request.MergeCartRequest;
import com.victor.demo.dto.request.UpdateCartItemRequest;
import com.victor.demo.dto.response.CartResponse;
import com.victor.demo.entity.Cart;
import com.victor.demo.entity.CartItem;
import com.victor.demo.entity.Product;
import com.victor.demo.entity.User;
import com.victor.demo.mapper.CartMapper;
import com.victor.demo.repository.CartRepository;
import com.victor.demo.repository.ProductRepository;
import com.victor.demo.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       CurrentUserProvider currentUserProvider) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        User user = currentUserProvider.getCurrentUser();
        return CartMapper.toResponse(cartRepository.findByUserId(user.getId()).orElse(null));
    }

    @Transactional
    public CartResponse addItem(CartItemRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Product product = findActiveProduct(request.productId());
        Cart cart = getOrCreateCart(user);

        CartItem item = findItem(cart, product.getId()).orElse(null);
        int requested = (item == null ? 0 : item.getQuantity()) + request.quantity();
        ensureStock(product, requested);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            cart.getItems().add(item);
        }
        item.setQuantity(requested);
        cartRepository.save(cart);

        log.info("Carrito: agregado productId={} cantidad={} userId={}",
                product.getId(), requested, user.getId());
        return CartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse updateQuantity(Long productId, UpdateCartItemRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Cart cart = requireCart(user);
        CartItem item = findItem(cart, productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El producto no está en el carrito"));

        Product product = findActiveProduct(productId);
        ensureStock(product, request.quantity());

        item.setQuantity(request.quantity());
        cartRepository.save(cart);

        log.info("Carrito: actualizado productId={} cantidad={} userId={}",
                productId, request.quantity(), user.getId());
        return CartMapper.toResponse(cart);
    }

    @Transactional
    public void removeItem(Long productId) {
        User user = currentUserProvider.getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null) return;

        boolean removed = cart.getItems().removeIf(item ->
                item.getProduct().getId().equals(productId));
        if (removed) {
            cartRepository.save(cart);
            log.info("Carrito: eliminado productId={} userId={}", productId, user.getId());
        }
    }

    @Transactional
    public void clear() {
        User user = currentUserProvider.getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) return;

        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Carrito: vaciado userId={}", user.getId());
    }

    @Transactional
    public CartResponse merge(MergeCartRequest request) {
        User user = currentUserProvider.getCurrentUser();
        Cart cart = getOrCreateCart(user);

        Map<Long, Integer> requestedByProduct = new LinkedHashMap<>();
        for (CartItemRequest item : request.items()) {
            requestedByProduct.merge(item.productId(), item.quantity(), Integer::sum);
        }

        for (Map.Entry<Long, Integer> entry : requestedByProduct.entrySet()) {
            Product product = productRepository.findByIdAndActiveTrue(entry.getKey()).orElse(null);
            if (product == null || product.getStock() == null || product.getStock() <= 0) {
                log.debug("Carrito merge: se omite productId={} (no disponible)", entry.getKey());
                continue;
            }

            CartItem item = findItem(cart, product.getId()).orElse(null);
            int current = item == null ? 0 : item.getQuantity();
            int quantity = Math.min(current + entry.getValue(), product.getStock());

            if (item == null) {
                item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                cart.getItems().add(item);
            }
            item.setQuantity(quantity);
        }
        cartRepository.save(cart);

        log.info("Carrito: fusionado userId={} items={}", user.getId(), requestedByProduct.size());
        return CartMapper.toResponse(cart);
    }

    private Cart requireCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El producto no está en el carrito"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private Optional<CartItem> findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }

    private Product findActiveProduct(Long productId) {
        return productRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El producto " + productId + " ya no está disponible"));
    }

    private void ensureStock(Product product, int requested) {
        if (product.getStock() == null || product.getStock() < requested) {
            log.warn("Carrito: stock insuficiente productId={} disponible={} solicitado={}",
                    product.getId(), product.getStock(), requested);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Stock insuficiente para el producto: " + product.getTitle()
                            + " (disponible: " + product.getStock() + ", solicitado: " + requested + ")");
        }
    }
}
