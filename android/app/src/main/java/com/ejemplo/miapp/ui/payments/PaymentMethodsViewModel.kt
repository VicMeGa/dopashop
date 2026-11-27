package com.ejemplo.miapp.ui.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.PaymentMethodRequest
import com.ejemplo.miapp.data.model.PaymentMethodResponse
import com.ejemplo.miapp.data.network.ApiErrorParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class PaymentMethodsUiState(
    val isLoading: Boolean = false,
    val methods: List<PaymentMethodResponse> = emptyList(),
    val showForm: Boolean = false,
    val isSaving: Boolean = false,
    val deletingId: Long? = null,
    val errorMessage: String? = null
)

class PaymentMethodsViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = getApplication<DopapcApplication>().container.apiService
    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, methods = emptyList(), errorMessage = null)
            }
            try {
                val methods = apiService.getPaymentMethods()
                _uiState.update { it.copy(isLoading = false, methods = methods) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.message(e, "Error al cargar métodos de pago")
                    )
                }
            }
        }
    }

    fun startCreate() {
        _uiState.update { it.copy(showForm = true, errorMessage = null) }
    }

    fun cancelForm() {
        _uiState.update { it.copy(showForm = false) }
    }

    fun save(request: PaymentMethodRequest) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                apiService.createPaymentMethod(request)
                _uiState.update { it.copy(isSaving = false, showForm = false) }
                load()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = ApiErrorParser.message(e, "Error al crear método de pago")
                    )
                }
            }
        }
    }

    fun delete(id: Long) {
        if (_uiState.value.deletingId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(deletingId = id, errorMessage = null) }
            try {
                val response = apiService.deletePaymentMethod(id)
                if (!response.isSuccessful) throw HttpException(response)
                _uiState.update { it.copy(deletingId = null) }
                load()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        deletingId = null,
                        errorMessage = ApiErrorParser.message(e, "Error al eliminar método de pago")
                    )
                }
            }
        }
    }
}
