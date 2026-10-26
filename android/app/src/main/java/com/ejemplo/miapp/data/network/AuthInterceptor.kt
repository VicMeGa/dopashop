package com.ejemplo.miapp.data.network

import com.ejemplo.miapp.data.session.SessionStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStore: SessionStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header("Authorization") != null) {
            return chain.proceed(request)
        }
        val token = sessionStore.session.value?.accessToken
            ?: return chain.proceed(request)
        return chain.proceed(
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        )
    }
}
