package com.ejemplo.miapp.ui.catalog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.ProductResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val isLoading: Boolean = false,
    val products: List<ProductResponse> = emptyList(),
    val categories: List<CategoryResponse> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = getApplication<DopapcApplication>().container.apiService
    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        loadCategories()
        loadProducts()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = apiService.getCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Ignore category load error for now
            }
        }
    }

    fun loadProducts() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value
                val products = apiService.getProducts(
                    title = state.searchQuery.ifBlank { null },
                    categoryId = state.selectedCategoryId
                )
                _uiState.update { it.copy(isLoading = false, products = products) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Error al cargar productos") }
            }
        }
    }

    fun selectCategory(categoryId: Long?) {
        searchJob?.cancel()
        _uiState.update { it.copy(selectedCategoryId = if (it.selectedCategoryId == categoryId) null else categoryId) }
        loadProducts()
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            loadProducts()
        }
    }
}
