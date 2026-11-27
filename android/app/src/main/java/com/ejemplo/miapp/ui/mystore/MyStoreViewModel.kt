package com.ejemplo.miapp.ui.mystore

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ejemplo.miapp.DopapcApplication
import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.ProductUpsertRequest
import com.ejemplo.miapp.data.model.StoreRequest
import com.ejemplo.miapp.data.model.StoreResponse
import com.ejemplo.miapp.data.network.ApiErrorParser
import com.ejemplo.miapp.data.network.StoreApiService
import com.ejemplo.miapp.util.ImagePartFactory
import com.ejemplo.miapp.util.ShopValidators
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import java.math.BigDecimal

data class MyStoreUiState(
    val isLoading: Boolean = true,
    val store: StoreResponse? = null,
    val storeLoaded: Boolean = false,
    val storeError: String? = null,
    val isCreatingStore: Boolean = false,
    val storeFormError: String? = null,
    val products: List<ProductResponse> = emptyList(),
    val isLoadingProducts: Boolean = false,
    val productsError: String? = null,
    val categories: List<CategoryResponse> = emptyList(),
    val isLoadingCategories: Boolean = false,
    val categoriesError: String? = null,
    val showProductForm: Boolean = false,
    val editingProduct: ProductResponse? = null,
    val isSavingProduct: Boolean = false,
    val productFormError: String? = null,
    val updatingProductId: Long? = null,
    val productStatusError: String? = null,
    val isUploadingImage: Boolean = false,
    val imageUploadError: String? = null
)

