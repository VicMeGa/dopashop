package com.ejemplo.miapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ejemplo.miapp.util.AuthValidators

enum class AuthMode {
    Login,
    Register
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    mode: AuthMode,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onSwitchMode: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(session) {
        if (session != null) onSuccess()
    }

    LaunchedEffect(mode) {
        viewModel.clearError()
        fieldErrors = emptyMap()
    }

    fun clearFieldError(field: String) {
        if (fieldErrors.containsKey(field)) {
            fieldErrors = fieldErrors - field
        }
        viewModel.clearError()
    }

    fun validate(): Boolean {
        val errors = linkedMapOf<String, String>()
        AuthValidators.email(email)?.let { errors["email"] = it }
        AuthValidators.password(password)?.let { errors["password"] = it }
        if (mode == AuthMode.Register) {
            AuthValidators.required(fullName, "El nombre completo")
                ?.let { errors["fullName"] = it }
            AuthValidators.maxLength(fullName, 120, "El nombre")
                ?.let { if (!errors.containsKey("fullName")) errors["fullName"] = it }
            AuthValidators.confirmPassword(password, confirmPassword)
                ?.let { errors["confirmPassword"] = it }
        }
        fieldErrors = errors
        return errors.isEmpty()
    }

    fun submit() {
        if (uiState.isLoading || !validate()) return
        if (mode == AuthMode.Login) {
            viewModel.login(email, password)
        } else {
            viewModel.register(fullName, email, password)
        }
    }

    val isRegister = mode == AuthMode.Register

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isRegister) "Crear cuenta" else "Iniciar sesión",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = if (isRegister) "Crea tu cuenta" else "Bienvenido de nuevo",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF08060d)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isRegister) {
                    "Regístrate para comprar en dopaPC."
                } else {
                    "Accede para gestionar tus compras."
                },
                fontSize = 14.sp,
                color = Color(0xFF6b6375)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.errorMessage != null) {
                Surface(
                    color = Color(0xFFfee2e2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = Color(0xFFb91c1c),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isRegister) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        clearFieldError("fullName")
                    },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    isError = fieldErrors["fullName"] != null,
                    supportingText = fieldErrors["fullName"]?.let { message ->
                        { Text(message) }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    clearFieldError("email")
                },
                label = { Text("Email") },
                singleLine = true,
                isError = fieldErrors["email"] != null,
                supportingText = fieldErrors["email"]?.let { message ->
                    { Text(message) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    clearFieldError("password")
                },
                label = { Text("Contraseña") },
                singleLine = true,
                isError = fieldErrors["password"] != null,
                supportingText = fieldErrors["password"]?.let { message ->
                    { Text(message) }
                },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(
                        onClick = { passwordVisible = !passwordVisible },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(if (passwordVisible) "Ocultar" else "Ver", fontSize = 12.sp)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (isRegister) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { submit() }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            if (isRegister) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        clearFieldError("confirmPassword")
                    },
                    label = { Text("Confirmar contraseña") },
                    singleLine = true,
                    isError = fieldErrors["confirmPassword"] != null,
                    supportingText = fieldErrors["confirmPassword"]?.let { message ->
                        { Text(message) }
                    },
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                if (confirmPasswordVisible) "Ocultar" else "Ver",
                                fontSize = 12.sp
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { submit() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563eb),
                    contentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRegister) "Crear cuenta" else "Iniciar sesión",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegister) "¿Ya tienes cuenta?" else "¿No tienes cuenta?",
                    fontSize = 14.sp,
                    color = Color(0xFF6b6375)
                )
                TextButton(onClick = onSwitchMode) {
                    Text(
                        text = if (isRegister) "Inicia sesión" else "Regístrate",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
