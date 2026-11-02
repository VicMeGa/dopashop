package com.ejemplo.miapp.ui.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ejemplo.miapp.data.model.AddressResponse
import com.ejemplo.miapp.data.model.OrderItemRequest
import com.ejemplo.miapp.data.model.PaymentMethodResponse
import com.ejemplo.miapp.ui.cart.CartViewModel
import com.ejemplo.miapp.ui.common.ErrorBanner
import com.ejemplo.miapp.ui.common.formatPrice
import com.ejemplo.miapp.ui.orders.OrderItemRow

private val TEXT_COLOR = Color(0xFF08060d)
private val MUTED_COLOR = Color(0xFF6b6375)
private val BORDER_COLOR = Color(0xFFe5e4e7)
private val PRIMARY_COLOR = Color(0xFF2563eb)

@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    cartViewModel: CartViewModel,
    onBackToCart: () -> Unit,
    onManageAddresses: () -> Unit,
    onManagePayments: () -> Unit,
    onOrderConfirmed: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by cartViewModel.items.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }

    LaunchedEffect(uiState.approvedOrderId) {
        val orderId = uiState.approvedOrderId ?: return@LaunchedEffect
        viewModel.consumeApprovedOrder()
        onOrderConfirmed(orderId)
    }

    if (cartItems.isEmpty()) {
        CheckoutMessage(
            message = "Tu carrito está vacío.",
            actionLabel = "Volver al carrito",
            onAction = onBackToCart,
            secondaryLabel = null,
            onSecondary = null
        )
        return
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.errorMessage != null &&
        (uiState.addresses.isEmpty() || uiState.paymentMethods.isEmpty())
    ) {
        CheckoutError(
            message = uiState.errorMessage.orEmpty(),
            onRetry = viewModel::load,
            onBackToCart = onBackToCart
        )
        return
    }

    if (uiState.addresses.isEmpty()) {
        CheckoutMessage(
            message = "Necesitás una dirección de envío para continuar.",
            actionLabel = "Agregar dirección",
            onAction = onManageAddresses,
            secondaryLabel = "Volver al carrito",
            onSecondary = onBackToCart
        )
        return
    }

    if (uiState.paymentMethods.isEmpty()) {
        CheckoutMessage(
            message = "Necesitás un método de pago para continuar.",
            actionLabel = "Agregar método de pago",
            onAction = onManagePayments,
            secondaryLabel = "Volver al carrito",
            onSecondary = onBackToCart
        )
        return
    }

    val orderItems = cartItems.map {
        OrderItemRequest(productId = it.product.id, quantity = it.quantity)
    }
    val total = cartItems.sumOf { it.product.price * it.quantity }
    val itemCount = cartItems.sumOf { it.quantity }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.errorMessage?.let { message ->
                ErrorBanner(message = message)
            }

            if (uiState.paymentRejected) {
                PaymentRejectedBanner()
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Dirección de envío")
                uiState.addresses.forEach { address ->
                    AddressOption(
                        address = address,
                        selected = address.id == uiState.selectedAddressId,
                        enabled = !uiState.isSubmitting,
                        onClick = { viewModel.selectAddress(address.id) }
                    )
                }
                TextButton(
                    onClick = onManageAddresses,
                    enabled = !uiState.isSubmitting
                ) {
                    Text("Gestionar direcciones")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Método de pago")
                uiState.paymentMethods.forEach { method ->
                    PaymentMethodOption(
                        method = method,
                        selected = method.id == uiState.selectedPaymentMethodId,
                        enabled = !uiState.isSubmitting,
                        onClick = { viewModel.selectPaymentMethod(method.id) }
                    )
                }
                TextButton(
                    onClick = onManagePayments,
                    enabled = !uiState.isSubmitting
                ) {
                    Text("Gestionar métodos de pago")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionTitle("Resumen")
                cartItems.forEach { item ->
                    OrderItemRow(
                        title = item.product.title,
                        imageUrl = item.product.images.firstOrNull(),
                        unitPrice = item.product.price,
                        quantity = item.quantity,
                        subtotal = item.product.price * item.quantity
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
                    Text("Productos", fontSize = 14.sp, color = MUTED_COLOR)
                    Text("$itemCount", fontSize = 14.sp, color = TEXT_COLOR)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = formatPrice(total),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_COLOR
                    )
                }
            }
        }

        CheckoutFooter(
            isSubmitting = uiState.isSubmitting,
            hasPendingOrder = uiState.pendingOrderId != null,
            onBackToCart = onBackToCart,
            onConfirm = { viewModel.confirm(orderItems) },
            onRetryPayment = viewModel::retryPayment
        )
    }
}

@Composable
private fun CheckoutFooter(
    isSubmitting: Boolean,
    hasPendingOrder: Boolean,
    onBackToCart: () -> Unit,
    onConfirm: () -> Unit,
    onRetryPayment: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBackToCart,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (hasPendingOrder) "Modificar carrito" else "Regresar al carrito",
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = if (hasPendingOrder) onRetryPayment else onConfirm,
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PRIMARY_COLOR),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when {
                        isSubmitting -> "Procesando..."
                        hasPendingOrder -> "Reintentar pago"
                        else -> "Confirmar compra"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AddressOption(
    address: AddressResponse,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFeff6ff) else Color.White
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) PRIMARY_COLOR else BORDER_COLOR
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(selectedColor = PRIMARY_COLOR)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = address.street,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TEXT_COLOR
                )
                Text(
                    text = "${address.city}, ${address.state} ${address.zipCode}",
                    fontSize = 12.sp,
                    color = MUTED_COLOR
                )
                if (address.isDefault) {
                    DefaultBadge()
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodOption(
    method: PaymentMethodResponse,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFeff6ff) else Color.White
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) PRIMARY_COLOR else BORDER_COLOR
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(selectedColor = PRIMARY_COLOR)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${method.cardBrand} •••• ${method.cardLastFour}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TEXT_COLOR
                )
                Text(
                    text = method.alias,
                    fontSize = 12.sp,
                    color = MUTED_COLOR
                )
            }
        }
    }
}

@Composable
private fun DefaultBadge() {
    Surface(
        color = Color(0xFFeff6ff),
        shape = CircleShape
    ) {
        Text(
            text = "Predeterminada",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = PRIMARY_COLOR,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
private fun PaymentRejectedBanner() {
    Surface(
        color = Color(0xFFfef3c7),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "Pago rechazado. Podés reintentar o modificar el carrito.",
            fontSize = 13.sp,
            color = Color(0xFF92400e),
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun CheckoutError(
    message: String,
    onRetry: () -> Unit,
    onBackToCart: () -> Unit
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
            TextButton(onClick = onBackToCart) {
                Text("Volver al carrito")
            }
        }
    }
}

@Composable
private fun CheckoutMessage(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    secondaryLabel: String?,
    onSecondary: (() -> Unit)?
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
            Text(
                text = message,
                fontSize = 16.sp,
                color = TEXT_COLOR,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionLabel)
            }
            if (secondaryLabel != null && onSecondary != null) {
                TextButton(onClick = onSecondary) {
                    Text(secondaryLabel)
                }
            }
        }
    }
}
