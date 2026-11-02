package com.ejemplo.miapp.ui.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.OrderResponse
import com.ejemplo.miapp.data.network.ApiErrorParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: OrderResponse? = null,
    val errorMessage: String? = null
)

class OrderDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = getApplication<DopapcApplication>().container.apiService
    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadOrder(orderId: Long) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, order = null, errorMessage = null)
            }
            try {
                val order = apiService.getOrderById(orderId)
                _uiState.update { it.copy(isLoading = false, order = order) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.message(e, "Error al cargar la orden")
                    )
                }
            }
        }
    }
}
