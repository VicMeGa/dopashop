package com.ejemplo.miapp.ui.product

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejemplo.miapp.data.model.ProductReviewsSummaryResponse
import com.ejemplo.miapp.data.model.ReviewResponse
import com.ejemplo.miapp.ui.common.ErrorBanner
import com.ejemplo.miapp.ui.common.formatOrderDate
import java.util.Locale

private val TEXT_COLOR = Color(0xFF08060d)
private val MUTED_COLOR = Color(0xFF6b6375)
private val BORDER_COLOR = Color(0xFFe5e4e7)
private val PRIMARY_COLOR = Color(0xFF2563eb)
private val STAR_COLOR = Color(0xFFf59e0b)
private val EMPTY_STAR_COLOR = Color(0xFFd1d5db)

@Composable
fun ProductReviewsSection(
    productId: Long,
    reviewsState: ProductReviewsUiState,
    isAuthenticated: Boolean,
    onRetry: () -> Unit,
    onLoginClick: () -> Unit,
    onSubmitReview: (rating: Int, comment: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Reseñas",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TEXT_COLOR
        )

        when {
            reviewsState.isLoading -> ReviewsLoading()
            reviewsState.errorMessage != null -> ReviewsError(
                message = reviewsState.errorMessage,
                onRetry = onRetry
            )
            reviewsState.summary == null || reviewsState.summary.totalReviews == 0 -> ReviewsEmpty()
            else -> reviewsState.summary?.let { ReviewsContent(summary = it) } ?: ReviewsEmpty()
        }

        HorizontalDivider(color = BORDER_COLOR)

        if (isAuthenticated) {
            ReviewForm(
                productId = productId,
                isSubmitting = reviewsState.isSubmitting,
                submitError = reviewsState.submitError,
                submitSuccess = reviewsState.submitSuccess,
                onSubmit = onSubmitReview
            )
        } else {
            LoginPrompt(onLoginClick = onLoginClick)
        }
    }
}

@Composable
private fun ReviewsContent(summary: ProductReviewsSummaryResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = summary.averageRating?.let {
                    String.format(Locale.US, "%.1f", it)
                } ?: "—",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TEXT_COLOR
            )
            Text("★", fontSize = 18.sp, color = STAR_COLOR)
            Text("·", fontSize = 14.sp, color = MUTED_COLOR)
            Text(
                text = "${summary.totalReviews} ${
                    if (summary.totalReviews == 1) "reseña" else "reseñas"
                }",
                fontSize = 13.sp,
                color = MUTED_COLOR
            )
        }

        summary.reviews.forEach { review ->
            ReviewItem(review = review)
        }
    }
}

@Composable
private fun ReviewItem(review: ReviewResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BORDER_COLOR)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.authorName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TEXT_COLOR,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatOrderDate(review.createdAt),
                    fontSize = 12.sp,
                    color = MUTED_COLOR,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Stars(rating = review.rating)

            review.comment?.takeIf { it.isNotBlank() }?.let { comment ->
                Text(
                    text = comment,
                    fontSize = 14.sp,
                    color = MUTED_COLOR,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun Stars(rating: Int) {
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "$rating de 5 estrellas"
        }
    ) {
        repeat(5) { index ->
            Text(
                text = if (index < rating) "★" else "☆",
                fontSize = 16.sp,
                color = if (index < rating) STAR_COLOR else EMPTY_STAR_COLOR
            )
        }
    }
}

@Composable
private fun ReviewsLoading() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = PRIMARY_COLOR
        )
        Text(
            text = "Cargando reseñas...",
            fontSize = 14.sp,
            color = MUTED_COLOR
        )
    }
}

@Composable
private fun ReviewsError(message: String, onRetry: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ErrorBanner(message = message)
        TextButton(
            onClick = onRetry,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun ReviewsEmpty() {
    Text(
        text = "Aún no hay reseñas para este producto. " +
            "¡Sé el primero en comentar si ya lo compraste!",
        fontSize = 14.sp,
        color = MUTED_COLOR,
        lineHeight = 20.sp
    )
}

@Composable
private fun LoginPrompt(onLoginClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Iniciá sesión para dejar una reseña.",
            fontSize = 14.sp,
            color = MUTED_COLOR
        )
        TextButton(
            onClick = onLoginClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Iniciar sesión")
        }
    }
}

@Composable
private fun ReviewForm(
    productId: Long,
    isSubmitting: Boolean,
    submitError: String?,
    submitSuccess: Boolean,
    onSubmit: (rating: Int, comment: String) -> Unit
) {
    var rating by rememberSaveable(productId) { mutableStateOf(0) }
    var comment by rememberSaveable(productId) { mutableStateOf("") }

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            rating = 0
            comment = ""
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Dejá tu reseña",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TEXT_COLOR
        )

        Text(
            text = "Tu puntaje",
            fontSize = 13.sp,
            color = MUTED_COLOR
        )

        Row {
            (1..5).forEach { value ->
                IconButton(
                    onClick = { rating = value },
                    enabled = !isSubmitting,
                    modifier = Modifier.semantics {
                        contentDescription = "$value estrellas"
                    }
                ) {
                    Text(
                        text = if (value <= rating) "★" else "☆",
                        fontSize = 28.sp,
                        color = if (value <= rating) STAR_COLOR else EMPTY_STAR_COLOR
                    )
                }
            }
        }

        if (rating == 0) {
            Text(
                text = "Elegí de 1 a 5 estrellas.",
                fontSize = 12.sp,
                color = MUTED_COLOR
            )
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it.take(2000) },
            label = { Text("Comentario (opcional)") },
            minLines = 3,
            maxLines = 6,
            enabled = !isSubmitting,
            shape = RoundedCornerShape(8.dp),
            supportingText = {
                Text("${comment.length}/2000")
            },
            modifier = Modifier.fillMaxWidth()
        )

        submitError?.let { message ->
            ErrorBanner(message = message)
        }

        if (submitSuccess) {
            SuccessBanner(message = "¡Gracias por tu reseña!")
        }

        Button(
            onClick = { onSubmit(rating, comment) },
            enabled = !isSubmitting && rating in 1..5,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PRIMARY_COLOR,
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (isSubmitting) "Enviando..." else "Enviar reseña",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SuccessBanner(message: String) {
    Surface(
        color = Color(0xFFdcfce7),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = Color(0xFF166534),
            fontSize = 14.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}