class MyStoreViewModel(
    private val apiService: StoreApiService,
    private val imagePartFactory: (Uri) -> MultipartBody.Part
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyStoreUiState())
    val uiState: StateFlow<MyStoreUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var productsJob: Job? = null
    private var categoriesJob: Job? = null

    fun load() {
        loadJob?.cancel()
        productsJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    store = null,
                    storeLoaded = false,
                    storeError = null,
                    isCreatingStore = false,
                    storeFormError = null,
                    products = emptyList(),
                    isLoadingProducts = false,
                    productsError = null,
                    showProductForm = false,
                    editingProduct = null,
                    isSavingProduct = false,
                    productFormError = null,
                    updatingProductId = null,
                    productStatusError = null,
                    isUploadingImage = false,
                    imageUploadError = null
                )
            }
            try {
                val response = apiService.getMyStore()
                if (response.code() == 404) {
                    _uiState.update {
                        it.copy(isLoading = false, storeLoaded = true, store = null)
                    }
                    return@launch
                }
                if (!response.isSuccessful) throw HttpException(response)
                val store = response.body()
                if (store == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            storeLoaded = true,
                            storeError = "Error al cargar tu tienda"
                        )
                    }
                    return@launch
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        storeLoaded = true,
                        store = store,
                        storeError = null
                    )
                }
                loadProducts()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        storeLoaded = true,
                        store = null,
                        storeError = ApiErrorParser.message(e, "Error al cargar tu tienda")
                    )
                }
            }
        }
    }

    fun clearStoreFormError() {
        if (_uiState.value.storeFormError != null) {
            _uiState.update { it.copy(storeFormError = null) }
        }
    }

    fun createStore(name: String, description: String) {
        if (_uiState.value.isCreatingStore) return
        val validationError = ShopValidators.storeName(name)
            ?: ShopValidators.storeDescription(description)
        if (validationError != null) {
            _uiState.update { it.copy(storeFormError = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isCreatingStore = true, storeFormError = null)
            }
            try {
                val store = apiService.createStore(
                    StoreRequest(
                        name = name.trim(),
                        description = description.trim()
                    )
                )
                _uiState.update {
                    it.copy(
                        isCreatingStore = false,
                        store = store,
                        storeLoaded = true,
                        storeError = null,
                        storeFormError = null
                    )
                }
                loadProducts()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isCreatingStore = false,
                        storeFormError = ApiErrorParser.message(
                            e,
                            "Error al crear la tienda"
                        )
                    )
                }
            }
        }
    }

    fun startCreateProduct() {
        _uiState.update {
            it.copy(
                showProductForm = true,
                editingProduct = null,
                productFormError = null
            )
        }
        loadCategories()
    }

    fun startEditProduct(product: ProductResponse) {
        _uiState.update {
            it.copy(
                showProductForm = true,
                editingProduct = product,
                productFormError = null
            )
        }
        loadCategories()
    }

    fun cancelProductForm() {
        if (_uiState.value.isSavingProduct) return
        _uiState.update {
            it.copy(
                showProductForm = false,
                editingProduct = null,
                productFormError = null
            )
        }
    }

    fun clearProductFormError() {
        if (_uiState.value.productFormError != null) {
            _uiState.update { it.copy(productFormError = null) }
        }
    }

    fun clearProductStatusError() {
        if (_uiState.value.productStatusError != null) {
            _uiState.update { it.copy(productStatusError = null) }
        }
    }

    fun clearImageUploadError() {
        if (_uiState.value.imageUploadError != null) {
            _uiState.update { it.copy(imageUploadError = null) }
        }
    }

    fun uploadProductImage(uri: Uri, onUploaded: (String) -> Unit) {
        if (_uiState.value.isUploadingImage) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isUploadingImage = true, imageUploadError = null)
            }
            try {
                val part = imagePartFactory(uri)
                val response = apiService.uploadImage(part)
                _uiState.update { it.copy(isUploadingImage = false) }
                onUploaded(response.url)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val message = when (e) {
                    is HttpException -> ApiErrorParser.message(
                        e,
                        "Error al subir la imagen"
                    )
                    is IOException -> "No se pudo conectar con el servidor"
                    else -> e.message ?: "Error al subir la imagen"
                }
                _uiState.update {
                    it.copy(
                        isUploadingImage = false,
                        imageUploadError = message
                    )
                }
            }
        }
    }

    fun deactivateProduct(product: ProductResponse) {
        updateProductStatus(product, activate = false)
    }

    fun reactivateProduct(product: ProductResponse) {
        updateProductStatus(product, activate = true)
    }

    private fun updateProductStatus(product: ProductResponse, activate: Boolean) {
        if (_uiState.value.updatingProductId != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    updatingProductId = product.id,
                    productStatusError = null
                )
            }
            try {
                val response = if (activate) {
                    apiService.reactivateProduct(product.id)
                } else {
                    apiService.deleteProduct(product.id)
                }
                if (!response.isSuccessful && response.code() != 404) {
                    throw HttpException(response)
                }
                _uiState.update { it.copy(updatingProductId = null) }
                loadProducts()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        updatingProductId = null,
                        productStatusError = ApiErrorParser.message(
                            e,
                            if (activate) {
                                "Error al reactivar el producto"
                            } else {
                                "Error al desactivar el producto"
                            }
                        )
                    )
                }
            }
        }
    }

    fun loadCategories(force: Boolean = false) {
        if (!force &&
            (_uiState.value.categories.isNotEmpty() || categoriesJob?.isActive == true)
        ) {
            return
        }
        categoriesJob?.cancel()
        categoriesJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingCategories = true, categoriesError = null)
            }
            try {
                val categories = apiService.getCategories()
                _uiState.update {
                    it.copy(isLoadingCategories = false, categories = categories)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoadingCategories = false,
                        categoriesError = ApiErrorParser.message(
                            e,
                            "No se pudieron cargar las categorías"
                        )
                    )
                }
            }
        }
    }

    fun saveProduct(request: ProductUpsertRequest) {
        if (_uiState.value.isSavingProduct) return
        val validationError = validateProduct(request)
        if (validationError != null) {
            _uiState.update { it.copy(productFormError = validationError) }
            return
        }

        val editing = _uiState.value.editingProduct
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSavingProduct = true, productFormError = null)
            }
            try {
                if (editing == null) {
                    apiService.createProduct(request)
                } else {
                    apiService.updateProduct(editing.id, request)
                }
                _uiState.update {
                    it.copy(
                        isSavingProduct = false,
                        showProductForm = false,
                        editingProduct = null,
                        productFormError = null
                    )
                }
                loadProducts()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isSavingProduct = false,
                        productFormError = ApiErrorParser.message(
                            e,
                            "Error al guardar el producto"
                        )
                    )
                }
            }
        }
    }

    private fun validateProduct(request: ProductUpsertRequest): String? {
        ShopValidators.productTitle(request.title)?.let { return it }
        ShopValidators.productDescription(request.description ?: "")?.let { return it }
        if (!request.price.isFinite() || request.price <= 0.0) {
            return "El precio debe ser un número mayor a 0"
        }
        if (BigDecimal.valueOf(request.price).stripTrailingZeros().scale() > 2) {
            return "El precio no puede tener más de 2 decimales"
        }
        if (request.stock < 0) {
            return "El stock debe ser un número entero mayor o igual a 0"
        }
        if (request.stock > 1_000_000) {
            return "El stock no puede superar 1.000.000"
        }
        if (request.categoryId <= 0L) {
            return "Selecciona una categoría"
        }
        return null
    }

    fun loadProducts() {
        if (_uiState.value.store == null) return
        productsJob?.cancel()
        productsJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingProducts = true, productsError = null)
            }
            try {
                val products = apiService.getMyProducts()
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
                            "Error al cargar tus productos"
                        )
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DopapcApplication
                MyStoreViewModel(
                    apiService = app.container.apiService,
                    imagePartFactory = { uri ->
                        ImagePartFactory.fromUri(app.contentResolver, uri)
                    }
                )
            }
        }
    }
}
