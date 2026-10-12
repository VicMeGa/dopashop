package com.ejemplo.miapp.data.network

import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface StoreApiService {
    @GET("api/store/products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: Long? = null
    ): List<ProductResponse>

    @GET("api/store/categories")
    suspend fun getCategories(): List<CategoryResponse>
}
