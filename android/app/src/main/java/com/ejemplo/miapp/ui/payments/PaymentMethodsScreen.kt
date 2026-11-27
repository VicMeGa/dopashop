package com.ejemplo.miapp.ui.payments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ejemplo.miapp.data.model.PaymentMethodResponse
import com.ejemplo.miapp.ui.common.ErrorBanner

@Composable
fun PaymentMethodsScreen(viewModel: PaymentMethodsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var methodToDelete by remember { mutableStateOf<PaymentMethodResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Mis métodos de pago",
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF08060d)
            )
            Button(
                onClick = {
                    if (uiState.showForm) viewModel.cancelForm() else viewModel.startCreate()
                },
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563eb),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (uiState.showForm) "Cancelar" else "Agregar tarjeta",
                    fontSize = 14.sp
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2563eb))
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                uiState.errorMessage?.let { message ->
                    ErrorBanner(message)
                    TextButton(
                        onClick = viewModel::load,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2563eb))
                    ) {
                        Text("Reintentar")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.showForm) {
                    CardForm(
                        isSaving = uiState.isSaving,
                        onSave = viewModel::save,
                        onCancel = viewModel::cancelForm
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.methods.isEmpty() && !uiState.showForm && uiState.errorMessage == null) {
                    Text(
                        text = "No tenés métodos de pago guardados.",
                        fontSize = 14.sp,
                        color = Color(0xFF6b6375)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                uiState.methods.forEach { method ->
                    PaymentMethodCard(
                        method = method,
                        deleting = uiState.deletingId == method.id,
                        onDelete = { methodToDelete = method }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    methodToDelete?.let { method ->
        AlertDialog(
            onDismissRequest = { methodToDelete = null },
            title = { Text("Eliminar tarjeta") },
            text = {
                Text("¿Eliminar ${method.alias} terminada en ${method.cardLastFour}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(method.id)
                        methodToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFe74c3c))
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { methodToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethodResponse,
    deleting: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFe5e4e7))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = method.cardBrand,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6b6375)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = method.alias,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF08060d)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "•••• ${method.cardLastFour}",
                    fontSize = 14.sp,
                    color = Color(0xFF6b6375)
                )
            }
            TextButton(
                onClick = onDelete,
                enabled = !deleting,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFe74c3c))
            ) {
                Text(if (deleting) "Eliminando..." else "Eliminar")
            }
        }
    }
}
