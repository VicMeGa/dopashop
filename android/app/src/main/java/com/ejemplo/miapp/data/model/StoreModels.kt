package com.ejemplo.miapp.data.model

data class StoreResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerName: String?,
    val createdAt: String?
)

data class StoreRequest(
    val name: String,
    val description: String?
)

data class ProductUpsertRequest(
    val title: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val categoryId: Long,
    val imageUrls: List<String>
)

data class ImageUploadResponse(
    val url: String
)
