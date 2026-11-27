package com.victor.demo.mapper;

import com.victor.demo.dto.response.CategoryResponse;
import com.victor.demo.entity.Category;

public class CategoryMapper {

    private CategoryMapper() {}

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getImageUrl()
        );
    }
}
