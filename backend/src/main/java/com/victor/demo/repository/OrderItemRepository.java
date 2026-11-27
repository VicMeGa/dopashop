package com.victor.demo.repository;

import com.victor.demo.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.user.id = :userId AND oi.order.status = 'CONFIRMED'")
    boolean existsConfirmedPurchase(@Param("productId") Long productId, @Param("userId") Long userId);
}
