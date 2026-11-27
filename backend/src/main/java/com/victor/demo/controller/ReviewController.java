package com.victor.demo.controller;

import com.victor.demo.dto.request.ReviewRequest;
import com.victor.demo.dto.response.ProductReviewsSummaryResponse;
import com.victor.demo.dto.response.ReviewResponse;
import com.victor.demo.service.ReviewService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long productId,
                                                       @Valid @RequestBody ReviewRequest request) {
        log.info("[CONTROLLER] POST /api/products/{}/reviews", productId);
        return ResponseEntity.ok(reviewService.createReview(productId, request));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ProductReviewsSummaryResponse> getReviews(@PathVariable Long productId) {
        log.debug("[CONTROLLER] GET /api/products/{}/reviews", productId);
        return ResponseEntity.ok(reviewService.getReviewsForProduct(productId));
    }
}
