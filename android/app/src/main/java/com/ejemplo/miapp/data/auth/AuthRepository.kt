package com.ejemplo.miapp.data.auth

import com.ejemplo.miapp.data.model.AuthResponse
import com.ejemplo.miapp.data.model.LoginRequest
import com.ejemplo.miapp.data.model.LogoutRequest
import com.ejemplo.miapp.data.model.RegisterRequest
import com.ejemplo.miapp.data.model.Session
import com.ejemplo.miapp.data.network.ApiErrorParser
import com.ejemplo.miapp.data.network.AuthApiService
import com.ejemplo.miapp.data.session.SessionStore
import kotlinx.coroutines.CancellationException

class AuthRepository(
    private val authApi: AuthApiService,
    private val sessionStore: SessionStore
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
            throw AuthException(
                ApiErrorParser.message(
                    error = e,
                    fallback = fallback,
                    unauthorizedMessage = "Credenciales inválidas"
                )
            )
        }
        val session = Session(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            user = response.user
        )
        sessionStore.save(session)
        return session
    }
}

class AuthException(message: String) : Exception(message)
