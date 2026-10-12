package com.ejemplo.miapp.data.model

data class ProductResponse(
    val id: Long,
    val title: String,
    val price: Double,
    val description: String,
    val stock: Int,
    val images: List<String>,
    val category: CategoryResponse?,
    val storeId: Long?,
    val storeName: String?,
    val active: Boolean
)
