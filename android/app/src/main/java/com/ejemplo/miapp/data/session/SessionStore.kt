package com.ejemplo.miapp.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ejemplo.miapp.data.model.Session
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val Context.sessionDataStore by preferencesDataStore(name = "session")

class SessionStore(
    context: Context,
    private val crypto: SessionCrypto = SessionCrypto(),
    private val gson: Gson = Gson()
) {
    private val dataStore = context.applicationContext.sessionDataStore
    private val mutex = Mutex()

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            mutex.withLock { loadLocked() }
        }
    }

    private suspend fun loadLocked() {
        val stored = runCatching { dataStore.data.first()[SESSION_KEY] }.getOrNull()
        val loaded = stored?.let { decode(it) }
        _session.value = loaded
        if (stored != null && loaded == null) {
            runCatching { dataStore.edit { it.remove(SESSION_KEY) } }
        }
        _isReady.value = true
    }

    private fun decode(encrypted: String): Session? = runCatching {
        gson.fromJson(crypto.decrypt(encrypted), Session::class.java)
    }.getOrNull()?.takeIf {
        it.accessToken.isNotBlank() && it.refreshToken.isNotBlank()
    }

    suspend fun save(session: Session) {
        mutex.withLock {
            val encrypted = crypto.encrypt(gson.toJson(session))
            dataStore.edit { it[SESSION_KEY] = encrypted }
            _session.value = session
        }
    }

    fun saveBlocking(session: Session) = runBlocking(Dispatchers.IO) {
        save(session)
    }

    suspend fun clear() {
        mutex.withLock {
            _session.value = null
            runCatching { dataStore.edit { it.remove(SESSION_KEY) } }
        }
    }

    fun clearBlocking() = runBlocking(Dispatchers.IO) {
        clear()
    }

    private companion object {
        val SESSION_KEY = stringPreferencesKey("session")
    }
}
