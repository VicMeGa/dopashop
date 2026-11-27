package com.ejemplo.miapp.data.network

import com.ejemplo.miapp.data.model.AuthResponse
import com.ejemplo.miapp.data.model.LoginRequest
import com.ejemplo.miapp.data.model.LogoutRequest
import com.ejemplo.miapp.data.model.LogoutResponse
import com.ejemplo.miapp.data.model.RefreshRequest
import com.ejemplo.miapp.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponse

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): LogoutResponse
}
