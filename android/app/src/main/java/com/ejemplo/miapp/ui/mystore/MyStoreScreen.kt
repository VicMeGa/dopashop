package com.ejemplo.miapp.ui.mystore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.ui.common.ErrorBanner
import com.ejemplo.miapp.ui.common.FormTextField
import com.ejemplo.miapp.ui.common.formatPrice

private val TEXT_COLOR = Color(0xFF08060d)
private val MUTED_COLOR = Color(0xFF6b6375)
private val BORDER_COLOR = Color(0xFFe5e4e7)

@Composable
fun MyStoreScreen(viewModel: MyStoreViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var productToDeactivate by remember { mutableStateOf<ProductResponse?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563eb))
            }
        }

        uiState.storeError != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ErrorBanner(message = uiState.storeError.orEmpty())
                    Button(
                        onClick = viewModel::load,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }

        uiState.store == null -> {
            CreateStoreContent(
                isCreating = uiState.isCreatingStore,
                errorMessage = uiState.storeFormError,
                onClearError = viewModel::clearStoreFormError,
                onCreate = viewModel::createStore
            )
        }

        else -> {
            val store = uiState.store ?: return
            val listState = rememberLazyListState()
            LaunchedEffect(uiState.showProductForm, uiState.editingProduct?.id) {
                if (uiState.showProductForm) listState.animateScrollToItem(1)
            }
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = store.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_COLOR
                    )
                    store.description?.takeIf { it.isNotBlank() }?.let { description ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = MUTED_COLOR
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!uiState.showProductForm) {
                        Button(
                            onClick = viewModel::startCreateProduct,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Agregar producto")
                        }
                    }
                }

                if (uiState.showProductForm) {
                    item {
                        ProductForm(
                            initialValues = uiState.editingProduct,
                            categories = uiState.categories,
                            isLoadingCategories = uiState.isLoadingCategories,
                            categoriesError = uiState.categoriesError,
                            isSaving = uiState.isSavingProduct,
                            isUploadingImage = uiState.isUploadingImage,
                            errorMessage = uiState.productFormError,
                            imageUploadError = uiState.imageUploadError,
                            onRetryCategories = {
                                viewModel.loadCategories(force = true)
                            },
                            onClearError = viewModel::clearProductFormError,
                            onClearImageError = viewModel::clearImageUploadError,
                            onUploadImage = viewModel::uploadProductImage,
                            onSubmit = viewModel::saveProduct,
                            onCancel = viewModel::cancelProductForm
                        )
                    }
                }

                item {
                    Text(
                        text = "Productos (${uiState.products.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_COLOR
                    )
                }

                uiState.productStatusError?.let { message ->
                    item {
                        Column {
                            ErrorBanner(message = message)
                            TextButton(onClick = viewModel::clearProductStatusError) {
                                Text("Cerrar")
                            }
                        }
                    }
                }

                if (uiState.isLoadingProducts) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2563eb))
                        }
                    }
                }

                uiState.productsError?.let { message ->
                    item {
                        Column {
                            ErrorBanner(message = message)
                            TextButton(onClick = viewModel::loadProducts) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                if (!uiState.showProductForm &&
                    !uiState.isLoadingProducts &&
                    uiState.productsError == null &&
                    uiState.products.isEmpty()
                ) {
                    item {
                        Text(
                            text = "Aún no tenés productos publicados.",
                            fontSize = 14.sp,
                            color = MUTED_COLOR
                        )
                    }
                }

                items(uiState.products, key = { it.id }) { product ->
                    MyStoreProductCard(
                        product = product,
                        isUpdatingStatus = uiState.updatingProductId == product.id,
                        statusActionsEnabled = uiState.updatingProductId == null,
                        onEdit = { viewModel.startEditProduct(product) },
                        onDeactivate = { productToDeactivate = product },
                        onReactivate = { viewModel.reactivateProduct(product) }
                    )
                }
            }
        }
    }

    productToDeactivate?.let { product ->
        AlertDialog(
            onDismissRequest = {
                if (uiState.updatingProductId != product.id) {
                    productToDeactivate = null
                }
            },
            title = { Text("Desactivar producto") },
            text = {
                Text(
                    "¿Desactivar \"${product.title}\"? Dejará de verse en el catálogo."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDeactivate = null
                        viewModel.deactivateProduct(product)
                    },
                    enabled = uiState.updatingProductId == null
                ) {
                    Text(
                        text = "Desactivar",
                        color = Color(0xFFdc2626),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { productToDeactivate = null },
                    enabled = uiState.updatingProductId != product.id
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CreateStoreContent(
    isCreating: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Crea tu tienda",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TEXT_COLOR
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Contanos qué vendés para empezar a publicar productos.",
            fontSize = 14.sp,
            color = MUTED_COLOR
        )
        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let { message ->
            ErrorBanner(message = message)
            Spacer(modifier = Modifier.height(12.dp))
        }

        FormTextField(
            value = name,
            onValueChange = {
                name = it
                onClearError()
            },
            label = "Nombre de la tienda",
            error = null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                onClearError()
            },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isCreating) onCreate(name, description)
            },
            enabled = !isCreating,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isCreating) "Creando..." else "Crear tienda",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MyStoreProductCard(
    product: ProductResponse,
    isUpdatingStatus: Boolean,
    statusActionsEnabled: Boolean,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
    onReactivate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BORDER_COLOR),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFf8f8f8), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = product.images.firstOrNull()
                    if (imageUrl.isNullOrBlank()) {
                        Text(
                            text = "Sin imagen",
                            fontSize = 10.sp,
                            color = MUTED_COLOR
                        )
                    } else {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = product.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TEXT_COLOR,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPrice(product.price),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TEXT_COLOR
                    )
                    Text(
                        text = "Stock: ${product.stock}",
                        fontSize = 12.sp,
                        color = MUTED_COLOR
                    )
                }

                ProductStatusBadge(active = product.active)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !isUpdatingStatus,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Editar")
                }

                if (product.active) {
                    Button(
                        onClick = onDeactivate,
                        enabled = statusActionsEnabled,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFdc2626)
                        )
                    ) {
                        Text(
                            text = if (isUpdatingStatus) "Desactivando..." else "Desactivar"
                        )
                    }
                } else {
                    Button(
                        onClick = onReactivate,
                        enabled = statusActionsEnabled,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isUpdatingStatus) "Reactivando..." else "Reactivar"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductStatusBadge(active: Boolean) {
    Surface(
        color = if (active) Color(0xFFdcfce7) else Color(0xFFf3f4f6),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = if (active) "Activo" else "Desactivado",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (active) Color(0xFF15803d) else MUTED_COLOR,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
