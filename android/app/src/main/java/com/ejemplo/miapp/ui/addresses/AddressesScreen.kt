package com.ejemplo.miapp.ui.addresses

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
import com.ejemplo.miapp.data.model.AddressResponse
import com.ejemplo.miapp.ui.common.ErrorBanner

@Composable
fun AddressesScreen(viewModel: AddressesViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var addressToDelete by remember { mutableStateOf<AddressResponse?>(null) }

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
                text = "Mis direcciones",
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
                    text = if (uiState.showForm) "Cancelar" else "Agregar dirección",
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
                    AddressForm(
                        initialValues = uiState.editingAddress,
                        isSaving = uiState.isSaving,
                        onSave = viewModel::save,
                        onCancel = viewModel::cancelForm
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.addresses.isEmpty() && !uiState.showForm && uiState.errorMessage == null) {
                    Text(
                        text = "No tenés direcciones guardadas.",
                        fontSize = 14.sp,
                        color = Color(0xFF6b6375)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                uiState.addresses.forEach { address ->
                    AddressCard(
                        address = address,
                        deleting = uiState.deletingId == address.id,
                        onEdit = { viewModel.startEdit(address) },
                        onDelete = { addressToDelete = address }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    addressToDelete?.let { address ->
        AlertDialog(
            onDismissRequest = { addressToDelete = null },
            title = { Text("Eliminar dirección") },
            text = { Text("¿Eliminar la dirección ${address.street}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(address.id)
                        addressToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFe74c3c))
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { addressToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AddressCard(
    address: AddressResponse,
    deleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFe5e4e7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = address.street,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF08060d)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${address.city}, ${address.state} ${address.zipCode}",
                fontSize = 14.sp,
                color = Color(0xFF6b6375)
            )
            Text(
                text = address.country,
                fontSize = 14.sp,
                color = Color(0xFF6b6375)
            )

            if (address.isDefault) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF2563eb).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Predeterminada",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2563eb),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !deleting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Editar")
                }
                TextButton(
                    onClick = onDelete,
                    enabled = !deleting,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFe74c3c))
                ) {
                    Text(if (deleting) "Eliminando..." else "Eliminar")
                }
            }
        }
    }
}
