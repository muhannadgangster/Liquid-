package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = DeepSpaceNavy,
    primaryContainer = Color(0x3338BDF8),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = IridescentTeal,
    onSecondary = DeepSpaceNavy,
    tertiary = NeonPinkSheen,
    onTertiary = DeepSpaceNavy,
    background = DeepSpaceNavy,
    onBackground = GlassTextPrimary,
    surface = SurfaceDarkGlass,
    onSurface = GlassTextPrimary,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = GlassTextSecondary,
    outline = GlassBorderSubtle
)

private val LightColorScheme = lightColorScheme(
    primary = SapphireBright,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = IridescentTeal,
    onSecondary = Color.White,
    tertiary = NeonPinkSheen,
    onTertiary = Color.White,
    background = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "DARK",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
