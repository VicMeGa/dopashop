package com.ejemplo.miapp.ui.checkout

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.AddressResponse
import com.ejemplo.miapp.data.model.CreateOrderRequest
import com.ejemplo.miapp.data.model.OrderItemRequest
import com.ejemplo.miapp.data.model.PaymentMethodResponse
import com.ejemplo.miapp.data.network.ApiErrorParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class CheckoutUiState(
    val isLoading: Boolean = true,
    val addresses: List<AddressResponse> = emptyList(),
    val paymentMethods: List<PaymentMethodResponse> = emptyList(),
    val selectedAddressId: Long? = null,
    val selectedPaymentMethodId: Long? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val paymentRejected: Boolean = false,
    val pendingOrderId: Long? = null,
    val pendingAddress: AddressResponse? = null,
    val approvedOrderId: Long? = null
)

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = getApplication<DopapcApplication>().container.apiService
    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load() {
        if (_uiState.value.isSubmitting) return
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val addressesRequest = async { apiService.getAddresses() }
                val methodsRequest = async { apiService.getPaymentMethods() }
                val addresses = addressesRequest.await()
                val methods = methodsRequest.await()

                _uiState.update { state ->
                    val addressId = state.selectedAddressId
                        ?.takeIf { id -> addresses.any { it.id == id } }
                        ?: addresses.firstOrNull { it.isDefault }?.id
                        ?: addresses.firstOrNull()?.id
                    val methodId = state.selectedPaymentMethodId
                        ?.takeIf { id -> methods.any { it.id == id } }
                        ?: methods.firstOrNull()?.id
                    val selectionChanged = addressId != state.selectedAddressId ||
                        methodId != state.selectedPaymentMethodId
                    val selectedAddress = addresses
                        .firstOrNull { it.id == addressId }
                        ?.copy(isDefault = false)
                    val addressChanged =
                        selectedAddress != state.pendingAddress?.copy(isDefault = false)
                    val keepPending = state.pendingOrderId != null &&
                        !selectionChanged &&
                        !addressChanged

                    state.copy(
                        isLoading = false,
                        addresses = addresses,
                        paymentMethods = methods,
                        selectedAddressId = addressId,
                        selectedPaymentMethodId = methodId,
                        pendingOrderId = if (keepPending) state.pendingOrderId else null,
                        pendingAddress = if (keepPending) state.pendingAddress else null,
                        paymentRejected = if (keepPending) state.paymentRejected else false
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ApiErrorParser.message(
                            e,
                            "Error al cargar datos de envío y pago"
                        )
                    )
                }
            }
        }
    }

    fun selectAddress(id: Long) {
        _uiState.update { state ->
            if (state.selectedAddressId == id) return@update state
            state.copy(
                selectedAddressId = id,
                pendingOrderId = null,
                pendingAddress = null,
                paymentRejected = false,
                errorMessage = null
            )
        }
    }

    fun selectPaymentMethod(id: Long) {
        _uiState.update { state ->
            if (state.selectedPaymentMethodId == id) return@update state
            state.copy(
                selectedPaymentMethodId = id,
                pendingOrderId = null,
                pendingAddress = null,
                paymentRejected = false,
                errorMessage = null
            )
        }
    }

    fun confirm(items: List<OrderItemRequest>) {
        val current = _uiState.value
        if (current.isSubmitting) return

        val addressId = current.selectedAddressId
        val paymentMethodId = current.selectedPaymentMethodId
        if (addressId == null || paymentMethodId == null || items.isEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Seleccioná una dirección, un método de pago y agregá productos."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    paymentRejected = false,
                    approvedOrderId = null,
                    pendingOrderId = null,
                    pendingAddress = null
                )
            }
            var createdOrderId: Long? = null
            try {
                val order = apiService.createOrder(
                    CreateOrderRequest(
                        addressId = addressId,
                        paymentMethodId = paymentMethodId,
                        items = items
                    )
                )
                createdOrderId = order.id
                _uiState.update {
                    it.copy(
                        pendingOrderId = order.id,
                        pendingAddress = current.addresses.firstOrNull { address ->
                            address.id == addressId
                        }
                    )
                }
                finishPayment(order.id)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = ApiErrorParser.message(e, "Error al confirmar la compra"),
                        pendingOrderId = createdOrderId
                    )
                }
            }
        }
    }

    fun retryPayment() {
        val current = _uiState.value
        val orderId = current.pendingOrderId ?: return
        if (current.isSubmitting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    paymentRejected = false,
                    approvedOrderId = null
                )
            }
            finishPayment(orderId)
        }
    }

    fun consumeApprovedOrder() {
        _uiState.update { it.copy(approvedOrderId = null) }
    }

    private suspend fun finishPayment(orderId: Long) {
        try {
            val payment = apiService.attemptPayment(orderId)
            if (payment.status == "APPROVED") {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        paymentRejected = false,
                        pendingOrderId = null,
                        pendingAddress = null,
                        approvedOrderId = orderId
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isSubmitting = false, paymentRejected = true)
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e

            if (e is HttpException && e.code() == 409) {
                val order = runCatching { apiService.getOrderById(orderId) }.getOrNull()
                if (order?.status == "CONFIRMED") {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            paymentRejected = false,
                            pendingOrderId = null,
                            pendingAddress = null,
                            approvedOrderId = orderId,
                            errorMessage = null
                        )
                    }
                    return
                }
            }

            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    errorMessage = ApiErrorParser.message(e, "Error al procesar el pago"),
                    pendingOrderId = orderId
                )
            }
        }
    }
}
