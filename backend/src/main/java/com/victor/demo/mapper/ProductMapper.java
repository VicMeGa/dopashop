package com.victor.demo.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.victor.demo.dto.response.CategoryResponse;
import com.victor.demo.dto.response.ProductResponse;
import com.victor.demo.entity.Product;

import java.util.Collections;
import java.util.List;

public class ProductMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProductMapper() {}

    public static ProductResponse toResponse(Product product) {
        List<String> images = parseImages(product.getImageUrls());
        CategoryResponse category = product.getCategory() != null
                ? CategoryMapper.toResponse(product.getCategory())
                : null;
        return new ProductResponse(
                product.getId(),
                product.getTitle(),
                product.getPrice(),
                product.getDescription(),
                product.getStock(),
                images,
                category
        );
    }

    private static List<String> parseImages(String imageUrls) {
        if (imageUrls == null || imageUrls.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(imageUrls, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
