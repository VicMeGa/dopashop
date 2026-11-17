package com.ejemplo.miapp.ui.mystore

import android.net.Uri
import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.ImageUploadResponse
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.ProductUpsertRequest
import com.ejemplo.miapp.data.model.StoreResponse
import com.ejemplo.miapp.data.network.StoreApiService
import com.ejemplo.miapp.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MyStoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val apiService = mockk<StoreApiService>()

    @Test
    fun `load 404 means there is no store yet`() = runTest {
        coEvery { apiService.getMyStore() } returns notFound()

        val viewModel = viewModel()
        viewModel.load()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.storeLoaded)
        assertNull(state.store)
        assertNull(state.storeError)
    }

    @Test
    fun `load success keeps store and products`() = runTest {
        val store = store()
        val products = listOf(product())
        coEvery { apiService.getMyStore() } returns Response.success(store)
        coEvery { apiService.getMyProducts() } returns products

        val viewModel = viewModel()
        viewModel.load()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(store, state.store)
        assertEquals(products, state.products)
        assertFalse(state.isLoadingProducts)
    }

    @Test
    fun `create store sets store and reloads products`() = runTest {
        val store = store()
        coEvery { apiService.createStore(any()) } returns store
        coEvery { apiService.getMyProducts() } returns emptyList()

        val viewModel = viewModel()
        viewModel.createStore("Tienda", "Descripción")

        val state = viewModel.uiState.value
        assertFalse(state.isCreatingStore)
        assertEquals(store, state.store)
        assertNull(state.storeFormError)
    }

    @Test
    fun `save product creates product and closes form`() = runTest {
        coEvery { apiService.getCategories() } returns emptyList()
        coEvery { apiService.createProduct(any()) } returns product()
        val request = productRequest()

        val viewModel = viewModel()
        viewModel.startCreateProduct()
        viewModel.saveProduct(request)

        val state = viewModel.uiState.value
        assertFalse(state.showProductForm)
        assertFalse(state.isSavingProduct)
        assertNull(state.productFormError)
        coVerify(exactly = 1) { apiService.createProduct(request) }
    }

    @Test
    fun `save product updates edited product`() = runTest {
        val product = product()
        coEvery { apiService.getCategories() } returns emptyList()
        coEvery { apiService.updateProduct(product.id, any()) } returns product
        val request = productRequest()

        val viewModel = viewModel()
        viewModel.startEditProduct(product)
        viewModel.saveProduct(request)

        val state = viewModel.uiState.value
        assertFalse(state.showProductForm)
        assertNull(state.editingProduct)
        assertNull(state.productFormError)
        coVerify(exactly = 1) { apiService.updateProduct(product.id, request) }
    }

    @Test
    fun `save product with blank title shows validation error`() = runTest {
        val viewModel = viewModel()
        viewModel.saveProduct(productRequest().copy(title = " "))

        assertEquals(
            "El título es obligatorio",
            viewModel.uiState.value.productFormError
        )
        coVerify(exactly = 0) { apiService.createProduct(any()) }
    }

    @Test
    fun `deactivate product treats 404 as success`() = runTest {
        val product = product()
        coEvery { apiService.deleteProduct(product.id) } returns notFound()

        val viewModel = viewModel()
        viewModel.deactivateProduct(product)

        val state = viewModel.uiState.value
        assertNull(state.updatingProductId)
        assertNull(state.productStatusError)
    }

    @Test
    fun `reactivate product treats 404 as success`() = runTest {
        val product = product()
        coEvery { apiService.reactivateProduct(product.id) } returns notFound()

        val viewModel = viewModel()
        viewModel.reactivateProduct(product)

        val state = viewModel.uiState.value
        assertNull(state.updatingProductId)
        assertNull(state.productStatusError)
    }

    @Test
    fun `upload image returns uploaded url`() = runTest {
        val url = "https://example.com/image.jpg"
        coEvery { apiService.uploadImage(any()) } returns ImageUploadResponse(url)
        val part = MultipartBody.Part.createFormData(
            "file",
            "image.jpg",
            "data".toRequestBody("image/jpeg".toMediaType())
        )
        var uploadedUrl: String? = null

        val viewModel = viewModel(imagePartFactory = { part })
        viewModel.uploadProductImage(mockk<Uri>()) { uploadedUrl = it }

        val state = viewModel.uiState.value
        assertEquals(url, uploadedUrl)
        assertFalse(state.isUploadingImage)
        assertNull(state.imageUploadError)
    }

    @Test
    fun `upload image local failure shows message`() = runTest {
        val viewModel = viewModel(
            imagePartFactory = {
                throw IllegalArgumentException("El archivo debe ser una imagen")
            }
        )
        viewModel.uploadProductImage(mockk<Uri>()) {}

        val state = viewModel.uiState.value
        assertFalse(state.isUploadingImage)
        assertEquals("El archivo debe ser una imagen", state.imageUploadError)
    }

    private fun viewModel(
        imagePartFactory: (Uri) -> MultipartBody.Part = {
            error("La subida de imágenes no está configurada en este test")
        }
    ) = MyStoreViewModel(
        apiService = apiService,
        imagePartFactory = imagePartFactory
    )

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

    private fun productRequest() = ProductUpsertRequest(
        title = "Producto",
        description = "Descripción",
        price = 10.0,
        stock = 5,
        categoryId = 1L,
        imageUrls = emptyList()
    )

    private fun <T> notFound(): Response<T> = Response.error(
        404,
        "".toResponseBody("text/plain".toMediaType())
    )
}
