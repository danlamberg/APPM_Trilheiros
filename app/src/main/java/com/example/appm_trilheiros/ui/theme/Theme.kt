package com.example.appm_trilheiros.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Definindo a cor laranja
private val Orange = Color(0xFFFFA500)

// Tema escuro
private val DarkColorScheme = darkColorScheme(
    primary = Orange,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// Tema claro
private val LightColorScheme = lightColorScheme(
    primary = Orange, // Botões em laranja no tema claro
    secondary = PurpleGrey40,
    tertiary = Pink40

    // Outras cores podem ser ajustadas conforme necessário
    // background = Color(0xFFFFFBFE),
    // surface = Color(0xFFFFFBFE),
    // onPrimary = Color.White, // Texto branco sobre o botão laranja
    // onSecondary = Color.White,
    // onTertiary = Color.White,
    // onBackground = Color(0xFF1C1B1F),
    // onSurface = Color(0xFF1C1B1F),
)

// Função de tema da aplicação
@Composable
fun APPM_TrilheirosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
