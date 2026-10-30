package com.ejemplo.miapp.util

import java.time.YearMonth

object ShopValidators {
    private val ZIP_CODE_REGEX = Regex("^[A-Za-z0-9\\s-]{1,20}$")

    fun street(value: String): String? =
        requiredMax(value, 200, "La calle", "La calle es obligatoria")

    fun city(value: String): String? =
        requiredMax(value, 200, "La ciudad", "La ciudad es obligatoria")

    fun state(value: String): String? =
        requiredMax(value, 200, "El estado/provincia", "El estado/provincia es obligatorio")

    fun country(value: String): String? =
        requiredMax(value, 200, "El país", "El país es obligatorio")

    fun zipCode(value: String): String? = when {
        value.isBlank() -> "El código postal es obligatorio"
        !ZIP_CODE_REGEX.matches(value.trim()) ->
            "El código postal solo puede contener letras, números, espacios y guiones"
        else -> null
    }

    fun cardholderName(value: String): String? =
        AuthValidators.required(value, "El nombre del titular")
            ?: AuthValidators.maxLength(value, 120, "El nombre del titular")

    fun cardNumber(value: String): String? {
        val digits = value.filter(Char::isDigit)
        if (digits.length !in 13..16) return "Número de tarjeta inválido"

        var sum = 0
        var alternate = false
        for (index in digits.indices.reversed()) {
            var digit = digits[index].digitToInt()
            if (alternate) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
            alternate = !alternate
        }
        return if (sum % 10 == 0) null else "Número de tarjeta inválido"
    }

    fun expiryDate(value: String, now: YearMonth = YearMonth.now()): String? {
        val digits = value.filter(Char::isDigit)
        if (digits.length < 4) return "Fecha de vencimiento inválida"

        val month = digits.substring(0, 2).toIntOrNull() ?: return "Fecha de vencimiento inválida"
        val year = digits.substring(2, 4).toIntOrNull()?.plus(2000)
            ?: return "Fecha de vencimiento inválida"
        if (month !in 1..12) return "Mes inválido"

        return if (YearMonth.of(year, month).isBefore(now)) "La tarjeta está vencida" else null
    }

    fun cvv(value: String): String? {
        val digits = value.filter(Char::isDigit)
        return if (digits.length in 3..4) null else "CVV inválido"
    }

    fun alias(value: String): String? = AuthValidators.maxLength(value, 60, "El alias")

    fun cardBrand(cardNumber: String): String {
        val digits = cardNumber.filter(Char::isDigit)
        return when {
            digits.startsWith("4") -> "VISA"
            digits.startsWith("5") -> "MASTERCARD"
            else -> "OTHER"
        }
    }

    fun cardLastFour(cardNumber: String): String =
        cardNumber.filter(Char::isDigit).takeLast(4)

    private fun requiredMax(
        value: String,
        max: Int,
        label: String,
        requiredMessage: String
    ): String? = AuthValidators.maxLength(value, max, label)
        ?: if (value.isBlank()) requiredMessage else null
}
