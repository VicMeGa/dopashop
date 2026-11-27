package com.victor.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "orders"})
@Entity
@Table(name = "payment_method")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String alias;

    @Column(name = "card_last_four", nullable = false, length = 4)
    private String cardLastFour;

    @Column(name = "card_brand", nullable = false)
    private String cardBrand;

    @OneToMany(mappedBy = "paymentMethod")
    private List<Order> orders = new ArrayList<>();
}
