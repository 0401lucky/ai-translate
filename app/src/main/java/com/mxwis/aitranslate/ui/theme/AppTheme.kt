package com.mxwis.aitranslate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7F1E7),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF546E7A),
    background = Color(0xFFF7FAF9),
    surface = Color.White,
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF52DBC9),
    onPrimary = Color(0xFF003731),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFFA7F1E7),
    secondary = Color(0xFFB8CAD0),
    background = Color(0xFF101414),
    surface = Color(0xFF171D1C),
    error = Color(0xFFFFB4AB),
)

@Composable
fun AiTranslateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
