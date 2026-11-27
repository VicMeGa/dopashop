package com.ejemplo.miapp.ui.common

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SPANISH_LOCALE = Locale.forLanguageTag("es-ES")
private val ORDER_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy", SPANISH_LOCALE)
private val ORDER_DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("d 'de' MMMM, yyyy HH:mm", SPANISH_LOCALE)

fun formatPrice(value: Double): String =
    String.format(Locale.US, "\$%.2f", value)

fun formatOrderDate(iso: String): String =
    parseOrderDate(iso)?.format(ORDER_DATE_FORMATTER) ?: iso

fun formatOrderDateTime(iso: String): String =
    parseOrderDate(iso)?.format(ORDER_DATE_TIME_FORMATTER) ?: iso

private fun parseOrderDate(iso: String): LocalDateTime? =
    runCatching { LocalDateTime.parse(iso) }.getOrNull()
        ?: runCatching { OffsetDateTime.parse(iso).toLocalDateTime() }.getOrNull()
