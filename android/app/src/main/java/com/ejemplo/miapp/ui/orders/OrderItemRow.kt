package com.ejemplo.miapp.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ejemplo.miapp.ui.common.formatPrice

@Composable
fun OrderItemRow(
    title: String,
    imageUrl: String?,
    unitPrice: Double,
    quantity: Int,
    subtotal: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFf8f8f8), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            } else {
                Text(
                    text = "Sin imagen",
                    fontSize = 9.sp,
                    color = Color(0xFF8a8594)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF08060d),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatPrice(unitPrice)} × $quantity",
                fontSize = 12.sp,
                color = Color(0xFF6b6375)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatPrice(subtotal),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF08060d)
        )
    }
}
