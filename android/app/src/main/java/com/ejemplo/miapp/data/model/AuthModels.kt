package com.ejemplo.miapp.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class LogoutRequest(
    val refreshToken: String
)

data class LogoutResponse(
    val status: String
)

data class UserSummary(
    val id: Long,
    val email: String,
    val fullName: String,
    val role: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserSummary
)

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val user: UserSummary
)

data class ApiError(
    val status: Int? = null,
    val error: String? = null,
    val message: String? = null
)
