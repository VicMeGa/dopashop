package com.ejemplo.miapp.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onContinueShopping: () -> Unit,
    onCheckout: () -> Unit
) {
    val cartItems by viewModel.items.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tu carrito está vacío",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF08060d)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onContinueShopping,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ir al catálogo")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { viewModel.increase(item.product.id) },
                        onDecrease = { viewModel.decrease(item.product.id) },
                        onRemove = { viewModel.remove(item.product.id) }
                    )
                }
            }

            CartFooter(
                total = cartItems.sumOf { it.product.price * it.quantity },
                itemCount = cartItems.sumOf { it.quantity },
                onCheckout = onCheckout,
                onClear = viewModel::clear,
                onContinueShopping = onContinueShopping
            )
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.images.firstOrNull().orEmpty(),
                contentDescription = item.product.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFf8f8f8), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF08060d),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatPrice(item.product.price),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF08060d)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Subtotal: ${formatPrice(item.product.price * item.quantity)}",
                    fontSize = 12.sp,
                    color = Color(0xFF6b6375)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onDecrease,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("−", fontSize = 18.sp)
                    }
                    Text(
                        text = item.quantity.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    TextButton(
                        onClick = onIncrease,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("+", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onRemove,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Quitar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartFooter(
    total: Double,
    itemCount: Int,
    onCheckout: () -> Unit,
    onClear: () -> Unit,
    onContinueShopping: () -> Unit
) {
    Surface(
        color = Color.White,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF08060d)
                )
                Text(
                    text = formatPrice(total),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF08060d)
                )
            }
            Text(
                text = if (itemCount == 1) "1 producto" else "$itemCount productos",
                fontSize = 12.sp,
                color = Color(0xFF6b6375)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563eb),
                    contentColor = Color.White
                )
            ) {
                Text("Continuar al checkout", fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onContinueShopping) {
                    Text("Seguir comprando", fontSize = 12.sp)
                }
                TextButton(onClick = onClear) {
                    Text("Vaciar carrito", fontSize = 12.sp, color = Color(0xFFe74c3c))
                }
            }
        }
    }
}

private fun formatPrice(value: Double): String =
    String.format(Locale.US, "\$%.2f", value)
