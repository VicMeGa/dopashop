package com.ejemplo.miapp.ui.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.CreateReviewRequest
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.ProductReviewsSummaryResponse
import com.ejemplo.miapp.data.network.ApiErrorParser
import com.ejemplo.miapp.util.ShopValidators
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductReviewsUiState(
    val isLoading: Boolean = false,
    val summary: ProductReviewsSummaryResponse? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
)

data class ProductDetailUiState(
    val isLoading: Boolean = false,
    val product: ProductResponse? = null,
    val errorMessage: String? = null,
    val reviews: ProductReviewsUiState = ProductReviewsUiState()
)

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = getApplication<DopapcApplication>().container.apiService
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var reviewsJob: Job? = null
    private var submitJob: Job? = null

    fun loadProduct(productId: Long) {
        loadJob?.cancel()
        reviewsJob?.cancel()
        submitJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    product = null,
                    errorMessage = null,
                    reviews = ProductReviewsUiState(isLoading = true)
                )
            }
            try {
                val product = apiService.getProductById(productId)
                _uiState.update { it.copy(isLoading = false, product = product) }
                loadReviewsInternal(productId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        reviews = it.reviews.copy(isLoading = false),
                        errorMessage = ApiErrorParser.message(
                            e,
                            "Error al cargar el detalle del producto"
                        )
                    )
                }
            }
        }
    }

    fun loadReviews(productId: Long) {
        if (reviewsJob?.isActive == true) return
        reviewsJob = viewModelScope.launch {
            loadReviewsInternal(productId)
        }
    }

    fun submitReview(rating: Int, comment: String) {
        val productId = _uiState.value.product?.id ?: return
        if (_uiState.value.reviews.isSubmitting) return

        val trimmedComment = comment.trim()
        val validationError = ShopValidators.reviewRating(rating)
            ?: ShopValidators.reviewComment(trimmedComment)
        if (validationError != null) {
            _uiState.update {
                it.copy(
                    reviews = it.reviews.copy(
                        submitError = validationError,
                        submitSuccess = false
                    )
                )
            }
            return
        }

        submitJob?.cancel()
        submitJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    reviews = it.reviews.copy(
                        isSubmitting = true,
                        submitError = null,
                        submitSuccess = false
                    )
                )
            }
            try {
                apiService.createReview(
                    productId = productId,
                    request = CreateReviewRequest(
                        rating = rating,
                        comment = trimmedComment.ifBlank { null }
                    )
                )
                _uiState.update {
                    it.copy(
                        reviews = it.reviews.copy(
                            isSubmitting = false,
                            submitSuccess = true
                        )
                    )
                }
                reviewsJob?.cancel()
                loadReviewsInternal(productId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        reviews = it.reviews.copy(
                            isSubmitting = false,
                            submitError = ApiErrorParser.message(
                                e,
                                "Error al enviar la reseña"
                            )
                        )
                    )
                }
            }
        }
    }

    private suspend fun loadReviewsInternal(productId: Long) {
        _uiState.update {
            it.copy(
                reviews = it.reviews.copy(
                    isLoading = true,
                    errorMessage = null
                )
            )
        }
        try {
            val summary = apiService.getProductReviews(productId)
            _uiState.update {
                it.copy(
                    reviews = it.reviews.copy(
                        isLoading = false,
                        summary = summary
                    )
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update {
                it.copy(
                    reviews = it.reviews.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.message(
                            e,
                            "Error al cargar reseñas"
                        )
                    )
                )
            }
        }
    }
}
