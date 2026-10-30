package com.ejemplo.miapp.data.model

data class AddressResponse(
    val id: Long,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val isDefault: Boolean
)

data class AddressRequest(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val isDefault: Boolean
)

data class PaymentMethodResponse(
    val id: Long,
    val alias: String,
    val cardLastFour: String,
    val cardBrand: String
)

data class PaymentMethodRequest(
    val alias: String,
    val cardLastFour: String,
    val cardBrand: String
)

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

data class CreateOrderRequest(
    val addressId: Long,
    val paymentMethodId: Long,
    val items: List<OrderItemRequest>
)

data class OrderItemResponse(
    val productId: Long,
    val imageUrl: String?,
    val productTitle: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

data class ShipmentResponse(
    val id: Long,
    val status: String,
    val trackingCode: String?,
    val updatedAt: String?
)

data class OrderResponse(
    val id: Long,
    val status: String,
    val total: Double,
    val createdAt: String,
    val items: List<OrderItemResponse>,
    val address: AddressResponse,
    val paymentMethod: PaymentMethodResponse,
    val shipment: ShipmentResponse?,
    val latestPaymentStatus: String?
)

data class PaymentAttemptResponse(
    val transactionId: Long,
    val status: String,
    val message: String,
    val order: OrderResponse
)
