package com.example.appm_trilheiros.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

// Tema escuro com fundo preto e botões laranja
private val DarkColorScheme = darkColorScheme(
    primary = Orange, // Cor principal (botões)
    onPrimary = Black, // Cor do texto em cima dos botões (preto)
    background = Black, // Fundo da tela
    surface = Black, // Cor de superfície (ex.: cards)
    onBackground = White, // Cor do texto sobre o fundo
    onSurface = White // Cor do texto sobre superfícies
)

// Tema claro com fundo branco e botões laranja
private val LightColorScheme = lightColorScheme(
    primary = Orange, // Cor principal (botões)
    onPrimary = Black, // Cor do texto em cima dos botões (preto)
    background = White, // Fundo da tela
    surface = White, // Cor de superfície
    onBackground = Black, // Cor do texto sobre o fundo
    onSurface = Black // Cor do texto sobre superfícies
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
