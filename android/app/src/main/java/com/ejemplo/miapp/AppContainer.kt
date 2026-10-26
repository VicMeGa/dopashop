package com.ejemplo.miapp

import android.content.Context
import com.ejemplo.miapp.data.auth.AuthRepository
import com.ejemplo.miapp.data.network.AuthApiService
import com.ejemplo.miapp.data.network.AuthInterceptor
import com.ejemplo.miapp.data.network.StoreApiService
import com.ejemplo.miapp.data.network.TokenAuthenticator
import com.ejemplo.miapp.data.session.SessionStore
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val gson = GsonBuilder().create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        redactHeader("Authorization")
    }

    private val bareClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val sessionStore = SessionStore(
        context = context.applicationContext,
        gson = gson
    )

    private val authApi: AuthApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(bareClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(AuthApiService::class.java)

    private val authenticatedClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sessionStore))
        .addInterceptor(loggingInterceptor)
        .authenticator(TokenAuthenticator(authApi, sessionStore))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: StoreApiService = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(authenticatedClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(StoreApiService::class.java)

    val authRepository = AuthRepository(
        authApi = authApi,
        sessionStore = sessionStore,
        gson = gson
    )
}
