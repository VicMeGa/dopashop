package com.ejemplo.miapp.ui.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ejemplo.miapp.data.model.OrderResponse
import com.ejemplo.miapp.ui.common.ErrorBanner
import com.ejemplo.miapp.ui.common.formatOrderDateTime
import com.ejemplo.miapp.ui.common.formatPrice

private val TEXT_COLOR = Color(0xFF08060d)
private val MUTED_COLOR = Color(0xFF6b6375)
private val BORDER_COLOR = Color(0xFFe5e4e7)
private val SUCCESS_COLOR = Color(0xFF16a34a)

@Composable
fun OrderConfirmedScreen(
    viewModel: OrderDetailViewModel,
    orderId: Long,
    onBackToCatalog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) { viewModel.loadOrder(orderId) }

    val order = uiState.order
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

        order == null -> {
            OrderConfirmedError(
                message = uiState.errorMessage ?: "No se pudo cargar la orden.",
                onRetry = { viewModel.loadOrder(orderId) },
                onBackToCatalog = onBackToCatalog
            )
        }

        else -> {
            OrderConfirmedContent(
                order = order,
                onBackToCatalog = onBackToCatalog
            )
        }
    }
}

@Composable
private fun OrderConfirmedContent(
    order: OrderResponse,
    onBackToCatalog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color(0xFFdcfce7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = SUCCESS_COLOR
            )
        }

        Text(
            text = "¡Compra realizada!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TEXT_COLOR
        )

        Text(
            text = "Tu pedido fue confirmado correctamente.",
            fontSize = 14.sp,
            color = MUTED_COLOR,
            textAlign = TextAlign.Center
        )

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BORDER_COLOR),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoRow(label = "Nro. de orden", value = "#${order.id}")
                InfoRow(
                    label = "Fecha",
                    value = formatOrderDateTime(order.createdAt)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estado", fontSize = 13.sp, color = MUTED_COLOR)
                    OrderStatusBadge(status = order.status)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SectionTitle("Productos")
            order.items.forEach { item ->
                OrderItemRow(
                    title = item.productTitle,
                    imageUrl = item.imageUrl,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    subtotal = item.subtotal
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = BORDER_COLOR
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total pagado",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_COLOR
                )
                Text(
                    text = formatPrice(order.total),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TEXT_COLOR
                )
            }
        }

        order.shipment?.let { shipment ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SectionTitle("Envío")
                OrderStatusBadge(
                    status = shipment.status,
                    type = OrderBadgeType.SHIPMENT
                )
                if (!shipment.trackingCode.isNullOrBlank()) {
                    Text(
                        text = "Seguimiento: ${shipment.trackingCode}",
                        fontSize = 13.sp,
                        color = TEXT_COLOR
                    )
                }
            }
        }

        Button(
            onClick = onBackToCatalog,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al catálogo")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = MUTED_COLOR)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TEXT_COLOR,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TEXT_COLOR
    )
}

@Composable
private fun OrderConfirmedError(
    message: String,
    onRetry: () -> Unit,
    onBackToCatalog: () -> Unit
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
            TextButton(onClick = onBackToCatalog) {
                Text("Volver al catálogo")
            }
        }
    }
}
