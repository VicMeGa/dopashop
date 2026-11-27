package com.ejemplo.miapp.ui.mystore

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ejemplo.miapp.data.model.CategoryResponse
import com.ejemplo.miapp.data.model.ProductResponse
import com.ejemplo.miapp.data.model.ProductUpsertRequest
import com.ejemplo.miapp.ui.common.ErrorBanner
import com.ejemplo.miapp.ui.common.FormTextField
import com.ejemplo.miapp.util.ShopValidators

private val TEXT_COLOR = Color(0xFF08060d)
private val MUTED_COLOR = Color(0xFF6b6375)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductForm(
    initialValues: ProductResponse?,
    categories: List<CategoryResponse>,
    isLoadingCategories: Boolean,
    categoriesError: String?,
    isSaving: Boolean,
    isUploadingImage: Boolean,
    errorMessage: String?,
    imageUploadError: String?,
    onRetryCategories: () -> Unit,
    onClearError: () -> Unit,
    onClearImageError: () -> Unit,
    onUploadImage: (Uri, (String) -> Unit) -> Unit,
    onSubmit: (ProductUpsertRequest) -> Unit,
    onCancel: () -> Unit
) {
    val formKey = initialValues?.id

    var title by rememberSaveable(formKey) {
        mutableStateOf(initialValues?.title ?: "")
    }
    var description by rememberSaveable(formKey) {
        mutableStateOf(initialValues?.description ?: "")
    }
    var price by rememberSaveable(formKey) {
        mutableStateOf(initialValues?.price?.toString() ?: "")
    }
    var stock by rememberSaveable(formKey) {
        mutableStateOf(initialValues?.stock?.toString() ?: "")
    }
    var selectedCategoryId by rememberSaveable(formKey) {
        mutableStateOf(initialValues?.category?.id?.toString() ?: "")
    }
    var expanded by remember { mutableStateOf(false) }
    var fieldErrors by remember(formKey) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var selectedImageUri by remember(formKey) {
        mutableStateOf<Uri?>(null)
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            onClearImageError()
        }
    }

    val selectedCategoryName = categories
        .firstOrNull { it.id.toString() == selectedCategoryId }
        ?.name
        .orEmpty()

    fun clearFieldError(field: String) {
        if (fieldErrors.containsKey(field)) fieldErrors = fieldErrors - field
    }

    fun validate(): Boolean {
        val errors = linkedMapOf<String, String>()
        ShopValidators.productTitle(title)?.let { errors["title"] = it }
        ShopValidators.productDescription(description)?.let { errors["description"] = it }
        ShopValidators.productPrice(price)?.let { errors["price"] = it }
        ShopValidators.productStock(stock)?.let { errors["stock"] = it }
        ShopValidators.productCategory(selectedCategoryId.toLongOrNull())
            ?.let { errors["category"] = it }
        fieldErrors = errors
        return errors.isEmpty()
    }

    fun submit() {
        if (isSaving || isUploadingImage || !validate()) return
        val parsedPrice = price.trim().toDoubleOrNull() ?: return
        val parsedStock = stock.trim().toDoubleOrNull()?.toInt() ?: return
        val categoryId = selectedCategoryId.toLongOrNull() ?: return

        val request = ProductUpsertRequest(
            title = title.trim(),
            description = description.trim(),
            price = parsedPrice,
            stock = parsedStock,
            categoryId = categoryId,
            imageUrls = initialValues?.images ?: emptyList()
        )

        val pickedImage = selectedImageUri
        if (pickedImage == null) {
            onSubmit(request)
        } else {
            onUploadImage(pickedImage) { url ->
                onSubmit(request.copy(imageUrls = listOf(url)))
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFe5e4e7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (initialValues == null) "Nuevo producto" else "Editar producto",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TEXT_COLOR
            )
            Spacer(modifier = Modifier.height(12.dp))

            errorMessage?.let { message ->
                ErrorBanner(message = message)
                Spacer(modifier = Modifier.height(12.dp))
            }

            imageUploadError?.let { message ->
                ErrorBanner(message = message)
                Spacer(modifier = Modifier.height(12.dp))
            }

            FormTextField(
                value = title,
                onValueChange = {
                    title = it
                    clearFieldError("title")
                    onClearError()
                },
                label = "Título",
                error = fieldErrors["title"],
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    placeholder = { Text("Selecciona una categoría") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    isError = fieldErrors["category"] != null,
                    supportingText = fieldErrors["category"]?.let { message ->
                        { Text(message) }
                    },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.id.toString()
                                expanded = false
                                clearFieldError("category")
                                onClearError()
                            }
                        )
                    }
                }
            }

            if (isLoadingCategories) {
                Text(
                    text = "Cargando categorías...",
                    fontSize = 12.sp,
                    color = MUTED_COLOR
                )
            }

            categoriesError?.let { message ->
                TextButton(onClick = onRetryCategories) {
                    Text(message)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        clearFieldError("price")
                        onClearError()
                    },
                    label = "Precio",
                    error = fieldErrors["price"],
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Decimal
                )
                FormTextField(
                    value = stock,
                    onValueChange = {
                        stock = it
                        clearFieldError("stock")
                        onClearError()
                    },
                    label = "Stock",
                    error = fieldErrors["stock"],
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    clearFieldError("description")
                    onClearError()
                },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6,
                isError = fieldErrors["description"] != null,
                supportingText = fieldErrors["description"]?.let { message ->
                    { Text(message) }
                },
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val previewModel: Any? = selectedImageUri
                ?: initialValues?.images?.firstOrNull()

            Text(
                text = "Imagen del producto",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TEXT_COLOR
            )

            if (previewModel != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color(0xFFf8f8f8), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Vista previa del producto",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }
                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nueva imagen lista para subir",
                        fontSize = 12.sp,
                        color = MUTED_COLOR
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                enabled = !isSaving && !isUploadingImage,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    if (previewModel == null) "Seleccionar imagen" else "Cambiar imagen"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { submit() },
                    enabled = !isSaving && !isUploadingImage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            isUploadingImage -> "Subiendo imagen..."
                            isSaving -> "Guardando..."
                            initialValues == null -> "Crear producto"
                            else -> "Guardar cambios"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSaving && !isUploadingImage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
