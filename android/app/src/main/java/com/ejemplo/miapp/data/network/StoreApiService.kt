package com.ejemplo.miapp.data.network

import com.ejemplo.miapp.data.model.AddressRequest
import com.ejemplo.miapp.data.model.AddressResponse
import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.CreateOrderRequest
import com.ejemplo.miapp.data.model.OrderResponse
import com.ejemplo.miapp.data.model.PaymentAttemptResponse
import com.ejemplo.miapp.data.model.PaymentMethodRequest
import com.ejemplo.miapp.data.model.PaymentMethodResponse
import com.ejemplo.miapp.data.model.ProductResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): OrderResponse

    @POST("api/orders/{id}/pay")
    suspend fun attemptPayment(@Path("id") id: Long): PaymentAttemptResponse

    @GET("api/orders/mine")
    suspend fun getMyOrders(): List<OrderResponse>

    @GET("api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): OrderResponse
}
