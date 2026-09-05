package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = ForestObsidian,
    primaryContainer = ForestCard,
    onPrimaryContainer = EmeraldPrimary,
    secondary = AmberAlert,
    onSecondary = ForestObsidian,
    secondaryContainer = Color(0xFF2C2210),
    onSecondaryContainer = AmberAlert,
    tertiary = CyanRadar,
    onTertiary = ForestObsidian,
    background = ForestObsidian,
    onBackground = TextPrimary,
    surface = ForestSurface,
    onSurface = TextPrimary,
    surfaceVariant = ForestCard,
    onSurfaceVariant = TextSecondary,
    outline = ForestCardBorder,
    error = CrimsonCritical,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF047857),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    tertiary = Color(0xFF0E7490),
    background = Color(0xFFF7FDF9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White
)

@Composable
fun WildGuardTheme(
    darkTheme: Boolean = true, // Default to sleek tactical dark command theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
