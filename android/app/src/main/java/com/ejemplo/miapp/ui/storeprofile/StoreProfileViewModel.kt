package com.ejemplo.miapp.ui.storeprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.StoreResponse
import com.ejemplo.miapp.data.network.ApiErrorParser
import com.ejemplo.miapp.data.network.StoreApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class StoreProfileUiState(
    val storeId: Long? = null,
    val isLoading: Boolean = true,
    val store: StoreResponse? = null,
    val notFound: Boolean = false,
    val errorMessage: String? = null,
    val products: List<ProductResponse> = emptyList(),
    val isLoadingProducts: Boolean = false,
    val productsError: String? = null
)

class StoreProfileViewModel(
    private val apiService: StoreApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreProfileUiState())
    val uiState: StateFlow<StoreProfileUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var productsJob: Job? = null

    fun load(storeId: Long) {
        loadJob?.cancel()
        productsJob?.cancel()
        _uiState.value = StoreProfileUiState(storeId = storeId, isLoading = true)

        loadJob = viewModelScope.launch {
            try {
                val response = apiService.getStoreById(storeId)
                if (response.code() == 404) {
                    _uiState.update {
                        it.copy(isLoading = false, notFound = true, store = null)
                    }
                    return@launch
                }
                if (!response.isSuccessful) throw HttpException(response)
                val store = response.body()
                if (store == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            store = null,
                            errorMessage = "Error al cargar la tienda"
                        )
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        store = store,
                        notFound = false,
                        errorMessage = null
                    )
                }
                loadProducts()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        store = null,
                        errorMessage = ApiErrorParser.message(
                            e,
                            "Error al cargar la tienda"
                        )
                    )
                }
            }
        }
    }

    fun retry() {
        _uiState.value.storeId?.let(::load)
    }

    fun loadProducts() {
        val storeId = _uiState.value.storeId ?: return
        productsJob?.cancel()
        productsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingProducts = true, productsError = null)
            }
            try {
                val products = apiService.getStoreProducts(storeId)
                _uiState.update {
                    it.copy(isLoadingProducts = false, products = products)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoadingProducts = false,
                        productsError = ApiErrorParser.message(
                            e,
                            "Error al cargar los productos de la tienda"
                        )
                    )
                }
            }
        }
    }

    fun retryProducts() {
        loadProducts()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DopapcApplication
                StoreProfileViewModel(apiService = app.container.apiService)
            }
        }
    }
}
