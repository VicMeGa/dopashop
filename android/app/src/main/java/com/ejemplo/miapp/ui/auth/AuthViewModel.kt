package com.ejemplo.miapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.auth.AuthException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = getApplication<DopapcApplication>().container.authRepository

    val session = repository.session
    val isSessionReady = repository.isSessionReady

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) = submit {
        repository.login(email, password)
    }

    fun register(fullName: String, email: String, password: String) = submit {
        repository.register(fullName, email, password)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun clearError() {
        _uiState.update {
            if (it.errorMessage == null) it else it.copy(errorMessage = null)
        }
    }

    private fun submit(block: suspend () -> Unit) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                block()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: AuthException) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Ocurrió un error inesperado"
                    )
                }
            }
        }
    }
}
