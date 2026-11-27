package com.ejemplo.miapp.data.network

import com.ejemplo.miapp.data.model.AddressRequest
import com.ejemplo.miapp.data.model.AddressResponse
import com.ejemplo.miapp.data.model.CartItemRequest
import com.ejemplo.miapp.data.model.CartResponse
import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.CreateOrderRequest
import com.ejemplo.miapp.data.model.CreateReviewRequest
import com.ejemplo.miapp.data.model.MergeCartRequest
import com.ejemplo.miapp.data.model.ImageUploadResponse
import com.ejemplo.miapp.data.model.OrderResponse
import com.ejemplo.miapp.data.model.PaymentAttemptResponse
import com.ejemplo.miapp.data.model.PaymentMethodRequest
import com.ejemplo.miapp.data.model.PaymentMethodResponse
import com.ejemplo.miapp.data.model.ProductUpsertRequest
import com.ejemplo.miapp.data.model.ReviewResponse
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.ProductReviewsSummaryResponse
import com.ejemplo.miapp.data.model.StoreRequest
import com.ejemplo.miapp.data.model.StoreResponse
import com.ejemplo.miapp.data.model.UpdateCartItemRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface StoreApiService {
    @GET("api/store/products")
    suspend fun getProducts(
        @Query("title") title: String? = null,
        @Query("categoryId") categoryId: Long? = null
    ): List<ProductResponse>

    @GET("api/store/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): ProductResponse

    @GET("api/store/categories")
    suspend fun getCategories(): List<CategoryResponse>

    @GET("api/stores/mine")
    suspend fun getMyStore(): Response<StoreResponse>

    @GET("api/stores/{id}")
    suspend fun getStoreById(@Path("id") id: Long): Response<StoreResponse>

    @GET("api/stores/{id}/products")
    suspend fun getStoreProducts(@Path("id") id: Long): List<ProductResponse>

    @POST("api/stores")
    suspend fun createStore(@Body request: StoreRequest): StoreResponse

    @Multipart
    @POST("api/stores/mine/images")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ImageUploadResponse

    @GET("api/stores/mine/products")
    suspend fun getMyProducts(): List<ProductResponse>

    @POST("api/stores/mine/products")
    suspend fun createProduct(@Body request: ProductUpsertRequest): ProductResponse

    @PUT("api/stores/mine/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Long,
        @Body request: ProductUpsertRequest
    ): ProductResponse

    @DELETE("api/stores/mine/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<Unit>

    @PUT("api/stores/mine/products/{id}/reactivate")
    suspend fun reactivateProduct(@Path("id") id: Long): Response<Unit>

    @GET("api/addresses")
    suspend fun getAddresses(): List<AddressResponse>

    @POST("api/addresses")
    suspend fun createAddress(@Body request: AddressRequest): AddressResponse

    @PUT("api/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") id: Long,
        @Body request: AddressRequest
    ): AddressResponse

    @DELETE("api/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: Long): Response<Unit>

    @GET("api/payment-methods")
    suspend fun getPaymentMethods(): List<PaymentMethodResponse>

    @POST("api/payment-methods")
    suspend fun createPaymentMethod(@Body request: PaymentMethodRequest): PaymentMethodResponse

    @DELETE("api/payment-methods/{id}")
    suspend fun deletePaymentMethod(@Path("id") id: Long): Response<Unit>

    @GET("api/cart")
    suspend fun getCart(): CartResponse

    @POST("api/cart/items")
    suspend fun addCartItem(@Body request: CartItemRequest): CartResponse

    @PUT("api/cart/items/{productId}")
    suspend fun updateCartItem(
        @Path("productId") productId: Long,
        @Body request: UpdateCartItemRequest
    ): CartResponse

    @DELETE("api/cart/items/{productId}")
    suspend fun removeCartItem(@Path("productId") productId: Long): Response<Unit>

    @DELETE("api/cart")
    suspend fun clearCart(): Response<Unit>

    @POST("api/cart/merge")
    suspend fun mergeCart(@Body request: MergeCartRequest): CartResponse

    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): OrderResponse

    @POST("api/orders/{id}/pay")
    suspend fun attemptPayment(@Path("id") id: Long): PaymentAttemptResponse

    @GET("api/orders/mine")
    suspend fun getMyOrders(): List<OrderResponse>

    @GET("api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): OrderResponse

    @GET("api/products/{productId}/reviews")
    suspend fun getProductReviews(
        @Path("productId") productId: Long
    ): ProductReviewsSummaryResponse

    @POST("api/products/{productId}/reviews")
    suspend fun createReview(
        @Path("productId") productId: Long,
        @Body request: CreateReviewRequest
    ): ReviewResponse
}
