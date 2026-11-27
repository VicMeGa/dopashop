package com.ejemplo.miapp.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.CartItemRequest
import com.ejemplo.miapp.data.model.CartItemResponse
import com.ejemplo.miapp.data.model.CartResponse
import com.ejemplo.miapp.data.model.MergeCartRequest
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.Session
import com.ejemplo.miapp.data.model.UpdateCartItemRequest
import com.ejemplo.miapp.data.network.ApiErrorParser
import com.ejemplo.miapp.data.network.StoreApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class CartUiState(
    val isLoading: Boolean = false,
    val items: List<CartItemResponse> = emptyList(),
    val errorMessage: String? = null
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
    val total: Double get() = items.sumOf { it.subtotal }
    val hasUnavailable: Boolean get() = items.any { !it.available }
}

class CartViewModel(
    private val apiService: StoreApiService,
    private val session: StateFlow<Session?>,
    isSessionReady: StateFlow<Boolean>
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private var guestItems: List<CartItemResponse> = emptyList()

    init {
        viewModelScope.launch {
            combine(isSessionReady, session) { ready, current -> ready to current }
                .distinctUntilChanged()
                .collect { (ready, current) ->
                    if (!ready) return@collect
                    if (current == null) {
                        _uiState.update {
                            it.copy(isLoading = false, items = guestItems, errorMessage = null)
                        }
                    } else {
                        syncForUser()
                    }
                }
        }
    }

    fun add(product: ProductResponse) {
        if (product.stock <= 0) return

        if (session.value == null) {
            val existing = guestItems.firstOrNull { it.product.id == product.id }
            val next = when {
                existing == null -> guestItems + toCartItem(product, 1)
                existing.quantity >= product.stock -> guestItems
                else -> guestItems.map {
                    if (it.product.id == product.id) toCartItem(it.product, it.quantity + 1) else it
                }
            }
            updateGuest(next)
            return
        }

        runApi("Error al agregar al carrito") {
            apiService.addCartItem(CartItemRequest(product.id, 1))
        }
    }

    fun increase(productId: Long) {
        if (session.value == null) {
            val existing = guestItems.firstOrNull { it.product.id == productId } ?: return
            if (existing.quantity >= existing.product.stock) return
            updateGuest(guestItems.map {
                if (it.product.id == productId) toCartItem(it.product, it.quantity + 1) else it
            })
            return
        }

        runApi("Error al actualizar el carrito") {
            apiService.addCartItem(CartItemRequest(productId, 1))
        }
    }

    fun decrease(productId: Long) {
        if (session.value == null) {
            val existing = guestItems.firstOrNull { it.product.id == productId } ?: return
            val next = if (existing.quantity <= 1) {
                guestItems.filterNot { it.product.id == productId }
            } else {
                guestItems.map {
                    if (it.product.id == productId) toCartItem(it.product, it.quantity - 1) else it
                }
            }
            updateGuest(next)
            return
        }

        val current = _uiState.value.items.firstOrNull { it.product.id == productId } ?: return
        viewModelScope.launch {
            try {
                if (current.quantity <= 1) {
                    val response = apiService.removeCartItem(productId)
                    if (!response.isSuccessful) throw HttpException(response)
                    _uiState.update { state ->
                        state.copy(items = state.items.filterNot { it.product.id == productId })
                    }
                } else {
                    val cart = apiService.updateCartItem(
                        productId,
                        UpdateCartItemRequest(current.quantity - 1)
                    )
                    applyCart(cart)
                }
            } catch (e: Exception) {
                handleError(e, "Error al actualizar el carrito")
            }
        }
    }

    fun remove(productId: Long) {
        if (session.value == null) {
            updateGuest(guestItems.filterNot { it.product.id == productId })
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.removeCartItem(productId)
                if (!response.isSuccessful) throw HttpException(response)
                _uiState.update { state ->
                    state.copy(items = state.items.filterNot { it.product.id == productId })
                }
            } catch (e: Exception) {
                handleError(e, "Error al quitar el producto")
            }
        }
    }

    fun clear() {
        if (session.value == null) {
            updateGuest(emptyList())
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.clearCart()
                if (!response.isSuccessful) throw HttpException(response)
                _uiState.update { it.copy(items = emptyList(), errorMessage = null) }
            } catch (e: Exception) {
                handleError(e, "Error al vaciar el carrito")
            }
        }
    }

    private suspend fun syncForUser() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val guest = guestItems
            val cart = if (guest.isEmpty()) {
                apiService.getCart()
            } else {
                val merged = apiService.mergeCart(
                    MergeCartRequest(guest.map { CartItemRequest(it.product.id, it.quantity) })
                )
                guestItems = emptyList()
                merged
            }
            applyCart(cart)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = ApiErrorParser.message(e, "Error al sincronizar el carrito")
                )
            }
        }
    }

    private fun runApi(fallback: String, call: suspend () -> CartResponse) {
        viewModelScope.launch {
            try {
                applyCart(call())
            } catch (e: Exception) {
                handleError(e, fallback)
            }
        }
    }

    private fun applyCart(cart: CartResponse) {
        _uiState.update {
            it.copy(isLoading = false, items = cart.items, errorMessage = null)
        }
    }

    private fun updateGuest(items: List<CartItemResponse>) {
        guestItems = items
        _uiState.update {
            it.copy(isLoading = false, items = items, errorMessage = null)
        }
    }

    private fun handleError(error: Exception, fallback: String) {
        if (error is CancellationException) throw error
        _uiState.update {
            it.copy(errorMessage = ApiErrorParser.message(error, fallback))
        }
    }

    private fun toCartItem(product: ProductResponse, quantity: Int): CartItemResponse {
        return CartItemResponse(
            product = product,
            quantity = quantity,
            subtotal = product.price * quantity,
            available = product.active && product.stock >= quantity
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DopapcApplication
                CartViewModel(
                    apiService = app.container.apiService,
                    session = app.container.authRepository.session,
                    isSessionReady = app.container.authRepository.isSessionReady
                )
            }
        }
    }
}
