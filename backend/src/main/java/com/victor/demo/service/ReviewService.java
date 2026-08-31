package com.victor.demo.service;

import com.victor.demo.dto.request.ReviewRequest;
import com.victor.demo.dto.response.ProductReviewsSummaryResponse;
import com.victor.demo.dto.response.ReviewResponse;
import com.victor.demo.entity.Product;
import com.victor.demo.entity.Review;
import com.victor.demo.entity.User;
import com.victor.demo.mapper.ReviewMapper;
import com.victor.demo.repository.OrderItemRepository;
import com.victor.demo.repository.ProductRepository;
import com.victor.demo.repository.ReviewRepository;
import com.victor.demo.security.CurrentUserProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final CurrentUserProvider currentUserProvider;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         OrderItemRepository orderItemRepository,
                         CurrentUserProvider currentUserProvider) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public ReviewResponse createReview(Long productId, ReviewRequest request) {
        User user = currentUserProvider.getCurrentUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        boolean hasPurchased = orderItemRepository.existsConfirmedPurchase(productId, user.getId());
        if (!hasPurchased) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Debes haber comprado este producto para poder comentarlo");
        }

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya reseñaste este producto");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());
        reviewRepository.save(review);

        return ReviewMapper.toResponse(review);
    }

    @Transactional(readOnly = true)
    public ProductReviewsSummaryResponse getReviewsForProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);

        Double averageRating = null;
        if (!reviews.isEmpty()) {
            double sum = reviews.stream().mapToInt(Review::getRating).sum();
            averageRating = BigDecimal.valueOf(sum / reviews.size())
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        return new ProductReviewsSummaryResponse(
                averageRating,
                reviews.size(),
                reviews.stream().map(ReviewMapper::toResponse).toList()
        );
    }
}
