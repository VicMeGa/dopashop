package com.ejemplo.miapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorsTest {
    @Test
    fun emailRejectsBlankAndMalformedValues() {
        assertEquals("El email es obligatorio", AuthValidators.email(""))
        assertEquals("El email es obligatorio", AuthValidators.email("   "))
        assertEquals(
            "El email no tiene un formato válido",
            AuthValidators.email("victor@")
        )
        assertEquals(
            "El email no tiene un formato válido",
            AuthValidators.email("victor@localhost")
        )
    }

    @Test
    fun emailAcceptsValidAddress() {
        assertNull(AuthValidators.email("victor@example.com"))
        assertNull(AuthValidators.email("  victor@example.com  "))
    }

    @Test
    fun passwordEnforcesLengthLimits() {
        assertEquals("La contraseña es obligatoria", AuthValidators.password(""))
        assertEquals(
            "La contraseña debe tener al menos 8 caracteres",
            AuthValidators.password("1234567")
        )
        assertEquals(
            "La contraseña no puede superar los 72 caracteres",
            AuthValidators.password("a".repeat(73))
        )
        assertNull(AuthValidators.password("12345678"))
        assertNull(AuthValidators.password("a".repeat(72)))
    }

    @Test
    fun requiredAndMaxLengthReportExpectedMessages() {
        assertEquals(
            "El nombre completo es obligatorio",
            AuthValidators.required("  ", "El nombre completo")
        )
        assertEquals(
            "El nombre no puede superar los 120 caracteres",
            AuthValidators.maxLength("a".repeat(121), 120, "El nombre")
        )
        assertNull(AuthValidators.required("Víctor", "El nombre completo"))
        assertNull(AuthValidators.maxLength("a".repeat(120), 120, "El nombre"))
    }

    @Test
    fun confirmPasswordRequiresExactMatch() {
        assertEquals(
            "Las contraseñas no coinciden",
            AuthValidators.confirmPassword("12345678", "87654321")
        )
        assertNull(AuthValidators.confirmPassword("12345678", "12345678"))
    }
}
