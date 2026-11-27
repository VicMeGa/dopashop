package com.ejemplo.miapp.ui.storeprofile

import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.StoreResponse
import com.ejemplo.miapp.data.network.StoreApiService
import com.ejemplo.miapp.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class StoreProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val apiService = mockk<StoreApiService>()

    @Test
    fun `load success keeps store and loads products`() = runTest {
        val store = store()
        val products = listOf(product())
        coEvery { apiService.getStoreById(1L) } returns Response.success(store)
        coEvery { apiService.getStoreProducts(1L) } returns products

        val viewModel = StoreProfileViewModel(apiService)
        viewModel.load(1L)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.notFound)
        assertNull(state.errorMessage)
        assertEquals(store, state.store)
        assertEquals(products, state.products)
        assertFalse(state.isLoadingProducts)
    }

    @Test
    fun `load 404 marks store as not found`() = runTest {
        coEvery { apiService.getStoreById(99L) } returns notFound()

        val viewModel = StoreProfileViewModel(apiService)
        viewModel.load(99L)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.notFound)
        assertNull(state.store)
        assertNull(state.errorMessage)
    }

    @Test
    fun `load network error exposes message`() = runTest {
        coEvery { apiService.getStoreById(1L) } throws IOException("offline")

        val viewModel = StoreProfileViewModel(apiService)
        viewModel.load(1L)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.notFound)
        assertNull(state.store)
        assertEquals("No se pudo conectar con el servidor", state.errorMessage)
    }

    @Test
    fun `products error keeps store loaded`() = runTest {
        val store = store()
        coEvery { apiService.getStoreById(1L) } returns Response.success(store)
        coEvery { apiService.getStoreProducts(1L) } throws IOException("offline")

        val viewModel = StoreProfileViewModel(apiService)
        viewModel.load(1L)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(store, state.store)
        assertTrue(state.products.isEmpty())
        assertFalse(state.isLoadingProducts)
        assertEquals(
            "No se pudo conectar con el servidor",
            state.productsError
        )
    }

    private fun store() = StoreResponse(
        id = 1L,
        name = "Tienda",
        description = "Descripción",
        ownerName = "Vendedor",
        createdAt = null
    )

    private fun product() = ProductResponse(
        id = 10L,
        title = "Producto",
        price = 10.0,
        description = "Descripción",
        stock = 5,
        images = emptyList(),
        category = CategoryResponse(1L, "Categoría", null),
        storeId = 1L,
        storeName = "Tienda",
        active = true
    )

    private fun <T> notFound(): Response<T> = Response.error(
        404,
        "".toResponseBody("text/plain".toMediaType())
    )
}
