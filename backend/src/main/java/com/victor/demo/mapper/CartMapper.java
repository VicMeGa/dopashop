package com.victor.demo.mapper;

import com.victor.demo.dto.response.CartItemResponse;
import com.victor.demo.dto.response.CartResponse;
import com.victor.demo.entity.Cart;
import com.victor.demo.entity.CartItem;
import com.victor.demo.entity.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public class CartMapper {

    private CartMapper() {}

    public static CartResponse toResponse(Cart cart) {
        if (cart == null) {
            return new CartResponse(null, List.of(), 0, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        List<CartItemResponse> items = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .map(CartMapper::toItemResponse)
                .toList();

        int itemCount = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return new CartResponse(cart.getId(), items, itemCount, total);
    }

    private static CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal subtotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        boolean available = Boolean.TRUE.equals(product.getActive())
                && product.getStock() != null
                && product.getStock() >= item.getQuantity();
        return new CartItemResponse(
                ProductMapper.toResponse(product),
                item.getQuantity(),
                subtotal,
                available
        );
    }
}
