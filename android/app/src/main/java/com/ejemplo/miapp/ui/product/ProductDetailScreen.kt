package com.ejemplo.miapp.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ejemplo.miapp.data.model.ProductResponse

@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onBackClick: () -> Unit,
    onAddToCart: (ProductResponse) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2563eb)
                )
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Error desconocido",
                        color = Color(0xFFe74c3c)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onBackClick) {
                        Text("Volver")
                    }
                }
            }
            uiState.product != null -> {
                val product = uiState.product!!
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color(0xFFf8f8f8), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageUrl = product.images.firstOrNull() ?: ""
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = product.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    product.category?.let { cat ->
                        Text(
                            text = cat.name.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6b6375)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = product.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF08060d)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$${product.price}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF08060d)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (product.stock > 0) "Stock disponible: ${product.stock}" else "Agotado",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (product.stock > 0) Color(0xFF22c55e) else Color(0xFFe74c3c)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color(0xFFe5e4e7))

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Descripción",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF08060d)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = product.description?.takeIf { it.isNotBlank() } ?: "Sin descripción",
                        fontSize = 14.sp,
                        color = Color(0xFF6b6375),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onAddToCart(product) },
                        enabled = product.stock > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563eb),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (product.stock > 0) "Agregar al carrito" else "Sin stock",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
