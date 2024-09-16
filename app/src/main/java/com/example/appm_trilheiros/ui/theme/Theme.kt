package com.example.appm_trilheiros.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable


private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    onPrimary = Black,
    background = Black,
    surface = Black,
    onBackground = White,
    onSurface = White
)


private val LightColorScheme = lightColorScheme(
    primary = Orange,
    onPrimary = Black,
    background = White,
    surface = White,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun APPM_TrilheirosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
