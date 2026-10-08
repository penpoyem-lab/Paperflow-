package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class GlassColors(
    val isDark: Boolean,
    val isGalacticCodex: Boolean = false,
    val isNothingOs: Boolean = false,
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

private val NothingOsColorScheme = darkColorScheme(
    primary = NothingCrimsonRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3A0A0D),
    onPrimaryContainer = Color(0xFFFFD9DC),
    secondary = NothingGlyphWhite,
    onSecondary = NothingObsidianBlack,
    secondaryContainer = Color(0xFF23262F),
    onSecondaryContainer = NothingGlyphWhite,
    tertiary = LiquidCyan,
    onTertiary = Color(0xFF042F2E),
    background = NothingObsidianBlack,
    onBackground = NothingGlyphWhite,
    surface = NothingCarbonSurface,
    onSurface = NothingGlyphWhite,
    surfaceVariant = Color(0xFF1A1D24),
    onSurfaceVariant = NothingMatrixSilver,
    error = NothingBrightRed
)

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
    isGalacticCodex: Boolean = false,
    isNothingOs: Boolean = false,
    content: @Composable () -> Unit
) {
    val effectiveDark = darkTheme || isGalacticCodex || isNothingOs
    val colorScheme = when {
        isNothingOs -> NothingOsColorScheme
        effectiveDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val targetGlassColors = when {
        isNothingOs -> GlassColors(
            isDark = true,
            isGalacticCodex = false,
            isNothingOs = true,
            panelBackground = Color(0xA8111318),
            panelBackgroundElevated = Color(0xCC181B22),
            borderHighlight = Color(0xD9FFFFFF),
            borderSubtle = Color(0x59D71921),
            textPrimary = NothingGlyphWhite,
            textSecondary = Color(0xFFD0D5DD),
            textMuted = NothingMatrixSilver,
            iconTint = NothingCrimsonRed,
            innerGlowTop = Color(0x52FFFFFF),
            innerGlowBottom = Color(0x40D71921)
        )
        isGalacticCodex -> GlassColors(
            isDark = true,
            isGalacticCodex = true,
            isNothingOs = false,
            panelBackground = Color(0x6B0F1A26),
            panelBackgroundElevated = Color(0x88162634),
            borderHighlight = Color(0x8CFBBF24),
            borderSubtle = Color(0x4014B8A6),
            textPrimary = Color(0xFFF8FAFC),
            textSecondary = Color(0xFFD6E4F0),
            textMuted = Color(0xFF94A3B8),
            iconTint = WarmGold,
            innerGlowTop = Color(0x4DF59E0B),
            innerGlowBottom = Color(0x3314B8A6)
        )
        effectiveDark -> GlassColors(
            isDark = true,
            isGalacticCodex = false,
            isNothingOs = false,
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
        else -> GlassColors(
            isDark = false,
            isGalacticCodex = false,
            isNothingOs = false,
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

    val colorSpring = spring<Color>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val animPanelBg by animateColorAsState(targetGlassColors.panelBackground, colorSpring, label = "panel_bg")
    val animPanelBgElev by animateColorAsState(targetGlassColors.panelBackgroundElevated, colorSpring, label = "panel_bg_elev")
    val animBorderHigh by animateColorAsState(targetGlassColors.borderHighlight, colorSpring, label = "border_high")
    val animBorderSub by animateColorAsState(targetGlassColors.borderSubtle, colorSpring, label = "border_sub")
    val animTextPri by animateColorAsState(targetGlassColors.textPrimary, colorSpring, label = "text_pri")
    val animTextSec by animateColorAsState(targetGlassColors.textSecondary, colorSpring, label = "text_sec")
    val animTextMuted by animateColorAsState(targetGlassColors.textMuted, colorSpring, label = "text_muted")
    val animIconTint by animateColorAsState(targetGlassColors.iconTint, colorSpring, label = "icon_tint")
    val animGlowTop by animateColorAsState(targetGlassColors.innerGlowTop, colorSpring, label = "glow_top")
    val animGlowBottom by animateColorAsState(targetGlassColors.innerGlowBottom, colorSpring, label = "glow_bot")

    val smoothGlassColors = targetGlassColors.copy(
        panelBackground = animPanelBg,
        panelBackgroundElevated = animPanelBgElev,
        borderHighlight = animBorderHigh,
        borderSubtle = animBorderSub,
        textPrimary = animTextPri,
        textSecondary = animTextSec,
        textMuted = animTextMuted,
        iconTint = animIconTint,
        innerGlowTop = animGlowTop,
        innerGlowBottom = animGlowBottom
    )

    CompositionLocalProvider(LocalGlassColors provides smoothGlassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = if (isNothingOs) NothingOsTypography else Typography,
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
