package com.victor.demo.service;

import com.victor.demo.dto.request.CreateOrderRequest;
import com.victor.demo.dto.response.OrderResponse;
import com.victor.demo.entity.*;
import com.victor.demo.mapper.OrderMapper;
import com.victor.demo.mapper.ProductMapper;
import com.victor.demo.repository.*;
import com.victor.demo.security.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    public OrderService(OrderRepository orderRepository,
                        AddressRepository addressRepository,
                        PaymentMethodRepository paymentMethodRepository,
                        ProductRepository productRepository,
                        CurrentUserProvider currentUserProvider) {
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.productRepository = productRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        User user = currentUserProvider.getCurrentUser();

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dirección no encontrada"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La dirección no pertenece al usuario actual");
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.paymentMethodId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));
        if (!paymentMethod.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El método de pago no pertenece al usuario actual");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findByIdAndActiveTrue(itemReq.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "El producto " + itemReq.productId() + " ya no está disponible"));

            if (product.getStock() < itemReq.quantity()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Stock insuficiente para el producto: " + product.getTitle()
                                + " (disponible: " + product.getStock() + ", solicitado: " + itemReq.quantity() + ")");
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductTitle(product.getTitle());
            List<String> images = ProductMapper.parseImages(product.getImageUrls());
            item.setProductImageUrl(images.isEmpty() ? null : images.get(0));
            item.setUnitPrice(product.getPrice());
            item.setQuantity(itemReq.quantity());
            item.setSubtotal(subtotal);
            orderItems.add(item);
        }

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(total);
        order.setOrderItems(orderItems);
        orderItems.forEach(item -> item.setOrder(order));
        orderRepository.save(order);

        return OrderMapper.toResponse(order);
    }

    public OrderResponse findByIdForCurrentUser(Long orderId) {
        User user = currentUserProvider.getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La orden no pertenece al usuario actual");
        }
        return OrderMapper.toResponse(order);
    }

    public List<OrderResponse> findAllForCurrentUser() {
        User user = currentUserProvider.getCurrentUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(OrderMapper::toResponse)
                .toList();
    }
}
