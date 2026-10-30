package com.ejemplo.miapp.ui.cart

import androidx.lifecycle.ViewModel
import com.ejemplo.miapp.data.model.ProductResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartItem(
    val product: ProductResponse,
    val quantity: Int
)

class CartViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun add(product: ProductResponse) {
        if (product.stock <= 0) return
        _items.update { current ->
            val existing = current.firstOrNull { it.product.id == product.id }
            if (existing == null) {
                current + CartItem(product, 1)
            } else {
                current.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            }
        }
    }

    fun increase(productId: Long) {
        _items.update { current ->
            current.map {
                if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it
            }
        }
    }

    fun decrease(productId: Long) {
        _items.update { current ->
            current.mapNotNull {
                when {
                    it.product.id != productId -> it
                    it.quantity <= 1 -> null
                    else -> it.copy(quantity = it.quantity - 1)
                }
            }
        }
    }

    fun remove(productId: Long) {
        _items.update { current -> current.filterNot { it.product.id == productId } }
    }

    fun clear() {
        _items.value = emptyList()
    }
}
