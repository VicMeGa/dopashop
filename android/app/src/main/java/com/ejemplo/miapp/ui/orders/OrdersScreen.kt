package com.ejemplo.miapp.ui.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ejemplo.miapp.data.model.OrderResponse
import com.ejemplo.miapp.ui.common.ErrorBanner
import com.ejemplo.miapp.ui.common.formatOrderDate
import com.ejemplo.miapp.ui.common.formatPrice

private val TEXT_COLOR = Color(0xFF08060d)
private val MUTED_COLOR = Color(0xFF6b6375)
private val BORDER_COLOR = Color(0xFFe5e4e7)

@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    onOrderClick: (Long) -> Unit,
    onGoToCatalog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null && uiState.orders.isEmpty() -> {
            OrdersError(
                message = uiState.errorMessage.orEmpty(),
                onRetry = viewModel::load,
                onGoToCatalog = onGoToCatalog
            )
        }

        uiState.orders.isEmpty() -> {
            OrdersEmpty(onGoToCatalog = onGoToCatalog)
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onOrderClick(order.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderResponse,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BORDER_COLOR)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Orden #${order.id}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_COLOR,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatOrderDate(order.createdAt),
                    fontSize = 12.sp,
                    color = MUTED_COLOR,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatPrice(order.total),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_COLOR
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OrderStatusBadge(status = order.status)
                    order.shipment?.let { shipment ->
                        OrderStatusBadge(
                            status = shipment.status,
                            type = OrderBadgeType.SHIPMENT
                        )
                    }
                }
            }

            Text(
                text = order.items.size.let { count ->
                    "$count ${if (count == 1) "producto" else "productos"}"
                },
                fontSize = 12.sp,
                color = MUTED_COLOR
            )
        }
    }
}

@Composable
private fun OrdersError(
    message: String,
    onRetry: () -> Unit,
    onGoToCatalog: () -> Unit
) {
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
            ErrorBanner(message = message)
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Reintentar")
            }
            TextButton(onClick = onGoToCatalog) {
                Text("Volver al catálogo")
            }
        }
    }
}

@Composable
private fun OrdersEmpty(onGoToCatalog: () -> Unit) {
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
                text = "Aún no has realizado ninguna compra.",
                fontSize = 16.sp,
                color = TEXT_COLOR,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onGoToCatalog,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ir al catálogo")
            }
        }
    }
}
