package com.victor.demo.service;

import com.victor.demo.dto.response.ProductResponse;
import com.victor.demo.mapper.ProductMapper;
import com.victor.demo.repository.ProductRepository;
import com.victor.demo.repository.spec.ProductSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAll(String title, BigDecimal priceMin,
                                          BigDecimal priceMax, Long categoryId) {
        var spec = ProductSpecifications.withFilters(title, priceMin, priceMax, categoryId);
        return productRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(ProductMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }
}
