package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber

@Composable
fun LiquidAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_refraction")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (glass.isDark) {
                // Deep Navy / Black Spatial Canvas with Luminous Orbs
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF060918),
                            Color(0xFF0C1430),
                            Color(0xFF10132E),
                            Color(0xFF050814)
                        )
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(w * (0.2f + 0.15f * drift), h * 0.14f),
                        radius = w * 0.72f
                    ),
                    radius = w * 0.72f,
                    center = Offset(w * (0.2f + 0.15f * drift), h * 0.14f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PrismPurple.copy(alpha = 0.24f), Color.Transparent),
                        center = Offset(w * (0.85f - 0.12f * drift), h * 0.34f),
                        radius = w * 0.68f
                    ),
                    radius = w * 0.68f,
                    center = Offset(w * (0.85f - 0.12f * drift), h * 0.34f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidCyan.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * 0.3f, h * (0.72f - 0.08f * drift)),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.3f, h * (0.72f - 0.08f * drift))
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(IridescentPink.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(w * 0.78f, h * 0.88f),
                        radius = w * 0.55f
                    ),
                    radius = w * 0.55f,
                    center = Offset(w * 0.78f, h * 0.88f)
                )
            } else {
                // Airy Iridescent VisionOS Light Mode Canvas matching the reference aesthetic
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF9AC5F8), // Soft sky electric blue top
                            Color(0xFFC5D8FF), // Periwinkle mist
                            Color(0xFFDECFF9), // Soft lavender refraction
                            Color(0xFFC7DAF8), // Cool glass blue
                            Color(0xFFD9C7F2)  // Iridescent bottom glow
                        )
                    )
                )

                // Top-left cyan/blue refraction wave
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LiquidCyan.copy(alpha = 0.38f), Color.Transparent),
                        center = Offset(w * (0.15f + 0.15f * drift), h * 0.09f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * (0.15f + 0.15f * drift), h * 0.09f)
                )

                // Top-right warm pearl/lavender highlight
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFDF4FF).copy(alpha = 0.72f), Color.Transparent),
                        center = Offset(w * (0.78f - 0.1f * drift), h * 0.12f),
                        radius = w * 0.58f
                    ),
                    radius = w * 0.58f,
                    center = Offset(w * (0.78f - 0.1f * drift), h * 0.12f)
                )

                // Center-right warm amber/pink refraction
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SolarAmber.copy(alpha = 0.22f), IridescentPink.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(w * 0.88f, h * (0.48f + 0.06f * drift)),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.88f, h * (0.48f + 0.06f * drift))
                )

                // Bottom-left electric blue & teal refraction
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.28f), CrystalTeal.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(w * 0.18f, h * 0.75f),
                        radius = w * 0.7f
                    ),
                    radius = w * 0.7f,
                    center = Offset(w * 0.18f, h * 0.75f)
                )
            }
        }
        content()
    }
}

@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    tintColor: Color = Color.Transparent,
    borderAlpha: Float = 0.85f,
    shadowElevation: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassColors.current
    val shape = RoundedCornerShape(cornerRadius)

    val baseSurface = if (glass.isDark) {
        Color(0xFF172242).copy(alpha = 0.68f)
    } else {
        Color.White.copy(alpha = 0.56f)
    }

    val specularTop = if (glass.isDark) {
        Color.White.copy(alpha = 0.22f)
    } else {
        Color.White.copy(alpha = 0.88f)
    }

    val specularBottom = if (glass.isDark) {
        tintColor.copy(alpha = 0.24f)
    } else {
        if (tintColor != Color.Transparent) tintColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.35f)
    }

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = if (glass.isDark) 0.52f * borderAlpha else 0.95f * borderAlpha),
            if (tintColor != Color.Transparent) tintColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.35f),
            Color.White.copy(alpha = if (glass.isDark) 0.20f * borderAlpha else 0.65f * borderAlpha)
        )
    )

    val fillBrush = Brush.linearGradient(
        colors = listOf(
            baseSurface,
            if (tintColor != Color.Transparent) {
                tintColor.copy(alpha = if (glass.isDark) 0.24f else 0.22f)
            } else {
                baseSurface
            },
            if (glass.isDark) Color(0xFF1E294E).copy(alpha = 0.62f) else Color.White.copy(alpha = 0.44f)
        )
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = if (tintColor != Color.Transparent) tintColor.copy(alpha = 0.35f) else ElectricBlue.copy(alpha = 0.18f),
                spotColor = if (tintColor != Color.Transparent) tintColor.copy(alpha = 0.35f) else PrismViolet.copy(alpha = 0.22f)
            )
            .clip(shape)
            .background(brush = fillBrush, shape = shape)
            .drawWithContent {
                // Top inner liquid glass specular sheen
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(specularTop.copy(alpha = 0.32f), Color.Transparent, specularBottom.copy(alpha = 0.14f))
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                )
                drawContent()
            }
            .border(width = 1.3.dp, brush = borderBrush, shape = shape)
            .then(clickableModifier),
        content = content
    )
}

