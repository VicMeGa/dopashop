package com.ejemplo.miapp.data.model

data class CartItemResponse(
    val product: ProductResponse,
    val quantity: Int,
    val subtotal: Double,
    val available: Boolean
)

data class CartResponse(
    val id: Long?,
    val items: List<CartItemResponse>,
    val itemCount: Int,
    val total: Double
)

data class CartItemRequest(
    val productId: Long,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)

data class MergeCartRequest(
    val items: List<CartItemRequest>
)
