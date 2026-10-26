package com.ejemplo.miapp.data.auth

import com.ejemplo.miapp.data.model.ApiError
import com.ejemplo.miapp.data.model.AuthResponse
import com.ejemplo.miapp.data.model.LoginRequest
import com.ejemplo.miapp.data.model.LogoutRequest
import com.ejemplo.miapp.data.model.RegisterRequest
import com.ejemplo.miapp.data.model.Session
import com.ejemplo.miapp.data.network.AuthApiService
import com.ejemplo.miapp.data.session.SessionStore
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val authApi: AuthApiService,
    private val sessionStore: SessionStore,
    private val gson: Gson = Gson()
) {
    val session = sessionStore.session
    val isSessionReady = sessionStore.isReady

    suspend fun login(email: String, password: String): Session =
        authenticate("No se pudo iniciar sesión") {
            authApi.login(LoginRequest(email.trim(), password))
        }

    suspend fun register(fullName: String, email: String, password: String): Session =
        authenticate("No se pudo completar el registro") {
            authApi.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    fullName = fullName.trim()
                )
            )
        }

    suspend fun logout() {
        val refreshToken = sessionStore.session.value?.refreshToken
        sessionStore.clear()
        if (refreshToken != null) {
            runCatching { authApi.logout(LogoutRequest(refreshToken)) }
        }
    }

    private suspend fun authenticate(
        fallback: String,
        call: suspend () -> AuthResponse
    ): Session {
        val response = try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AuthException(errorMessage(e, fallback))
        }
        val session = Session(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            user = response.user
        )
        sessionStore.save(session)
        return session
    }

    private fun errorMessage(error: Exception, fallback: String): String {
        if (error is HttpException) {
            val body = error.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) {
                val apiError = runCatching {
                    gson.fromJson(body, ApiError::class.java)
                }.getOrNull()
                apiError?.message?.takeIf { it.isNotBlank() }?.let { return it }
                apiError?.error?.takeIf { it.isNotBlank() }?.let { return it }
            }
            if (error.code() == 401) return "Credenciales inválidas"
        }
        if (error is IOException) return "No se pudo conectar con el servidor"
        return fallback
    }
}

class AuthException(message: String) : Exception(message)
