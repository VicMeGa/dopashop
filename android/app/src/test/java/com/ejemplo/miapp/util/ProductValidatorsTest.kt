package com.ejemplo.miapp.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductValidatorsTest {
    @Test
    fun storeFieldsEnforceRequiredAndMaxLength() {
        assertEquals("El nombre de la tienda es obligatorio", ShopValidators.storeName("  "))
        assertEquals(
            "El nombre de la tienda no puede superar los 120 caracteres",
            ShopValidators.storeName("a".repeat(121))
        )
        assertEquals(
            "La descripción no puede superar los 4000 caracteres",
            ShopValidators.storeDescription("a".repeat(4001))
        )
        assertNull(ShopValidators.storeName("Componentes Víctor"))
        assertNull(ShopValidators.storeDescription("a".repeat(4000)))
    }

    @Test
    fun productFieldsEnforceRequiredAndMaxLength() {
        assertEquals("El título es obligatorio", ShopValidators.productTitle("  "))
        assertEquals(
            "El título no puede superar los 200 caracteres",
            ShopValidators.productTitle("a".repeat(201))
        )
        assertEquals(
            "La descripción no puede superar los 4000 caracteres",
            ShopValidators.productDescription("a".repeat(4001))
        )
        assertNull(ShopValidators.productTitle("Mouse inalámbrico"))
        assertNull(ShopValidators.productDescription("a".repeat(4000)))
    }

    @Test
    fun productPriceRequiresPositiveValueAndAtMostTwoDecimals() {
        assertEquals(
            "El precio debe ser un número mayor a 0",
            ShopValidators.productPrice("")
        )
        assertEquals(
            "El precio debe ser un número mayor a 0",
            ShopValidators.productPrice("0")
        )
        assertEquals(
            "El precio debe ser un número mayor a 0",
            ShopValidators.productPrice("-1")
        )
        assertEquals(
            "El precio debe ser un número mayor a 0",
            ShopValidators.productPrice("abc")
        )
        assertEquals(
            "El precio no puede tener más de 2 decimales",
            ShopValidators.productPrice("10.999")
        )
        assertNull(ShopValidators.productPrice("10"))
        assertNull(ShopValidators.productPrice("10.5"))
        assertNull(ShopValidators.productPrice("10.50"))
        assertNull(ShopValidators.productPrice(" 10.50 "))
    }

    @Test
    fun productStockRequiresNonNegativeWholeNumber() {
        assertEquals("El stock es obligatorio", ShopValidators.productStock("  "))
        assertEquals(
            "El stock debe ser un número entero mayor o igual a 0",
            ShopValidators.productStock("abc")
        )
        assertEquals(
            "El stock debe ser un número entero mayor o igual a 0",
            ShopValidators.productStock("-1")
        )
        assertEquals(
            "El stock debe ser un número entero mayor o igual a 0",
            ShopValidators.productStock("1.5")
        )
        assertEquals(
            "El stock no puede superar 1.000.000",
            ShopValidators.productStock("1000001")
        )
        assertNull(ShopValidators.productStock("0"))
        assertNull(ShopValidators.productStock("1.0"))
        assertNull(ShopValidators.productStock("1000000"))
    }

    @Test
    fun productCategoryIsRequired() {
        assertEquals("Selecciona una categoría", ShopValidators.productCategory(null))
        assertNull(ShopValidators.productCategory(1L))
    }
}
