package com.victor.demo.repository;

import com.victor.demo.entity.Category;
import com.victor.demo.entity.Product;
import com.victor.demo.entity.Store;
import com.victor.demo.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private TestEntityManager entityManager;

    @Test
    void decrementStockNeverGoesBelowZero() {
        User owner = new User();
        owner.setEmail("owner@test.com");
        owner.setPasswordHash("hash");
        owner.setFullName("Owner");
        entityManager.persist(owner);

        Store store = new Store();
        store.setName("Store");
        store.setOwner(owner);
        entityManager.persist(store);

        Category category = new Category();
        category.setName("Category");
        entityManager.persist(category);

        Product product = new Product();
        product.setTitle("Mouse");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(3);
        product.setActive(true);
        product.setStore(store);
        product.setCategory(category);
        entityManager.persistAndFlush(product);

        assertThat(productRepository.decrementStock(product.getId(), 2)).isEqualTo(1);
        entityManager.clear();
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(1);

        assertThat(productRepository.decrementStock(product.getId(), 2)).isZero();
        entityManager.clear();
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(1);
    }
}
