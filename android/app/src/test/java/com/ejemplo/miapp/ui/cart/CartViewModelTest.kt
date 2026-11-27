package com.ejemplo.miapp.ui.cart

import com.ejemplo.miapp.data.model.CartItemResponse
import com.ejemplo.miapp.data.model.CartResponse
import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.Session
import com.ejemplo.miapp.data.model.UserSummary
import com.ejemplo.miapp.data.network.StoreApiService
import com.ejemplo.miapp.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val apiService = mockk<StoreApiService>()
    private val sessionFlow = MutableStateFlow<Session?>(null)
    private val readyFlow = MutableStateFlow(true)

    @Test
    fun `guest can add increase decrease and remove locally`() = runTest {
        val viewModel = viewModel()
        val product = product(stock = 5)

        viewModel.add(product)
        viewModel.add(product)
        assertEquals(2, viewModel.uiState.value.itemCount)
        assertEquals(20.0, viewModel.uiState.value.total, 0.001)

        viewModel.increase(product.id)
        assertEquals(3, viewModel.uiState.value.itemCount)

        viewModel.decrease(product.id)
        assertEquals(2, viewModel.uiState.value.itemCount)

        viewModel.remove(product.id)
        assertTrue(viewModel.uiState.value.items.isEmpty())
        coVerify(exactly = 0) { apiService.getCart() }
    }

    @Test
    fun `guest cannot exceed stock`() = runTest {
        val viewModel = viewModel()
        val product = product(stock = 2)

        repeat(5) { viewModel.add(product) }

        assertEquals(2, viewModel.uiState.value.itemCount)
        assertTrue(viewModel.uiState.value.items.single().available)
    }

    @Test
    fun `authenticated user loads cart from server`() = runTest {
        sessionFlow.value = session()
        val item = cartItem(quantity = 2)
        coEvery { apiService.getCart() } returns cartResponse(item)

        val viewModel = viewModel()

        assertEquals(2, viewModel.uiState.value.itemCount)
        assertEquals(20.0, viewModel.uiState.value.total, 0.001)
        coVerify { apiService.getCart() }
    }

    @Test
    fun `authenticated add calls server and applies response`() = runTest {
        sessionFlow.value = session()
        coEvery { apiService.getCart() } returns cartResponse()
        coEvery { apiService.addCartItem(any()) } returns cartResponse(cartItem(quantity = 1))

        val viewModel = viewModel()
        viewModel.add(product())

        assertEquals(1, viewModel.uiState.value.itemCount)
        coVerify { apiService.addCartItem(any()) }
    }

    @Test
    fun `guest cart is merged when session starts`() = runTest {
        val viewModel = viewModel()
        viewModel.add(product(stock = 5))
        assertEquals(1, viewModel.uiState.value.itemCount)

        coEvery { apiService.mergeCart(any()) } returns cartResponse(cartItem(quantity = 1))
        sessionFlow.value = session()

        coVerify { apiService.mergeCart(any()) }
        assertEquals(1, viewModel.uiState.value.itemCount)
    }

    @Test
    fun `unavailable items are flagged`() = runTest {
        sessionFlow.value = session()
        coEvery { apiService.getCart() } returns cartResponse(cartItem(quantity = 5, stock = 2))

        val viewModel = viewModel()

        assertTrue(viewModel.uiState.value.hasUnavailable)
    }

    private fun viewModel() = CartViewModel(
        apiService = apiService,
        session = sessionFlow,
        isSessionReady = readyFlow
    )

    private fun session() = Session(
        accessToken = "access",
        refreshToken = "refresh",
        user = UserSummary(1L, "user@test.com", "Usuario", "USER")
    )

    private fun product(stock: Int = 5) = ProductResponse(
        id = 10L,
        title = "Producto",
        price = 10.0,
        description = "Descripción",
        stock = stock,
        images = emptyList(),
        category = CategoryResponse(1L, "Categoría", null),
        storeId = 1L,
        storeName = "Tienda",
        active = true
    )

    private fun cartItem(quantity: Int, stock: Int = 5) = CartItemResponse(
        product = product(stock),
        quantity = quantity,
        subtotal = 10.0 * quantity,
        available = stock >= quantity
    )

    private fun cartResponse(vararg items: CartItemResponse) = CartResponse(
        id = 1L,
        items = items.toList(),
        itemCount = items.sumOf { it.quantity },
        total = items.sumOf { it.subtotal }
    )
}
