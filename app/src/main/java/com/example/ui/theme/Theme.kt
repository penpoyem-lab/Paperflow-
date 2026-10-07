package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class GlassColors(
    val isDark: Boolean,
    val panelBackground: Color,
    val panelBackgroundElevated: Color,
    val borderHighlight: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val iconTint: Color,
    val innerGlowTop: Color,
    val innerGlowBottom: Color
)

val LocalGlassColors = staticCompositionLocalOf {
    GlassColors(
        isDark = false,
        panelBackground = LightGlassSurface,
        panelBackgroundElevated = LightGlassSurfaceElevated,
        borderHighlight = Color(0xEBFFFFFF),
        borderSubtle = Color(0x73FFFFFF),
        textPrimary = LightTextPrimary,
        textSecondary = LightTextSecondary,
        textMuted = LightTextMuted,
        iconTint = ElectricBlue,
        innerGlowTop = Color(0x88FFFFFF),
        innerGlowBottom = Color(0x263B82F6)
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = PrismViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = LiquidCyan,
    onTertiary = Color(0xFF083344),
    background = DarkAmbientBase,
    onBackground = DarkTextPrimary,
    surface = DarkAmbientNavy,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = DarkTextSecondary,
    error = LiquidMagenta
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = PrismViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF4C1D95),
    tertiary = CrystalTeal,
    onTertiary = Color.White,
    background = LightAmbientBase,
    onBackground = LightTextPrimary,
    surface = Color(0xFFF8FAFC),
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = LightTextSecondary,
    error = LiquidMagenta
)

@Composable
fun GlassPaperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val glassColors = if (darkTheme) {
        GlassColors(
            isDark = true,
            panelBackground = Color(0x5E16203D),
            panelBackgroundElevated = Color(0x801E2B52),
            borderHighlight = Color(0x66FFFFFF),
            borderSubtle = Color(0x26FFFFFF),
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            textMuted = DarkTextMuted,
            iconTint = LiquidCyan,
            innerGlowTop = Color(0x3D60A5FA),
            innerGlowBottom = Color(0x26A855F7)
        )
    } else {
        GlassColors(
            isDark = false,
            panelBackground = Color(0x8CFFFFFF),
            panelBackgroundElevated = Color(0xB8FFFFFF),
            borderHighlight = Color(0xF2FFFFFF),
            borderSubtle = Color(0x80FFFFFF),
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            iconTint = ElectricBlue,
            innerGlowTop = Color(0xB3FFFFFF),
            innerGlowBottom = Color(0x2E3B82F6)
        )
    }

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    GlassPaperTheme(darkTheme = darkTheme, content = content)
}
