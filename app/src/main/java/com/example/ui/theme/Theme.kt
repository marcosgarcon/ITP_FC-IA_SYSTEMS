package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = IndigoPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = EmeraldSuccess,
    background = DarkBg,
    onBackground = Color(0xFFF3F4F6),
    surface = DarkSurface,
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = DarkOutline,
    outlineVariant = Color(0xFF4B5563),
    error = RoseError
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECFEFF),
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = EmeraldSuccess,
    background = LightBg,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF64748B),
    outline = LightOutline,
    outlineVariant = Color(0xFFE2E8F0),
    error = RoseError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional sleek tech palette
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
