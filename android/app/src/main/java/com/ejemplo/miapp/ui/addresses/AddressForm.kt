package com.ejemplo.miapp.ui.addresses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejemplo.miapp.data.model.AddressRequest
import com.ejemplo.miapp.data.model.AddressResponse
import com.ejemplo.miapp.ui.common.FormTextField
import com.ejemplo.miapp.util.ShopValidators

@Composable
fun AddressForm(
    initialValues: AddressResponse?,
    isSaving: Boolean,
    onSave: (AddressRequest) -> Unit,
    onCancel: () -> Unit
) {
    var street by rememberSaveable(initialValues?.id) {
        mutableStateOf(initialValues?.street ?: "")
    }
    var city by rememberSaveable(initialValues?.id) {
        mutableStateOf(initialValues?.city ?: "")
    }
    var state by rememberSaveable(initialValues?.id) {
        mutableStateOf(initialValues?.state ?: "")
    }
    var zipCode by rememberSaveable(initialValues?.id) {
        mutableStateOf(initialValues?.zipCode ?: "")
    }
    var country by rememberSaveable(initialValues?.id) {
        mutableStateOf(initialValues?.country ?: "")
    }
    var isDefault by rememberSaveable(initialValues?.id) {
        mutableStateOf(initialValues?.isDefault ?: false)
    }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(initialValues?.id) {
        fieldErrors = emptyMap()
    }

    fun clearFieldError(field: String) {
        if (fieldErrors.containsKey(field)) fieldErrors = fieldErrors - field
    }

    fun validate(): Boolean {
        val errors = linkedMapOf<String, String>()
        ShopValidators.street(street)?.let { errors["street"] = it }
        ShopValidators.city(city)?.let { errors["city"] = it }
        ShopValidators.state(state)?.let { errors["state"] = it }
        ShopValidators.zipCode(zipCode)?.let { errors["zipCode"] = it }
        ShopValidators.country(country)?.let { errors["country"] = it }
        fieldErrors = errors
        return errors.isEmpty()
    }

    fun submit() {
        if (isSaving || !validate()) return
        onSave(
            AddressRequest(
                street = street.trim(),
                city = city.trim(),
                state = state.trim(),
                zipCode = zipCode.trim(),
                country = country.trim(),
                isDefault = isDefault
            )
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFe5e4e7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (initialValues == null) "Nueva dirección" else "Editar dirección",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF08060d)
            )
            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                value = street,
                onValueChange = {
                    street = it
                    clearFieldError("street")
                },
                label = "Calle",
                error = fieldErrors["street"],
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            FormTextField(
                value = city,
                onValueChange = {
                    city = it
                    clearFieldError("city")
                },
                label = "Ciudad",
                error = fieldErrors["city"],
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            FormTextField(
                value = state,
                onValueChange = {
                    state = it
                    clearFieldError("state")
                },
                label = "Estado / Provincia",
                error = fieldErrors["state"],
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            FormTextField(
                value = zipCode,
                onValueChange = {
                    zipCode = it
                    clearFieldError("zipCode")
                },
                label = "Código postal",
                error = fieldErrors["zipCode"],
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            FormTextField(
                value = country,
                onValueChange = {
                    country = it
                    clearFieldError("country")
                },
                label = "País",
                error = fieldErrors["country"],
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done,
                onDone = { submit() }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSaving) { isDefault = !isDefault },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it },
                    enabled = !isSaving
                )
                Text(
                    text = "Dirección predeterminada",
                    fontSize = 14.sp,
                    color = Color(0xFF08060d)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { submit() },
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563eb),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (isSaving) "Guardando..." else "Guardar",
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
