package com.ejemplo.miapp.util

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShopValidatorsTest {
    @Test
    fun addressFieldsEnforceRequiredAndMaxLength() {
        assertEquals("La calle es obligatoria", ShopValidators.street("  "))
        assertEquals(
            "La calle no puede superar los 200 caracteres",
            ShopValidators.street("a".repeat(201))
        )
        assertEquals("La ciudad es obligatoria", ShopValidators.city(""))
        assertEquals("El estado/provincia es obligatorio", ShopValidators.state(""))
        assertEquals("El país es obligatorio", ShopValidators.country(""))
        assertNull(ShopValidators.street("Av. Siempre Viva 742"))
    }

    @Test
    fun zipCodeOnlyAllowsLettersNumbersSpacesAndDashes() {
        assertEquals(
            "El código postal es obligatorio",
            ShopValidators.zipCode(" ")
        )
        assertEquals(
            "El código postal solo puede contener letras, números, espacios y guiones",
            ShopValidators.zipCode("B1900!")
        )
        assertNull(ShopValidators.zipCode("B1900-ABC"))
    }

    @Test
    fun cardNumberRequiresValidLengthAndLuhnCheck() {
        assertEquals("Número de tarjeta inválido", ShopValidators.cardNumber("1234"))
        assertEquals(
            "Número de tarjeta inválido",
            ShopValidators.cardNumber("4111 1111 1111 1112")
        )
        assertNull(ShopValidators.cardNumber("4111 1111 1111 1111"))
        assertNull(ShopValidators.cardNumber("5555555555554444"))
    }

    @Test
    fun expiryDateRejectsInvalidAndExpiredCards() {
        val now = YearMonth.of(2026, 10)
        assertEquals(
            "Fecha de vencimiento inválida",
            ShopValidators.expiryDate("12/2", now)
        )
        assertEquals("Mes inválido", ShopValidators.expiryDate("13/30", now))
        assertEquals("La tarjeta está vencida", ShopValidators.expiryDate("09/26", now))
        assertNull(ShopValidators.expiryDate("10/26", now))
        assertNull(ShopValidators.expiryDate("12/30", now))
    }

    @Test
    fun cvvRequiresThreeOrFourDigits() {
        assertEquals("CVV inválido", ShopValidators.cvv("12"))
        assertEquals("CVV inválido", ShopValidators.cvv("12345"))
        assertNull(ShopValidators.cvv("123"))
        assertNull(ShopValidators.cvv("1234"))
    }

    @Test
    fun cardHelpersReturnBrandAndLastFour() {
        assertEquals("VISA", ShopValidators.cardBrand("4111 1111 1111 1111"))
        assertEquals("MASTERCARD", ShopValidators.cardBrand("5555 5555 5555 4444"))
        assertEquals("OTHER", ShopValidators.cardBrand("6011 1111 1111 1117"))
        assertEquals("1111", ShopValidators.cardLastFour("4111 1111 1111 1111"))
    }

    @Test
    fun aliasIsOptionalButLimitedToSixtyCharacters() {
        assertNull(ShopValidators.alias(""))
        assertEquals(
            "El alias no puede superar los 60 caracteres",
            ShopValidators.alias("a".repeat(61))
        )
        assertNull(ShopValidators.alias("Mi Visa principal"))
    }
}
