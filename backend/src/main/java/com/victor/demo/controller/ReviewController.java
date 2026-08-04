package com.victor.demo.controller;

import com.victor.demo.dto.request.ReviewRequest;
import com.victor.demo.dto.response.ProductReviewsSummaryResponse;
import com.victor.demo.dto.response.ReviewResponse;
import com.victor.demo.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long productId,
                                                       @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(productId, request));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ProductReviewsSummaryResponse> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsForProduct(productId));
    }
}
