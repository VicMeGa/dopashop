package com.ejemplo.miapp.ui.orders

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class OrderBadgeType {
    ORDER,
    SHIPMENT
}

private data class BadgeStyle(
    val label: String,
    val background: Color,
    val content: Color
)

private val ORDER_STATUS_STYLES = mapOf(
    "PENDING" to BadgeStyle("Pendiente", Color(0xFFf3f4f6), Color(0xFF6b6375)),
    "CONFIRMED" to BadgeStyle("Confirmada", Color(0xFFdcfce7), Color(0xFF166534)),
    "CANCELLED" to BadgeStyle("Cancelada", Color(0xFFfee2e2), Color(0xFFb91c1c))
)

private val SHIPMENT_STATUS_STYLES = mapOf(
    "PENDING" to BadgeStyle("Pendiente", Color(0xFFf3f4f6), Color(0xFF6b6375)),
    "PREPARING" to BadgeStyle("Preparando", Color(0xFFdbeafe), Color(0xFF1d4ed8)),
    "SHIPPED" to BadgeStyle("Enviado", Color(0xFFf3e8ff), Color(0xFF7e22ce)),
    "DELIVERED" to BadgeStyle("Entregado", Color(0xFFdcfce7), Color(0xFF166534))
)

private val FALLBACK_STYLE = BadgeStyle("", Color(0xFFf3f4f6), Color(0xFF6b6375))

@Composable
fun OrderStatusBadge(
    status: String,
    type: OrderBadgeType = OrderBadgeType.ORDER
) {
    val styles = if (type == OrderBadgeType.SHIPMENT) {
        SHIPMENT_STATUS_STYLES
    } else {
        ORDER_STATUS_STYLES
    }
    val style = styles[status] ?: FALLBACK_STYLE.copy(label = status)

    Surface(
        color = style.background,
        shape = CircleShape
    ) {
        Text(
            text = style.label,
            color = style.content,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
