package com.ejemplo.miapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DopaPcLightColorScheme = lightColorScheme(
    primary = Color(0xFF2563eb),
    onPrimary = Color(0xFFffffff),
    primaryContainer = Color(0xFFffffff),
    onPrimaryContainer = Color(0xFF08060d),
    background = Color(0xFFffffff),
    surface = Color(0xFFffffff),
    onSurface = Color(0xFF08060d),
    outline = Color(0xFFe5e4e7)
)

@Composable
fun DopaPcTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DopaPcLightColorScheme,
        content = content
    )
}
