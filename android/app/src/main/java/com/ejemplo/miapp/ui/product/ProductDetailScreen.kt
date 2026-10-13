package com.ejemplo.miapp.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Producto", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    Text(
                        text = uiState.errorMessage ?: "Error desconocido",
                        color = Color(0xFFe74c3c),
                        modifier = Modifier.align(Alignment.Center)
                    )
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
                        // Image container
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

                        // Category
                        product.category?.let { cat ->
                            Text(
                                text = cat.name.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6b6375)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Title
                        Text(
                            text = product.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF08060d)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Price
                        Text(
                            text = "$${product.price}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF08060d)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stock
                        Text(
                            text = if (product.stock > 0) "Stock disponible: ${product.stock}" else "Agotado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (product.stock > 0) Color(0xFF22c55e) else Color(0xFFe74c3c)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(color = Color(0xFFe5e4e7))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description title
                        Text(
                            text = "Descripción",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF08060d)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description body
                        Text(
                            text = product.description,
                            fontSize = 14.sp,
                            color = Color(0xFF6b6375),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
