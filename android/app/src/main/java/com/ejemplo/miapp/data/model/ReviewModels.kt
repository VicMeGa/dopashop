package com.ejemplo.miapp.data.model

data class ReviewResponse(
    val id: Long,
    val rating: Int,
    val comment: String?,
    val createdAt: String,
    val authorName: String
)

data class ProductReviewsSummaryResponse(
    val averageRating: Double?,
    val totalReviews: Int,
    val reviews: List<ReviewResponse>
)

data class CreateReviewRequest(
    val rating: Int,
    val comment: String?
)
