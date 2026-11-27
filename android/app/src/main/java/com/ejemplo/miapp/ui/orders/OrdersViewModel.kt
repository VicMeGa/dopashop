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

data class OrdersUiState(
    val isLoading: Boolean = true,
    val orders: List<OrderResponse> = emptyList(),
    val errorMessage: String? = null
)

class OrdersViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = getApplication<DopapcApplication>().container.apiService
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, orders = emptyList(), errorMessage = null)
            }
            try {
                val orders = apiService.getMyOrders()
                _uiState.update { it.copy(isLoading = false, orders = orders) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.message(e, "Error al cargar tus órdenes")
                    )
                }
            }
        }
    }
}
