package com.ejemplo.miapp.data.network

import com.ejemplo.miapp.data.model.RefreshRequest
import com.ejemplo.miapp.data.model.Session
import com.ejemplo.miapp.data.session.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException

class TokenAuthenticator(
    private val authApi: AuthApiService,
    private val sessionStore: SessionStore
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        if (response.request.url.encodedPath.startsWith("/api/auth/")) return null

        val requestToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        synchronized(this) {
            val session = sessionStore.session.value ?: return null

            if (requestToken != session.accessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${session.accessToken}")
                    .build()
            }

            val refreshed = try {
                runBlocking(Dispatchers.IO) {
                    authApi.refresh(RefreshRequest(session.refreshToken))
                }
            } catch (e: HttpException) {
                if (e.code() == 401 || e.code() == 403) {
                    sessionStore.clearBlocking()
                }
                return null
            } catch (e: IOException) {
                return null
            } catch (e: Exception) {
                return null
            }

            val newSession = Session(
                accessToken = refreshed.accessToken,
                refreshToken = refreshed.refreshToken,
                user = refreshed.user
            )
            return try {
                sessionStore.saveBlocking(newSession)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newSession.accessToken}")
                    .build()
            } catch (e: Exception) {
                sessionStore.clearBlocking()
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
