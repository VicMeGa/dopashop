package com.ejemplo.miapp.ui.payments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ejemplo.miapp.data.model.PaymentMethodRequest
import com.ejemplo.miapp.ui.common.FormTextField
import com.ejemplo.miapp.util.ShopValidators

@Composable
fun CardForm(
    isSaving: Boolean,
    onSave: (PaymentMethodRequest) -> Unit,
    onCancel: () -> Unit
) {
    var cardholderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    fun clearFieldError(field: String) {
        if (fieldErrors.containsKey(field)) fieldErrors = fieldErrors - field
    }

    fun validate(): Boolean {
        val errors = linkedMapOf<String, String>()
        ShopValidators.cardholderName(cardholderName)?.let { errors["cardholderName"] = it }
        ShopValidators.cardNumber(cardNumber)?.let { errors["cardNumber"] = it }
        ShopValidators.expiryDate(expiryDate)?.let { errors["expiryDate"] = it }
        ShopValidators.cvv(cvv)?.let { errors["cvv"] = it }
        ShopValidators.alias(alias)?.let { errors["alias"] = it }
        fieldErrors = errors
        return errors.isEmpty()
    }

    fun submit() {
        if (isSaving || !validate()) return

        val lastFour = ShopValidators.cardLastFour(cardNumber)
        val brand = ShopValidators.cardBrand(cardNumber)
        val finalAlias = alias.trim().ifBlank { "Tarjeta terminada en $lastFour" }

        onSave(
            PaymentMethodRequest(
                alias = finalAlias,
                cardLastFour = lastFour,
                cardBrand = brand
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
                text = "Nueva tarjeta",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF08060d)
            )
            Spacer(modifier = Modifier.height(12.dp))

            FormTextField(
                value = cardholderName,
                onValueChange = {
                    cardholderName = it
                    clearFieldError("cardholderName")
                },
                label = "Titular de la tarjeta",
                error = fieldErrors["cardholderName"],
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            FormTextField(
                value = cardNumber,
                onValueChange = {
                    cardNumber = formatCardNumber(it)
                    clearFieldError("cardNumber")
                },
                label = "Número de tarjeta",
                error = fieldErrors["cardNumber"],
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormTextField(
                    value = expiryDate,
                    onValueChange = {
                        expiryDate = formatExpiry(it)
                        clearFieldError("expiryDate")
                    },
                    label = "Vencimiento",
                    error = fieldErrors["expiryDate"],
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
                FormTextField(
                    value = cvv,
                    onValueChange = {
                        cvv = formatCvv(it)
                        clearFieldError("cvv")
                    },
                    label = "CVV",
                    error = fieldErrors["cvv"],
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FormTextField(
                value = alias,
                onValueChange = {
                    alias = it
                    clearFieldError("alias")
                },
                label = "Alias (opcional)",
                error = fieldErrors["alias"],
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done,
                onDone = { submit() }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Solo guardamos los últimos 4 dígitos y la marca de la tarjeta.",
                fontSize = 12.sp,
                color = Color(0xFF6b6375)
            )
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
                        text = if (isSaving) "Guardando..." else "Guardar tarjeta",
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

private fun formatCardNumber(value: String): String =
    value.filter(Char::isDigit)
        .take(16)
        .chunked(4)
        .joinToString(" ")

private fun formatExpiry(value: String): String {
    val digits = value.filter(Char::isDigit).take(4)
    return if (digits.length <= 2) digits else "${digits.take(2)}/${digits.drop(2)}"
}

private fun formatCvv(value: String): String =
    value.filter(Char::isDigit).take(4)
