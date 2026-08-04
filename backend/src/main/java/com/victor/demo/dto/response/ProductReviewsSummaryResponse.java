package com.victor.demo.dto.response;

import java.util.List;

public record ProductReviewsSummaryResponse(
        Double averageRating,
        int totalReviews,
        List<ReviewResponse> reviews
) {}
