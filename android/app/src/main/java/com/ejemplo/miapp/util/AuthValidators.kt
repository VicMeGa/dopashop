package com.ejemplo.miapp.util

object AuthValidators {
    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun email(value: String): String? = when {
        value.isBlank() -> "El email es obligatorio"
        !EMAIL_REGEX.matches(value.trim()) -> "El email no tiene un formato válido"
        else -> null
    }

    fun password(value: String, min: Int = 8, max: Int = 72): String? = when {
        value.isEmpty() -> "La contraseña es obligatoria"
        value.length < min -> "La contraseña debe tener al menos $min caracteres"
        value.length > max -> "La contraseña no puede superar los $max caracteres"
        else -> null
    }

    fun required(value: String, label: String): String? =
        if (value.isBlank()) "$label es obligatorio" else null

    fun maxLength(value: String, max: Int, label: String): String? =
        if (value.length > max) "$label no puede superar los $max caracteres" else null

    fun confirmPassword(password: String, confirmation: String): String? =
        if (password != confirmation) "Las contraseñas no coinciden" else null
}
