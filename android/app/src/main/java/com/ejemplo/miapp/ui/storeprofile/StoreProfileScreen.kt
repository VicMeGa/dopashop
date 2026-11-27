package com.ejemplo.miapp.ui.storeprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.ui.catalog.ProductItemCard
import com.ejemplo.miapp.ui.common.ErrorBanner

private val TEXT_COLOR = Color(0xFF08060d)
private val MUTED_COLOR = Color(0xFF6b6375)

@Composable
fun StoreProfileScreen(
    viewModel: StoreProfileViewModel,
    onBack: () -> Unit,
    onProductClick: (Long) -> Unit,
    onAddToCart: (ProductResponse) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563eb))
            }
        }

        uiState.notFound -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tienda no encontrada",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_COLOR
                    )
                    Text(
                        text = "La tienda que buscas no existe o fue eliminada.",
                        fontSize = 14.sp,
                        color = MUTED_COLOR
                    )
                    Button(onClick = onBack, shape = RoundedCornerShape(8.dp)) {
                        Text("Volver al catálogo")
                    }
                }
            }
        }

        uiState.errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ErrorBanner(
                        message = uiState.errorMessage
                            ?: "Error al cargar la tienda"
                    )
                    Button(
                        onClick = viewModel::retry,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reintentar")
                    }
                    TextButton(onClick = onBack) {
                        Text("Volver al catálogo")
                    }
                }
            }
        }

        uiState.store != null -> {
            val store = uiState.store ?: return
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = store.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TEXT_COLOR
                        )
                        store.description?.takeIf { it.isNotBlank() }?.let { description ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = description,
                                fontSize = 14.sp,
                                color = MUTED_COLOR
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Productos de la tienda (${uiState.products.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_COLOR
                    )
                }

                uiState.productsError?.let { message ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ErrorBanner(message = message)
                            TextButton(onClick = viewModel::retryProducts) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                if (uiState.isLoadingProducts) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2563eb))
                        }
                    }
                }

                if (!uiState.isLoadingProducts &&
                    uiState.productsError == null &&
                    uiState.products.isEmpty()
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Esta tienda aún no tiene productos publicados.",
                            fontSize = 14.sp,
                            color = MUTED_COLOR
                        )
                    }
                }

                items(uiState.products, key = { it.id }) { product ->
                    ProductItemCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAddToCart = { onAddToCart(product) }
                    )
                }
            }
        }
    }
}
