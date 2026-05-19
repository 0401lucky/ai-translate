package com.mxwis.aitranslate.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF5A5D94), // Sophisticated warm violet-indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF0FF), // Soft lavender container
    onPrimaryContainer = Color(0xFF1E214D),
    secondary = Color(0xFF7A6854), // Warm brown/slate-beige
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF7F2EC),
    onSecondaryContainer = Color(0xFF2C241B),
    tertiary = Color(0xFF8B5CF6), // Sleek bright lavender
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF3B0764),
    background = Color(0xFFFAF9F6), // Warm beige/alabaster background, premium cream feel
    surface = Color.White,
    onSurface = Color(0xFF1F1F24), // Charcoal typography
    onSurfaceVariant = Color(0xFF5D5C64),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFECECEF), // Extremely delicate divider/outline
    error = Color(0xFFE11D48), // Vibrant rose error
    errorContainer = Color(0xFFFFECEE),
    onErrorContainer = Color(0xFF4C0519),
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
