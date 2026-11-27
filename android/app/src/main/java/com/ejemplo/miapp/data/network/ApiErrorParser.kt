package com.ejemplo.miapp.data.network

import com.ejemplo.miapp.data.model.ApiError
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

object ApiErrorParser {
    private val gson = Gson()

    fun message(
        error: Throwable,
        fallback: String,
        unauthorizedMessage: String? = null
    ): String {
        if (error is HttpException) {
            val apiError = runCatching {
                error.response()?.errorBody()?.string()
            }.getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?.let { body -> runCatching { gson.fromJson(body, ApiError::class.java) }.getOrNull() }

            apiError?.message?.takeIf { it.isNotBlank() }?.let { return it }
            apiError?.error?.takeIf { it.isNotBlank() }?.let { return it }
            if (error.code() == 401 && unauthorizedMessage != null) return unauthorizedMessage
        }
        if (error is IOException) return "No se pudo conectar con el servidor"
        return fallback
    }
}
