package com.victor.demo.mapper;

import com.victor.demo.dto.response.ReviewResponse;
import com.victor.demo.entity.Review;

public class ReviewMapper {

    private ReviewMapper() {}

    public static ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUser().getFullName()
        );
    }
}
