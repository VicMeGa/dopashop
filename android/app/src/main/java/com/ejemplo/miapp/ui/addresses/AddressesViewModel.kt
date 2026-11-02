package com.ejemplo.miapp.ui.addresses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.AddressRequest
import com.ejemplo.miapp.data.model.AddressResponse
import com.ejemplo.miapp.data.network.ApiErrorParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AddressesUiState(
    val isLoading: Boolean = false,
    val addresses: List<AddressResponse> = emptyList(),
    val showForm: Boolean = false,
    val editingAddress: AddressResponse? = null,
    val isSaving: Boolean = false,
    val deletingId: Long? = null,
    val errorMessage: String? = null
)

class AddressesViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = getApplication<DopapcApplication>().container.apiService
    private val _uiState = MutableStateFlow(AddressesUiState())
    val uiState: StateFlow<AddressesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, addresses = emptyList(), errorMessage = null)
            }
            try {
                val addresses = apiService.getAddresses()
                _uiState.update { it.copy(isLoading = false, addresses = addresses) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.message(e, "Error al cargar direcciones")
                    )
                }
            }
        }
    }

    fun startCreate() {
        _uiState.update {
            it.copy(showForm = true, editingAddress = null, errorMessage = null)
        }
    }

    fun startEdit(address: AddressResponse) {
        _uiState.update {
            it.copy(showForm = true, editingAddress = address, errorMessage = null)
        }
    }

    fun cancelForm() {
        _uiState.update { it.copy(showForm = false, editingAddress = null) }
    }

    fun save(request: AddressRequest) {
        if (_uiState.value.isSaving) return
        val editing = _uiState.value.editingAddress
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                if (editing == null) {
                    apiService.createAddress(request)
                } else {
                    apiService.updateAddress(editing.id, request)
                }
                _uiState.update {
                    it.copy(isSaving = false, showForm = false, editingAddress = null)
                }
                load()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = ApiErrorParser.message(e, "Error al guardar la dirección")
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
                val response = apiService.deleteAddress(id)
                if (!response.isSuccessful) throw HttpException(response)
                _uiState.update { it.copy(deletingId = null) }
                load()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        deletingId = null,
                        errorMessage = ApiErrorParser.message(e, "Error al eliminar la dirección")
                    )
                }
            }
        }
    }
}
