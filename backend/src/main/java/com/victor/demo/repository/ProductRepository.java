package com.victor.demo.repository;

import com.victor.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStoreId(Long storeId);
    Optional<Product> findByIdAndActiveTrue(Long id);
    List<Product> findByStoreIdAndActiveTrue(Long storeId);
}
