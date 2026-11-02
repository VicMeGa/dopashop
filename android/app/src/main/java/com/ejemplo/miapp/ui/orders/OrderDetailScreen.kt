package com.ejemplo.miapp.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
fun OrderDetailScreen(
    viewModel: OrderDetailViewModel,
    orderId: Long,
    onBack: () -> Unit
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
            OrderDetailError(
                message = uiState.errorMessage ?: "No se pudo cargar la orden.",
                onRetry = { viewModel.loadOrder(orderId) },
                onBack = onBack
            )
        }

        else -> {
            OrderDetailContent(order = order, onBack = onBack)
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: OrderResponse,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Orden #${order.id}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TEXT_COLOR
            )
            OrderStatusBadge(status = order.status)
        }

        Text(
            text = formatOrderDate(order.createdAt),
            fontSize = 13.sp,
            color = MUTED_COLOR
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    text = "Total",
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

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SectionTitle("Envío a")
            Text(order.address.street, fontSize = 14.sp, color = TEXT_COLOR)
            Text(
                text = "${order.address.city}, ${order.address.state} ${order.address.zipCode}",
                fontSize = 13.sp,
                color = MUTED_COLOR
            )
            Text(order.address.country, fontSize = 13.sp, color = MUTED_COLOR)
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SectionTitle("Pago")
            Text(
                text = "${order.paymentMethod.cardBrand} •••• ${order.paymentMethod.cardLastFour}",
                fontSize = 14.sp,
                color = TEXT_COLOR
            )
            Text(
                text = order.paymentMethod.alias,
                fontSize = 13.sp,
                color = MUTED_COLOR
            )
        }

        order.shipment?.let { shipment ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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

        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al historial")
        }
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
private fun OrderDetailError(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
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
            TextButton(onClick = onBack) {
                Text("Volver al historial")
            }
        }
    }
}
