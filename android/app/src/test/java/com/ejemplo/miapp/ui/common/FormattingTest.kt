package com.ejemplo.miapp.ui.common

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingTest {
    @Test
    fun formatPriceUsesTwoDecimalsAndDollarSign() {
        assertEquals("\$12.50", formatPrice(12.5))
        assertEquals("\$0.00", formatPrice(0.0))
    }

    @Test
    fun formatOrderDateUsesSpanishDate() {
        assertEquals("2 de noviembre, 2026", formatOrderDate("2026-11-02T15:30:00"))
    }

    @Test
    fun formatOrderDateTimeIncludesTime() {
        assertEquals(
            "2 de noviembre, 2026 15:30",
            formatOrderDateTime("2026-11-02T15:30:00")
        )
    }

    @Test
    fun formatOrderDateFallsBackToRawValue() {
        assertEquals("not-a-date", formatOrderDate("not-a-date"))
    }
}