@Composable
fun GlassCircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    tint: Color? = null,
    accentGlow: Color = ElectricBlue
) {
    val glass = LocalGlassColors.current
    val resolvedTint = tint ?: if (glass.isDark) Color.White else Color(0xFF1E293B)

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = accentGlow.copy(alpha = 0.25f),
                spotColor = accentGlow.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = if (glass.isDark) {
                        listOf(Color.White.copy(alpha = 0.22f), Color(0xFF1E293B).copy(alpha = 0.65f))
                    } else {
                        listOf(Color.White.copy(alpha = 0.78f), Color.White.copy(alpha = 0.42f))
                    }
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (glass.isDark) 0.65f else 0.95f),
                        accentGlow.copy(alpha = 0.35f)
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = resolvedTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun FloatingGlassNavigationBar(
    currentTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val capsuleShape = RoundedCornerShape(42.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .height(78.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = capsuleShape,
                    ambientColor = ElectricBlue.copy(alpha = 0.32f),
                    spotColor = PrismPurple.copy(alpha = 0.35f)
                )
                .clip(capsuleShape)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (glass.isDark) {
                            listOf(
                                Color(0xFF162244).copy(alpha = 0.86f),
                                Color(0xFF1F1D47).copy(alpha = 0.86f),
                                Color(0xFF281A45).copy(alpha = 0.86f),
                                Color(0xFF162647).copy(alpha = 0.86f)
                            )
                        } else {
                            listOf(
                                Color(0xFFD8ECFF).copy(alpha = 0.85f),
                                Color(0xFFEAF2FF).copy(alpha = 0.88f),
                                Color(0xFFF6E6FF).copy(alpha = 0.86f),
                                Color(0xFFDFF0FF).copy(alpha = 0.85f)
                            )
                        }
                    )
                )
                .drawBehind {
                    // Colorful liquid-light refraction glow inside capsule
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(LiquidCyan.copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(size.width * 0.16f, size.height * 0.5f),
                            radius = size.height * 1.1f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(IridescentPink.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width * 0.72f, size.height * 0.35f),
                            radius = size.height * 1.1f
                        )
                    )
                }
                .border(
                    width = 1.6.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (glass.isDark) 0.7f else 0.98f),
                            LiquidCyan.copy(alpha = 0.55f),
                            IridescentPink.copy(alpha = 0.50f),
                            Color.White.copy(alpha = if (glass.isDark) 0.6f else 0.95f)
                        )
                    ),
                    shape = capsuleShape
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val items = listOf(
                    NavTabSpec(MainTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home, ElectricBlue, "nav_home"),
                    NavTabSpec(MainTab.TOOLS, "Tools", Icons.Filled.Build, Icons.Outlined.Build, PrismViolet, "nav_tools"),
                    NavTabSpec(MainTab.LIBRARY, "Library", Icons.Filled.FolderSpecial, Icons.Outlined.FolderCopy, CrystalTeal, "nav_library"),
                    NavTabSpec(MainTab.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, IridescentPink, "nav_settings")
                )

                items.forEach { item ->
                    val isSelected = currentTab == item.tab
                    val pillElevation by animateDpAsState(
                        targetValue = if (isSelected) 8.dp else 0.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "nav_elev"
                    )
                    val textColor by animateColorAsState(
                        targetValue = when {
                            isSelected && glass.isDark -> Color.White
                            isSelected && !glass.isDark -> Color(0xFF0A4DA2)
                            glass.isDark -> Color(0xFFCBD5E1)
                            else -> Color(0xFF1E293B)
                        },
                        label = "nav_text"
                    )

                    val innerCapsuleShape = RoundedCornerShape(34.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag(item.testTag)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(
                                            elevation = pillElevation,
                                            shape = innerCapsuleShape,
                                            ambientColor = item.glowColor.copy(alpha = 0.45f),
                                            spotColor = LiquidCyan.copy(alpha = 0.45f)
                                        )
                                        .clip(innerCapsuleShape)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = if (glass.isDark) {
                                                    listOf(
                                                        Color.White.copy(alpha = 0.25f),
                                                        item.glowColor.copy(alpha = 0.35f)
                                                    )
                                                } else {
                                                    listOf(
                                                        Color.White.copy(alpha = 0.92f),
                                                        Color(0xFFD6F2FF).copy(alpha = 0.85f)
                                                    )
                                                }
                                            )
                                        )
                                        .border(
                                            width = 1.4.dp,
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color.White,
                                                    LiquidCyan.copy(alpha = 0.7f),
                                                    Color.White.copy(alpha = 0.85f)
                                                )
                                            ),
                                            shape = innerCapsuleShape
                                        )
                                } else {
                                    Modifier.clip(innerCapsuleShape)
                                }
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelectTab(item.tab) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavTabSpec(
    val tab: MainTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val glowColor: Color,
    val testTag: String
)
