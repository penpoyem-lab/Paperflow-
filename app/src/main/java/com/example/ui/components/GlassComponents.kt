package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.DesertClayCardElevated
import com.example.ui.theme.DesertClayCardSurface
import com.example.ui.theme.DesertClayCocoaBrown
import com.example.ui.theme.DesertClayCreamBase
import com.example.ui.theme.DesertClayDeepTerracotta
import com.example.ui.theme.DesertClayMutedBrown
import com.example.ui.theme.DesertClayPeachTerracotta
import com.example.ui.theme.DesertClaySoftApricot
import com.example.ui.theme.DesertClayWarmSand
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ForestCanopyDarkMoss
import com.example.ui.theme.ForestCanopyDeepGreen
import com.example.ui.theme.ForestCopperBark
import com.example.ui.theme.ForestDeepMossText
import com.example.ui.theme.ForestGoldenFern
import com.example.ui.theme.ForestParchmentLight
import com.example.ui.theme.ForestParchmentWarm
import com.example.ui.theme.ForestSageGreen
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.NothingBrightRed
import com.example.ui.theme.NothingCarbonSurface
import com.example.ui.theme.NothingCrimsonRed
import com.example.ui.theme.NothingGlyphWhite
import com.example.ui.theme.NothingObsidianBlack
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.WarmGold

@Composable
fun LiquidAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height

                val nothingBgBrush = Brush.verticalGradient(
                    colors = listOf(
                        NothingObsidianBlack,
                        Color(0xFF0A0B0F),
                        Color(0xFF101319),
                        Color(0xFF07080C),
                        NothingObsidianBlack
                    )
                )
                val nothingCrimsonOrb = Brush.radialGradient(
                    colors = listOf(
                        NothingCrimsonRed.copy(alpha = 0.25f),
                        NothingBrightRed.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.78f, h * 0.18f),
                    radius = w * 0.70f
                )
                val nothingGlyphOrb = Brush.radialGradient(
                    colors = listOf(
                        NothingGlyphWhite.copy(alpha = 0.14f),
                        Color(0xFF9EA4B0).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.22f, h * 0.42f),
                    radius = w * 0.65f
                )
                val nothingCyanRefractionOrb = Brush.radialGradient(
                    colors = listOf(
                        LiquidCyan.copy(alpha = 0.12f),
                        NothingCrimsonRed.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.52f, h * 0.76f),
                    radius = w * 0.68f
                )

                val galacticBgBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF04060D),
                        Color(0xFF0B111E),
                        Color(0xFF141824),
                        Color(0xFF090D16),
                        Color(0xFF05070E)
                    )
                )
                val galacticGoldOrb = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD97706).copy(alpha = 0.25f),
                        Color(0xFF78350F).copy(alpha = 0.13f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.75f, h * 0.22f),
                    radius = w * 0.72f
                )
                val galacticTealOrb = Brush.radialGradient(
                    colors = listOf(
                        CrystalTeal.copy(alpha = 0.23f),
                        Color(0xFF0F766E).copy(alpha = 0.13f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.28f, h * 0.36f),
                    radius = w * 0.68f
                )
                val galacticCenterOrb = Brush.radialGradient(
                    colors = listOf(
                        SolarAmber.copy(alpha = 0.18f),
                        PrismPurple.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.48f, h * 0.56f),
                    radius = w * 0.75f
                )

                val darkBgBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060918),
                        Color(0xFF0C1430),
                        Color(0xFF10132E),
                        Color(0xFF050814)
                    )
                )
                val darkBlueOrb = Brush.radialGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.26f), Color.Transparent),
                    center = Offset(w * 0.25f, h * 0.14f),
                    radius = w * 0.70f
                )
                val darkPurpleOrb = Brush.radialGradient(
                    colors = listOf(PrismPurple.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(w * 0.80f, h * 0.34f),
                    radius = w * 0.66f
                )
                val darkCyanOrb = Brush.radialGradient(
                    colors = listOf(LiquidCyan.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(w * 0.30f, h * 0.70f),
                    radius = w * 0.62f
                )

                val lightBgBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9AC5F8),
                        Color(0xFFC5D8FF),
                        Color(0xFFDECFF9),
                        Color(0xFFC7DAF8),
                        Color(0xFFD9C7F2)
                    )
                )
                val lightCyanOrb = Brush.radialGradient(
                    colors = listOf(LiquidCyan.copy(alpha = 0.36f), Color.Transparent),
                    center = Offset(w * 0.20f, h * 0.09f),
                    radius = w * 0.65f
                )
                val lightPearlOrb = Brush.radialGradient(
                    colors = listOf(Color(0xFFFDF4FF).copy(alpha = 0.70f), Color.Transparent),
                    center = Offset(w * 0.75f, h * 0.12f),
                    radius = w * 0.58f
                )

                val desertClayBgBrush = Brush.verticalGradient(
                    colors = listOf(
                        DesertClayCreamBase,
                        Color(0xFFF3E4D5),
                        DesertClayWarmSand,
                        Color(0xFFEDD9C6),
                        DesertClayCreamBase
                    )
                )
                val desertWarmSunOrb = Brush.radialGradient(
                    colors = listOf(
                        DesertClayPeachTerracotta.copy(alpha = 0.20f),
                        DesertClaySoftApricot.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.78f, h * 0.18f),
                    radius = w * 0.72f
                )
                val desertIvoryOrb = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.75f),
                        DesertClayCardSurface.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.22f, h * 0.12f),
                    radius = w * 0.65f
                )

                val forestCanopyBgBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF061B14),
                        ForestCanopyDeepGreen,
                        ForestCanopyDarkMoss,
                        Color(0xFF0A281E),
                        Color(0xFF051812)
                    )
                )
                val forestSunbeamOrb = Brush.radialGradient(
                    colors = listOf(
                        ForestGoldenFern.copy(alpha = 0.22f),
                        ForestSageGreen.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.52f, h * 0.22f),
                    radius = w * 0.75f
                )
                val forestMossOrb = Brush.radialGradient(
                    colors = listOf(
                        ForestSageGreen.copy(alpha = 0.20f),
                        ForestCopperBark.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.80f, h * 0.68f),
                    radius = w * 0.68f
                )

                val starRBig = 2.0.dp.toPx()
                val starRMed = 1.25.dp.toPx()
                val starRSmall = 0.85.dp.toPx()
                val dotGridStep = 24.dp.toPx()
                val dotRadius = 1.15.dp.toPx()
                val glyphStroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)

                onDrawBehind {
                    if (glass.isDesertDuneClay) {
                        drawRect(brush = desertClayBgBrush)
                        drawCircle(brush = desertWarmSunOrb, radius = w * 0.72f, center = Offset(w * 0.78f, h * 0.18f))
                        drawCircle(brush = desertIvoryOrb, radius = w * 0.65f, center = Offset(w * 0.22f, h * 0.12f))

                        // Sculpted warm sandstone dune contour waves along edges matching Screenshot 1
                        for (waveIdx in 0..3) {
                            val dunePath = Path()
                            val yBase = h * (0.24f + waveIdx * 0.22f)
                            dunePath.moveTo(0f, yBase)
                            dunePath.cubicTo(
                                w * 0.32f, yBase - 38.dp.toPx(),
                                w * 0.68f, yBase + 42.dp.toPx(),
                                w, yBase - 18.dp.toPx()
                            )
                            drawPath(
                                path = dunePath,
                                color = DesertClayPeachTerracotta.copy(alpha = 0.08f),
                                style = Stroke(width = 28.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    } else if (glass.isEnchantedForestCodex) {
                        drawRect(brush = forestCanopyBgBrush)
                        drawCircle(brush = forestSunbeamOrb, radius = w * 0.75f, center = Offset(w * 0.52f, h * 0.22f))
                        drawCircle(brush = forestMossOrb, radius = w * 0.68f, center = Offset(w * 0.80f, h * 0.68f))

                        // Subtle botanical fern & woodland leaf silhouettes around canopy borders matching Screenshot 2
                        for (i in 0 until 18) {
                            val lx = if (i % 2 == 0) (i * 17 % 28) / 100f * w else w - ((i * 19 % 28) / 100f * w)
                            val ly = ((i * 53 + 7) % 100) / 100f * h
                            drawCircle(
                                color = if (i % 3 == 0) ForestGoldenFern.copy(alpha = 0.16f) else ForestSageGreen.copy(alpha = 0.14f),
                                radius = (8 + (i % 4) * 5).dp.toPx(),
                                center = Offset(lx, ly)
                            )
                        }
                    } else if (glass.isNothingOs) {
                        drawRect(brush = nothingBgBrush)
                        drawCircle(brush = nothingCrimsonOrb, radius = w * 0.70f, center = Offset(w * 0.78f, h * 0.18f))
                        drawCircle(brush = nothingGlyphOrb, radius = w * 0.65f, center = Offset(w * 0.22f, h * 0.42f))
                        drawCircle(brush = nothingCyanRefractionOrb, radius = w * 0.68f, center = Offset(w * 0.52f, h * 0.76f))

                        // Cached Nothing OS Precision Dot-Matrix Grid Overlay (zero allocation in draw loop)
                        val cols = (w / dotGridStep).toInt()
                        val rows = (h / dotGridStep).toInt()
                        for (r in 1..rows) {
                            val py = r * dotGridStep
                            for (c in 1..cols) {
                                val px = c * dotGridStep
                                val isAccentNode = (r + c) % 13 == 0
                                drawCircle(
                                    color = if (isAccentNode) {
                                        NothingCrimsonRed.copy(alpha = 0.32f)
                                    } else {
                                        Color.White.copy(alpha = 0.085f)
                                    },
                                    radius = if (isAccentNode) dotRadius * 1.35f else dotRadius,
                                    center = Offset(px, py)
                                )
                            }
                        }

                        // Subtle Nothing Phone Transparent Backplate Glyph Ring & Diagonal Telemetry Trace
                        val ringCenter = Offset(w * 0.82f, h * 0.20f)
                        val ringRadius = w * 0.28f
                        drawCircle(
                            color = Color.White.copy(alpha = 0.11f),
                            radius = ringRadius,
                            center = ringCenter,
                            style = glyphStroke
                        )
                        drawCircle(
                            color = NothingCrimsonRed.copy(alpha = 0.55f),
                            radius = 4.dp.toPx(),
                            center = Offset(ringCenter.x - ringRadius * 0.707f, ringCenter.y + ringRadius * 0.707f)
                        )
                    } else if (glass.isGalacticCodex) {
                        drawRect(brush = galacticBgBrush)
                        drawCircle(brush = galacticGoldOrb, radius = w * 0.72f, center = Offset(w * 0.75f, h * 0.22f))
                        drawCircle(brush = galacticTealOrb, radius = w * 0.68f, center = Offset(w * 0.28f, h * 0.36f))
                        drawCircle(brush = galacticCenterOrb, radius = w * 0.75f, center = Offset(w * 0.48f, h * 0.56f))

                        // Cached crisp starfield (28 stars, zero per-frame trig calculations)
                        for (i in 0 until 28) {
                            val sx = ((i * 73 + 19) % 100) / 100f * w
                            val sy = ((i * 41 + 11) % 100) / 100f * h
                            val starRadius = if (i % 7 == 0) starRBig else if (i % 3 == 0) starRMed else starRSmall
                            val starColor = when {
                                i % 7 == 0 -> WarmGold.copy(alpha = 0.75f)
                                i % 5 == 0 -> LiquidCyan.copy(alpha = 0.70f)
                                else -> Color.White.copy(alpha = 0.55f)
                            }
                            drawCircle(color = starColor, radius = starRadius, center = Offset(sx, sy))
                        }
                    } else if (glass.isDark) {
                        drawRect(brush = darkBgBrush)
                        drawCircle(brush = darkBlueOrb, radius = w * 0.70f, center = Offset(w * 0.25f, h * 0.14f))
                        drawCircle(brush = darkPurpleOrb, radius = w * 0.66f, center = Offset(w * 0.80f, h * 0.34f))
                        drawCircle(brush = darkCyanOrb, radius = w * 0.62f, center = Offset(w * 0.30f, h * 0.70f))
                    } else {
                        drawRect(brush = lightBgBrush)
                        drawCircle(brush = lightCyanOrb, radius = w * 0.65f, center = Offset(w * 0.20f, h * 0.09f))
                        drawCircle(brush = lightPearlOrb, radius = w * 0.58f, center = Offset(w * 0.75f, h * 0.12f))
                    }
                }
            },
        content = content
    )
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
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    val baseSurface = when {
        glass.isDesertDuneClay -> DesertClayCardSurface.copy(alpha = 0.96f)
        glass.isEnchantedForestCodex -> Color(0xFF0E2F23).copy(alpha = 0.86f)
        glass.isNothingOs -> Color(0xFF10131A).copy(alpha = 0.74f)
        glass.isDark -> Color(0xFF172242).copy(alpha = 0.68f)
        else -> Color.White.copy(alpha = 0.56f)
    }

    val specularTop = when {
        glass.isDesertDuneClay -> Color.White
        glass.isEnchantedForestCodex -> ForestGoldenFern.copy(alpha = 0.35f)
        glass.isNothingOs -> Color.White.copy(alpha = 0.30f)
        glass.isDark -> Color.White.copy(alpha = 0.22f)
        else -> Color.White.copy(alpha = 0.88f)
    }

    val specularBottom = when {
        glass.isDesertDuneClay -> DesertClaySoftApricot.copy(alpha = 0.45f)
        glass.isEnchantedForestCodex -> ForestSageGreen.copy(alpha = 0.30f)
        glass.isNothingOs -> (if (tintColor != Color.Transparent) tintColor else NothingCrimsonRed).copy(alpha = 0.22f)
        glass.isDark -> tintColor.copy(alpha = 0.24f)
        else -> if (tintColor != Color.Transparent) tintColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.35f)
    }

    val borderBrush = remember(glass.isDark, glass.isNothingOs, glass.isDesertDuneClay, glass.isEnchantedForestCodex, tintColor, borderAlpha) {
        when {
            glass.isDesertDuneClay -> Brush.linearGradient(
                colors = listOf(
                    Color.White,
                    DesertClaySoftApricot.copy(alpha = 0.75f),
                    Color.White.copy(alpha = 0.90f)
                )
            )
            glass.isEnchantedForestCodex -> Brush.linearGradient(
                colors = listOf(
                    ForestSageGreen.copy(alpha = 0.75f * borderAlpha),
                    ForestGoldenFern.copy(alpha = 0.55f * borderAlpha),
                    ForestCopperBark.copy(alpha = 0.45f * borderAlpha)
                )
            )
            glass.isNothingOs -> Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.76f * borderAlpha),
                    NothingCrimsonRed.copy(alpha = 0.60f * borderAlpha),
                    Color.White.copy(alpha = 0.28f * borderAlpha)
                )
            )
            else -> Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (glass.isDark) 0.52f * borderAlpha else 0.95f * borderAlpha),
                    if (tintColor != Color.Transparent) tintColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = if (glass.isDark) 0.20f * borderAlpha else 0.65f * borderAlpha)
                )
            )
        }
    }

    val fillBrush = remember(glass.isDark, glass.isNothingOs, glass.isDesertDuneClay, glass.isEnchantedForestCodex, tintColor, baseSurface) {
        when {
            glass.isDesertDuneClay -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFCF9),
                    DesertClayCardSurface,
                    DesertClayCardElevated
                )
            )
            glass.isEnchantedForestCodex -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF12382A).copy(alpha = 0.88f),
                    Color(0xFF0C2A1F).copy(alpha = 0.92f),
                    Color(0xFF082017).copy(alpha = 0.94f)
                )
            )
            glass.isNothingOs -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF171A23).copy(alpha = 0.78f),
                    if (tintColor != Color.Transparent) tintColor.copy(alpha = 0.16f) else Color(0xFF11131A).copy(alpha = 0.76f),
                    Color(0xFF0A0C10).copy(alpha = 0.82f)
                )
            )
            else -> Brush.linearGradient(
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
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "glass_panel_press_scale"
    )

    val clickableModifier = if (onClick != null) {
        Modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    } else {
        Modifier
    }

    val isNothingMode = glass.isNothingOs

    Box(
        modifier = modifier
            .then(clickableModifier)
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = when {
                    isNothingMode -> NothingCrimsonRed.copy(alpha = 0.30f)
                    tintColor != Color.Transparent -> tintColor.copy(alpha = 0.35f)
                    else -> ElectricBlue.copy(alpha = 0.18f)
                },
                spotColor = when {
                    isNothingMode -> Color.White.copy(alpha = 0.22f)
                    tintColor != Color.Transparent -> tintColor.copy(alpha = 0.35f)
                    else -> PrismViolet.copy(alpha = 0.22f)
                }
            )
            .clip(shape)
            .background(brush = fillBrush, shape = shape)
            .drawWithCache {
                val sheenBrush = Brush.verticalGradient(
                    colors = listOf(specularTop.copy(alpha = 0.32f), Color.Transparent, specularBottom.copy(alpha = 0.14f))
                )
                val cr = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
                val ledR = 2.4.dp.toPx()
                val padEdge = 12.dp.toPx()
                onDrawWithContent {
                    drawRoundRect(
                        brush = sheenBrush,
                        cornerRadius = cr
                    )
                    if (isNothingMode && size.width > 90.dp.toPx() && size.height > 54.dp.toPx()) {
                        // Signature Nothing OS corner telemetry dot-LED & subtle glyph hairline
                        drawCircle(
                            color = NothingCrimsonRed.copy(alpha = 0.72f),
                            radius = ledR,
                            center = Offset(size.width - padEdge, padEdge)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.38f),
                            radius = ledR * 0.72f,
                            center = Offset(size.width - padEdge - 8.dp.toPx(), padEdge)
                        )
                    }
                    drawContent()
                }
            }
            .border(width = if (isNothingMode) 1.35.dp else 1.3.dp, brush = borderBrush, shape = shape),
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "circular_btn_scale"
    )

    val fillBrush = remember(glass.isDark) {
        Brush.linearGradient(
            colors = if (glass.isDark) {
                listOf(Color.White.copy(alpha = 0.22f), Color(0xFF1E293B).copy(alpha = 0.65f))
            } else {
                listOf(Color.White.copy(alpha = 0.78f), Color.White.copy(alpha = 0.42f))
            }
        )
    }

    val borderBrush = remember(glass.isDark, accentGlow) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (glass.isDark) 0.65f else 0.95f),
                accentGlow.copy(alpha = 0.35f)
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = accentGlow.copy(alpha = 0.25f),
                spotColor = accentGlow.copy(alpha = 0.28f)
            )
            .clip(CircleShape)
            .background(brush = fillBrush)
            .border(
                width = 1.2.dp,
                brush = borderBrush,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = resolvedTint,
            modifier = Modifier.size(if (size <= 40.dp) 19.dp else 22.dp)
        )
    }
}

@Composable
fun FloatingGlassNavigationBar(
    currentTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    selectionProgress: Float = currentTab.ordinal.toFloat(),
    scrollCompressionProgress: Float = 0f
) {
    val glass = LocalGlassColors.current
    val outerCapsuleShape = RoundedCornerShape(34.dp)
    val innerSelectionShape = RoundedCornerShape(26.dp)

    val items = remember {
        listOf(
            NavTabSpec(MainTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home, ElectricBlue, "nav_home"),
            NavTabSpec(MainTab.TOOLS, "Tools", Icons.Filled.Build, Icons.Outlined.Build, PrismViolet, "nav_tools"),
            NavTabSpec(MainTab.LIBRARY, "Library", Icons.Filled.FolderSpecial, Icons.Outlined.FolderCopy, CrystalTeal, "nav_library"),
            NavTabSpec(MainTab.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, IridescentPink, "nav_settings")
        )
    }

    // Smooth high-stiffness spring-driven indicator position for instant responsiveness
    val animatedTabIndex by animateFloatAsState(
        targetValue = selectionProgress.coerceIn(0f, 3f),
        animationSpec = spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessHigh
        ),
        label = "liquid_capsule_slide"
    )

    val refractionPhase = 0.45f

    val activeAccentColor by animateColorAsState(
        targetValue = when {
            glass.isDesertDuneClay -> DesertClayPeachTerracotta
            glass.isEnchantedForestCodex -> ForestSageGreen
            glass.isNothingOs -> NothingCrimsonRed
            glass.isGalacticCodex -> WarmGold
            else -> when (currentTab) {
                MainTab.HOME -> ElectricBlue
                MainTab.TOOLS -> PrismViolet
                MainTab.LIBRARY -> CrystalTeal
                MainTab.SETTINGS -> IridescentPink
            }
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_active_accent"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Ambient Light Halo & Soft Floating Shadow Separation beneath the Glass Pill
        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .height(66.dp)
                .drawBehind {
                    // Soft ambient shadow + subtle light halo around the floating glass
                    val activeCenterFraction = (animatedTabIndex + 0.5f) / 4f
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeAccentColor.copy(alpha = if (glass.isDark) 0.28f else 0.20f),
                                LiquidCyan.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * activeCenterFraction, size.height * 0.65f),
                            radius = size.width * 0.55f
                        ),
                        cornerRadius = CornerRadius(42.dp.toPx(), 42.dp.toPx())
                    )
                }
                .shadow(
                    elevation = 24.dp,
                    shape = outerCapsuleShape,
                    ambientColor = Color.Black.copy(alpha = 0.55f),
                    spotColor = activeAccentColor.copy(alpha = 0.48f)
                )
                .clip(outerCapsuleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = when {
                            glass.isDesertDuneClay -> listOf(
                                Color(0xFFFDF8F3).copy(alpha = 0.96f),
                                Color(0xFFEEE0D3).copy(alpha = 0.98f)
                            )
                            glass.isEnchantedForestCodex -> listOf(
                                ForestParchmentLight.copy(alpha = 0.96f),
                                ForestParchmentWarm.copy(alpha = 0.96f)
                            )
                            glass.isNothingOs -> listOf(
                                Color(0xFF141720).copy(alpha = 0.84f),
                                Color(0xFF07080B).copy(alpha = 0.90f)
                            )
                            glass.isGalacticCodex -> listOf(
                                Color(0xFF281F14).copy(alpha = 0.82f),
                                Color(0xFF120E09).copy(alpha = 0.88f)
                            )
                            glass.isDark -> listOf(
                                Color(0xFF1A2238).copy(alpha = 0.66f),
                                Color(0xFF0E1324).copy(alpha = 0.74f)
                            )
                            else -> listOf(
                                Color(0xFFF8FAFC).copy(alpha = 0.72f),
                                Color(0xFFE2E8F0).copy(alpha = 0.78f)
                            )
                        }
                    )
                )
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val activeCenterX = w * ((animatedTabIndex + 0.5f) / 4f)

                    if (glass.isNothingOs) {
                        // Nothing OS Glyph LED light strip + precision dot-matrix ticks along the bottom dock
                        val stepPx = 12.dp.toPx()
                        val dotCols = (w / stepPx).toInt()
                        for (c in 2 until dotCols - 1) {
                            val dx = c * stepPx
                            val prox = (1f - kotlin.math.abs(dx - activeCenterX) / (w * 0.25f)).coerceIn(0f, 1f)
                            drawCircle(
                                color = if (prox > 0.45f) {
                                    NothingCrimsonRed.copy(alpha = 0.25f + 0.45f * prox)
                                } else {
                                    Color.White.copy(alpha = 0.08f)
                                },
                                radius = if (prox > 0.45f) 1.35.dp.toPx() else 0.95.dp.toPx(),
                                center = Offset(dx, h * 0.14f)
                            )
                        }
                    } else if (glass.isGalacticCodex) {
                        // Flowing Golden Light-Wave Ribbons across the capsule matching the Galactic Codex reference photo
                        for (ribbonIdx in 0..4) {
                            val ribbonPath = Path()
                            val phaseShift = ribbonIdx * 0.48f + refractionPhase * 1.4f
                            val amp = h * (0.14f + ribbonIdx * 0.025f)
                            val yMid = h * (0.46f + (ribbonIdx - 2) * 0.045f)
                            ribbonPath.moveTo(0f, yMid + sin(phaseShift) * amp * 0.5f)
                            ribbonPath.cubicTo(
                                w * 0.28f,
                                yMid - cos(phaseShift + 0.9f) * amp,
                                w * 0.68f,
                                yMid + sin(phaseShift + 1.8f) * amp,
                                w,
                                yMid - cos(phaseShift + 2.5f) * amp * 0.5f
                            )
                            drawPath(
                                path = ribbonPath,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        SolarAmber.copy(alpha = 0.36f),
                                        WarmGold.copy(alpha = 0.68f),
                                        Color(0xFFFFFBEB).copy(alpha = 0.55f),
                                        SolarAmber.copy(alpha = 0.36f),
                                        Color.Transparent
                                    )
                                ),
                                style = Stroke(width = (1.6f + (ribbonIdx % 2) * 0.8f).dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    // 1. Subtle background color distortion & chromatic refraction visible through the frosted glass
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                activeAccentColor.copy(alpha = if (glass.isDark) 0.22f else 0.16f),
                                LiquidCyan.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(activeCenterX, h * 0.52f),
                            radius = h * 1.35f
                        ),
                        radius = h * 1.35f,
                        center = Offset(activeCenterX, h * 0.52f)
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrismPurple.copy(alpha = if (glass.isDark) 0.14f else 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.22f + 0.55f * refractionPhase), h * 0.3f),
                            radius = h * 1.1f
                        ),
                        radius = h * 1.1f,
                        center = Offset(w * (0.22f + 0.55f * refractionPhase), h * 0.3f)
                    )

                    // 2. Soft top inner specular highlight across the glass curvature
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (glass.isDark) 0.18f else 0.65f),
                                Color.Transparent,
                                Color.Black.copy(alpha = if (glass.isDark) 0.16f else 0.03f)
                            )
                        ),
                        cornerRadius = CornerRadius(38.dp.toPx(), 38.dp.toPx())
                    )
                }
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (glass.isDark) 0.42f else 0.92f),
                            Color.White.copy(alpha = if (glass.isDark) 0.14f else 0.55f),
                            activeAccentColor.copy(alpha = 0.36f),
                            Color.White.copy(alpha = if (glass.isDark) 0.28f else 0.85f)
                        )
                    ),
                    shape = outerCapsuleShape
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val slotWidth = maxWidth / items.size
                val slotWidthPx = constraints.maxWidth.toFloat() / items.size

                // Physically Sliding Translucent Liquid-Glass Selection Capsule Underneath the Active Item
                // Uses lambda .offset { IntOffset(...) } to execute in the layout phase at 165Hz with zero recomposition
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (slotWidthPx * animatedTabIndex).roundToInt(),
                                y = 0
                            )
                        }
                        .width(slotWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = innerSelectionShape,
                            ambientColor = activeAccentColor.copy(alpha = 0.45f),
                            spotColor = LiquidCyan.copy(alpha = 0.40f)
                        )
                        .clip(innerSelectionShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (glass.isDark) {
                                    listOf(
                                        Color.White.copy(alpha = 0.20f),
                                        activeAccentColor.copy(alpha = 0.24f),
                                        Color.White.copy(alpha = 0.08f)
                                    )
                                } else {
                                    listOf(
                                        Color.White.copy(alpha = 0.92f),
                                        activeAccentColor.copy(alpha = 0.18f),
                                        Color.White.copy(alpha = 0.78f)
                                    )
                                }
                            )
                        )
                        .drawBehind {
                            // Internal lens refraction & top crest highlight on the sliding selection capsule
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (glass.isDark) 0.26f else 0.65f),
                                        activeAccentColor.copy(alpha = 0.16f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.5f, size.height * 0.18f),
                                    radius = size.width * 0.72f
                                ),
                                cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx())
                            )
                        }
                        .border(
                            width = 1.1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (glass.isDark) 0.65f else 0.98f),
                                    activeAccentColor.copy(alpha = 0.42f),
                                    Color.White.copy(alpha = if (glass.isDark) 0.22f else 0.65f)
                                )
                            ),
                            shape = innerSelectionShape
                        )
                )

                // 4 Navigation Destinations: Home | Tools | Library | Settings
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = currentTab == item.tab
                        val itemInteractionSource = remember { MutableInteractionSource() }
                        val isPressed by itemInteractionSource.collectIsPressedAsState()

                        // Touch micro-interaction: 0.96x scale on press, smooth spring return to 1.0x
                        val pressScale by animateFloatAsState(
                            targetValue = if (isPressed) 0.96f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "nav_item_press_scale_$index"
                        )

                        // Proximity brightness based on the sliding capsule position
                        val distance = kotlin.math.abs(animatedTabIndex - index).coerceIn(0f, 1f)
                        val selectionFraction = 1f - distance

                        val itemTint by animateColorAsState(
                            targetValue = when {
                                glass.isDesertDuneClay && isSelected -> DesertClayCocoaBrown
                                glass.isDesertDuneClay -> DesertClayMutedBrown
                                glass.isEnchantedForestCodex && isSelected -> ForestDeepMossText
                                glass.isEnchantedForestCodex -> if (index == 1) ForestCopperBark else ForestSageGreen
                                glass.isGalacticCodex && isSelected -> Color(0xFFFFFBEB)
                                glass.isGalacticCodex -> Color(0xFFFDE68A).copy(alpha = 0.88f)
                                isSelected && glass.isDark -> Color.White
                                isSelected && !glass.isDark -> Color(0xFF0A4DA2)
                                glass.isDark -> Color(0xFF94A3B8).copy(alpha = 0.78f)
                                else -> Color(0xFF475569).copy(alpha = 0.80f)
                            },
                            animationSpec = tween(durationMillis = 220),
                            label = "nav_item_tint_$index"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(
                                    horizontal = if (glass.isGalacticCodex || glass.isDesertDuneClay) 4.dp else 0.dp,
                                    vertical = if (glass.isGalacticCodex || glass.isDesertDuneClay) 2.dp else 0.dp
                                )
                                .graphicsLayer {
                                    scaleX = pressScale
                                    scaleY = pressScale
                                }
                                .clip(
                                    if (glass.isGalacticCodex || glass.isDesertDuneClay) RoundedCornerShape(18.dp)
                                    else innerSelectionShape
                                )
                                .then(
                                    when {
                                        glass.isDesertDuneClay -> {
                                            Modifier
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        colors = if (isSelected) {
                                                            listOf(
                                                                DesertClaySoftApricot,
                                                                DesertClayPeachTerracotta
                                                            )
                                                        } else {
                                                            listOf(
                                                                Color(0xFFFDF8F3),
                                                                Color(0xFFEDE0D4)
                                                            )
                                                        }
                                                    )
                                                )
                                                .border(
                                                    width = 1.3.dp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.75f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                        }
                                        glass.isGalacticCodex -> {
                                            Modifier
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFF5C4424).copy(alpha = if (isSelected) 0.78f else 0.52f),
                                                            Color(0xFF2B1E10).copy(alpha = if (isSelected) 0.88f else 0.68f)
                                                        )
                                                    )
                                                )
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color(0xFFFFFBEB).copy(alpha = if (isSelected) 0.95f else 0.65f),
                                                            WarmGold.copy(alpha = if (isSelected) 0.85f else 0.45f),
                                                            Color(0xFF92400E).copy(alpha = 0.55f)
                                                        )
                                                    ),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                        }
                                        else -> Modifier
                                    }
                                )
                                .testTag(item.testTag)
                                .clickable(
                                    interactionSource = itemInteractionSource,
                                    indication = null,
                                    onClick = { onSelectTab(item.tab) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // High-resolution anti-aliased custom vector icon with subtle active halo
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .drawBehind {
                                            if (selectionFraction > 0.05f) {
                                                drawCircle(
                                                    brush = Brush.radialGradient(
                                                        colors = listOf(
                                                            item.glowColor.copy(alpha = 0.34f * selectionFraction),
                                                            Color.Transparent
                                                        ),
                                                        center = center,
                                                        radius = size.minDimension * 0.88f
                                                    ),
                                                    radius = size.minDimension * 0.88f
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    LiquidNavVectorIcon(
                                        tab = item.tab,
                                        tint = itemTint,
                                        selectionFraction = selectionFraction,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.5.sp,
                                        letterSpacing = if (isSelected) 0.35.sp else 0.2.sp
                                    ),
                                    color = itemTint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-resolution anti-aliased vector icons for the 4 Paperflow destinations:
 * 1. HOME: Minimal modern house with rounded roof and warm doorway arch
 * 2. TOOLS: Professional studio sliders/faders icon with glowing control knobs
 * 3. LIBRARY: Clean layered book / document collection folio icon
 * 4. SETTINGS: Minimal precision gear / control ring icon
 */
@Composable
private fun LiquidNavVectorIcon(
    tab: MainTab,
    tint: Color,
    selectionFraction: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = (1.85f + 0.35f * selectionFraction).dp.toPx()
        val strokeStyle = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (tab) {
            MainTab.HOME -> {
                // Minimal modern house silhouette
                val housePath = Path().apply {
                    moveTo(w * 0.14f, h * 0.46f)
                    lineTo(w * 0.50f, h * 0.14f)
                    lineTo(w * 0.86f, h * 0.46f)
                }
                drawPath(path = housePath, color = tint, style = strokeStyle)

                val wallsPath = Path().apply {
                    moveTo(w * 0.24f, h * 0.42f)
                    lineTo(w * 0.24f, h * 0.84f)
                    lineTo(w * 0.76f, h * 0.84f)
                    lineTo(w * 0.76f, h * 0.42f)
                }
                drawPath(path = wallsPath, color = tint, style = strokeStyle)

                // Subtle glowing doorway pill
                drawRoundRect(
                    color = tint.copy(alpha = 0.22f * selectionFraction),
                    topLeft = Offset(w * 0.24f, h * 0.42f),
                    size = Size(w * 0.52f, h * 0.42f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.50f, h * 0.84f),
                    end = Offset(w * 0.50f, h * 0.60f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            MainTab.TOOLS -> {
                // Professional 3-track studio sliders/controls icon
                val y1 = h * 0.25f
                val y2 = h * 0.50f
                val y3 = h * 0.75f
                val k1 = w * (0.34f + 0.04f * selectionFraction)
                val k2 = w * (0.68f - 0.04f * selectionFraction)
                val k3 = w * (0.42f + 0.03f * selectionFraction)
                val r = 2.6.dp.toPx()

                // Track 1
                drawLine(tint, Offset(w * 0.14f, y1), Offset(k1 - r, y1), strokeWidth, StrokeCap.Round)
                drawLine(tint.copy(alpha = 0.6f), Offset(k1 + r, y1), Offset(w * 0.86f, y1), strokeWidth, StrokeCap.Round)
                drawCircle(color = tint, radius = r, center = Offset(k1, y1), style = Stroke(width = strokeWidth * 0.9f))

                // Track 2
                drawLine(tint, Offset(w * 0.14f, y2), Offset(k2 - r, y2), strokeWidth, StrokeCap.Round)
                drawLine(tint.copy(alpha = 0.6f), Offset(k2 + r, y2), Offset(w * 0.86f, y2), strokeWidth, StrokeCap.Round)
                drawCircle(color = tint, radius = r, center = Offset(k2, y2), style = Stroke(width = strokeWidth * 0.9f))

                // Track 3
                drawLine(tint, Offset(w * 0.14f, y3), Offset(k3 - r, y3), strokeWidth, StrokeCap.Round)
                drawLine(tint.copy(alpha = 0.6f), Offset(k3 + r, y3), Offset(w * 0.86f, y3), strokeWidth, StrokeCap.Round)
                drawCircle(color = tint, radius = r, center = Offset(k3, y3), style = Stroke(width = strokeWidth * 0.9f))
            }

            MainTab.LIBRARY -> {
                // Clean open book / document folio icon
                val bookPath = Path().apply {
                    // Left page
                    moveTo(w * 0.50f, h * 0.24f)
                    cubicTo(w * 0.38f, h * 0.18f, w * 0.22f, h * 0.18f, w * 0.14f, h * 0.24f)
                    lineTo(w * 0.14f, h * 0.78f)
                    cubicTo(w * 0.22f, h * 0.72f, w * 0.38f, h * 0.72f, w * 0.50f, h * 0.78f)
                    // Right page
                    cubicTo(w * 0.62f, h * 0.72f, w * 0.78f, h * 0.72f, w * 0.86f, h * 0.78f)
                    lineTo(w * 0.86f, h * 0.24f)
                    cubicTo(w * 0.78f, h * 0.18f, w * 0.62f, h * 0.18f, w * 0.50f, h * 0.24f)
                }
                if (selectionFraction > 0.1f) {
                    drawPath(
                        path = bookPath,
                        color = tint.copy(alpha = 0.18f * selectionFraction)
                    )
                }
                drawPath(path = bookPath, color = tint, style = strokeStyle)
                drawLine(
                    color = tint,
                    start = Offset(w * 0.50f, h * 0.24f),
                    end = Offset(w * 0.50f, h * 0.78f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            MainTab.SETTINGS -> {
                // Minimal precision gear icon
                val cx = w * 0.5f
                val cy = h * 0.5f
                val outerRadius = w * 0.28f
                val innerRadius = w * 0.13f

                if (selectionFraction > 0.1f) {
                    drawCircle(
                        color = tint.copy(alpha = 0.18f * selectionFraction),
                        radius = outerRadius,
                        center = Offset(cx, cy)
                    )
                }

                drawCircle(
                    color = tint,
                    radius = outerRadius,
                    center = Offset(cx, cy),
                    style = strokeStyle
                )
                drawCircle(
                    color = tint,
                    radius = innerRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth * 0.9f)
                )

                val teeth = 8
                for (i in 0 until teeth) {
                    val angle = (2.0 * PI * i) / teeth
                    val cosA = cos(angle).toFloat()
                    val sinA = sin(angle).toFloat()
                    drawLine(
                        color = tint,
                        start = Offset(cx + cosA * (outerRadius + 0.5.dp.toPx()), cy + sinA * (outerRadius + 0.5.dp.toPx())),
                        end = Offset(cx + cosA * (w * 0.39f), cy + sinA * (h * 0.39f)),
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
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

/**
 * Play Store / Android System Expressive 10-Lobed Organic Scalloped Blob Spinner.
 *
 * Matches the reference screenshot:
 * - Soft light-blue fill (#8EC5FF)
 * - Smooth rounded 10-lobed organic scalloped silhouette (no text, percentage, or circular ring)
 * - Hardware-accelerated continuous gentle rotation + subtle scale breathing + organic lobe morphing
 * - Crisp vector anti-aliasing on high-resolution AMOLED displays
 */
@Composable
fun PlayStoreOrganicBlobSpinner(
    modifier: Modifier = Modifier,
    indicatorSize: Dp = 38.dp,
    blobColor: Color = Color(0xFF8EC5FF),
    lobes: Int = 10
) {
    val infiniteTransition = rememberInfiniteTransition(label = "play_store_blob_transition")

    // Seamless infinite rotation (1000 ms per cycle, right in the center of the 800–1200 ms spec)
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blob_rotation"
    )

    // Subtle scale breathing / pulsing (980 ms per cycle, within the 800–1200 ms spec)
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 980, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob_scale_pulse"
    )

    // Subtle organic lobe depth morphing (1040 ms per cycle, within the 800–1200 ms spec)
    val lobeMorph by infiniteTransition.animateFloat(
        initialValue = 0.068f,
        targetValue = 0.098f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1040, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob_lobe_morph"
    )

    val reusablePath = remember { Path() }

    Canvas(
        modifier = modifier
            .size(indicatorSize)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                rotationZ = rotationDegrees
                scaleX = scalePulse
                scaleY = scalePulse
            }
            .testTag("play_store_organic_blob_spinner")
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val baseRadius = (minOf(size.width, size.height) / 2f) * 0.86f
        val amplitude = baseRadius * lobeMorph

        // High-density 240-point polar sampling with smooth harmonic scallop for ultra-crisp AMOLED anti-aliasing
        val steps = 240
        reusablePath.reset()
        for (i in 0 until steps) {
            val theta = (2.0 * PI * i) / steps
            val r = baseRadius + amplitude * cos(lobes * theta).toFloat()
            val x = cx + r * cos(theta).toFloat()
            val y = cy + r * sin(theta).toFloat()
            if (i == 0) {
                reusablePath.moveTo(x, y)
            } else {
                reusablePath.lineTo(x, y)
            }
        }
        reusablePath.close()

        drawPath(
            path = reusablePath,
            color = blobColor
        )
    }
}

/**
 * Full-screen transparent Android / Google Play Store–style system loading & pull-to-refresh overlay.
 *
 * - Keeps a 100% transparent background (`Color.Transparent`) so the underlying app UI,
 *   Android status bar, and navigation bar remain completely visible.
 * - Shows interactive pull-down feedback (live scaling & rotation of the 10-lobed `#8EC5FF` organic
 *   scalloped blob as the user drags downward from the top of any screen).
 * - Transitions smoothly into the centered spinning 10-lobed Play Store blob during active refresh,
 *   startup, or tool execution, then fades + scales out cleanly when complete.
 */
@Composable
fun PlayStoreSystemLoadingOverlay(
    visible: Boolean,
    pullProgress: Float = 0f,
    modifier: Modifier = Modifier
) {
    val clampedPull = pullProgress.coerceIn(0f, 1.35f)
    val showPullPreview = !visible && clampedPull > 0.04f

    // 1. Live interactive pull-down preview (follows finger drag before release/trigger)
    if (showPullPreview) {
        val pullAlpha = (clampedPull / 0.85f).coerceIn(0f, 1f)
        val pullScale = (0.45f + 0.60f * clampedPull).coerceIn(0.45f, 1.08f)
        val pullOffsetDp = (28f + 64f * clampedPull).dp

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = pullOffsetDp)
                    .graphicsLayer {
                        alpha = pullAlpha
                        scaleX = pullScale
                        scaleY = pullScale
                        rotationZ = clampedPull * 160f
                    }
            ) {
                PlayStoreOrganicBlobSpinner(
                    indicatorSize = 38.dp,
                    blobColor = Color(0xFF8EC5FF),
                    lobes = 10
                )
            }
        }
    }

    // 2. Active Google Play Store–style transparent loading & refresh state
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 150)) +
            scaleIn(
                initialScale = 0.82f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            ),
        exit = fadeOut(animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)) +
            scaleOut(
                targetScale = 0.76f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            ),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .testTag("play_store_loading_screen"),
            contentAlignment = Alignment.Center
        ) {
            PlayStoreOrganicBlobSpinner(
                indicatorSize = 40.dp,
                blobColor = Color(0xFF8EC5FF),
                lobes = 10
            )
        }
    }
}

/**
 * 3D Refractive Liquid Glass Capsule Dropdown Menu matching Photo (2):
 * - Sculpted 34.dp rounded capsule silhouette with curved optical dome sheen
 * - Prismatic rainbow / cyan / gold / crimson chromatic aberration rim lighting
 * - Ambient colour refraction orbs (crimson, violet-magenta, emerald-cyan) glowing through frosted dark glass
 * - Subtle horizontal glass shelf dividers between items
 */
@Composable
fun LiquidGlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val capsuleRadius = 34.dp
    val capsuleShape = remember { RoundedCornerShape(capsuleRadius) }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        shape = capsuleShape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .widthIn(min = 248.dp, max = 300.dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 28.dp,
                    shape = capsuleShape,
                    ambientColor = LiquidCyan.copy(alpha = 0.55f),
                    spotColor = PrismPurple.copy(alpha = 0.65f)
                )
                .clip(capsuleShape)
                .drawWithCache {
                    val w = size.width
                    val h = size.height
                    val rPx = capsuleRadius.toPx()

                    // Deep smoky translucent glass core
                    val baseFillBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3E3A44).copy(alpha = 0.92f),
                            Color(0xFF28222C).copy(alpha = 0.90f),
                            Color(0xFF211C27).copy(alpha = 0.91f),
                            Color(0xFF1A1822).copy(alpha = 0.93f),
                            Color(0xFF2B2A35).copy(alpha = 0.94f)
                        )
                    )

                    // Top frosted silver-white curved dome reflection
                    val topDomeBrush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.44f),
                            Color(0xFFE2E8F0).copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.52f, h * 0.03f),
                        radius = w * 0.88f
                    )

                    // Diagonal curved glass specular highlight across upper half
                    val diagonalGlossPath = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.36f)
                        cubicTo(
                            w * 0.68f, h * 0.24f,
                            w * 0.32f, h * 0.16f,
                            0f, h * 0.08f
                        )
                        close()
                    }
                    val diagonalGlossBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.28f),
                            Color.White.copy(alpha = 0.09f),
                            Color.Transparent
                        ),
                        start = Offset(w * 0.15f, 0f),
                        end = Offset(w * 0.85f, h * 0.34f)
                    )

                    // Warm crimson-coral refraction bleed (upper-left / middle-left)
                    val crimsonRefractionOrb = Brush.radialGradient(
                        colors = listOf(
                            NothingCrimsonRed.copy(alpha = 0.36f),
                            Color(0xFFFB7185).copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.24f, h * 0.34f),
                        radius = w * 0.68f
                    )

                    // Vibrant violet-magenta refraction bleed (lower-left / center)
                    val violetRefractionOrb = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFD946EF).copy(alpha = 0.34f),
                            PrismPurple.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.32f, h * 0.68f),
                        radius = w * 0.66f
                    )

                    // Emerald-cyan refraction bleed (lower-right)
                    val emeraldCyanRefractionOrb = Brush.radialGradient(
                        colors = listOf(
                            EmeraldGreen.copy(alpha = 0.34f),
                            LiquidCyan.copy(alpha = 0.24f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.78f, h * 0.66f),
                        radius = w * 0.68f
                    )

                    // Bottom frosted silver-cyan upward reflection
                    val bottomBounceBrush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.34f),
                            LiquidCyan.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.50f, h * 0.99f),
                        radius = w * 0.80f
                    )

                    // Left & Right inner 3D glass thickness bevel highlights
                    val sideBevelBrush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.00f to LiquidCyan.copy(alpha = 0.38f),
                            0.04f to Color.White.copy(alpha = 0.16f),
                            0.12f to Color.Transparent,
                            0.88f to Color.Transparent,
                            0.96f to Color.White.copy(alpha = 0.18f),
                            1.00f to LiquidCyan.copy(alpha = 0.42f)
                        )
                    )

                    // Outer chromatic aberration prismatic rim border (cyan + white + amber/gold + magenta + cyan)
                    val prismaticOuterRimBrush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0.00f to LiquidCyan.copy(alpha = 0.95f),
                            0.08f to WarmGold.copy(alpha = 0.85f),
                            0.18f to LiquidCyan.copy(alpha = 0.95f),
                            0.28f to Color.White.copy(alpha = 0.90f),
                            0.42f to Color(0xFFF472B6).copy(alpha = 0.80f),
                            0.55f to LiquidCyan.copy(alpha = 0.92f),
                            0.72f to Color.White.copy(alpha = 0.96f),
                            0.86f to LiquidCyan.copy(alpha = 0.92f),
                            1.00f to LiquidCyan.copy(alpha = 0.95f)
                        ),
                        center = Offset(w * 0.5f, h * 0.5f)
                    )

                    // Inner crisp specular white rim border for double-walled glass depth
                    val innerSpecularRimBrush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.White.copy(alpha = 0.88f),
                            0.20f to Color.White.copy(alpha = 0.35f),
                            0.50f to Color.White.copy(alpha = 0.22f),
                            0.80f to Color.White.copy(alpha = 0.38f),
                            1.00f to Color.White.copy(alpha = 0.85f)
                        )
                    )

                    val outerStrokeWidth = 2.4.dp.toPx()
                    val innerStrokeWidth = 1.15.dp.toPx()
                    val innerInset = 3.2.dp.toPx()

                    onDrawWithContent {
                        // 1. Base smoky translucent glass body
                        drawRoundRect(
                            brush = baseFillBrush,
                            cornerRadius = CornerRadius(rPx, rPx)
                        )

                        // 2. Internal chromatic refraction light pools
                        drawRect(brush = crimsonRefractionOrb)
                        drawRect(brush = violetRefractionOrb)
                        drawRect(brush = emeraldCyanRefractionOrb)

                        // 3. Top dome & bottom bounce reflections + side bevels
                        drawRect(brush = sideBevelBrush)
                        drawRect(brush = topDomeBrush)
                        drawPath(path = diagonalGlossPath, brush = diagonalGlossBrush)
                        drawRect(brush = bottomBounceBrush)

                        // 4. Draw menu items and internal glass shelf dividers
                        drawContent()

                        // 5. Inner 3D glass wall highlight
                        drawRoundRect(
                            brush = innerSpecularRimBrush,
                            topLeft = Offset(innerInset, innerInset),
                            size = Size(w - innerInset * 2f, h - innerInset * 2f),
                            cornerRadius = CornerRadius(
                                (rPx - innerInset).coerceAtLeast(8f),
                                (rPx - innerInset).coerceAtLeast(8f)
                            ),
                            style = Stroke(width = innerStrokeWidth)
                        )

                        // 6. Outer chromatic prismatic rim
                        drawRoundRect(
                            brush = prismaticOuterRimBrush,
                            topLeft = Offset(outerStrokeWidth * 0.5f, outerStrokeWidth * 0.5f),
                            size = Size(w - outerStrokeWidth, h - outerStrokeWidth),
                            cornerRadius = CornerRadius(rPx, rPx),
                            style = Stroke(width = outerStrokeWidth)
                        )
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(IntrinsicSize.Max)
                    .padding(vertical = 8.dp),
                content = content
            )
        }
    }
}

/**
 * Individual row inside [LiquidGlassDropdownMenu] with bold clean sans-serif typography
 * and a subtle refractive glass shelf divider line along the bottom when [showDivider] is true.
 */
@Composable
fun LiquidGlassDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    leadingIcon: (@Composable () -> Unit)? = null,
    showDivider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        DropdownMenuItem(
            text = {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp,
                        letterSpacing = 0.1.sp
                    ),
                    color = textColor
                )
            },
            leadingIcon = leadingIcon,
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 11.dp),
            colors = MenuDefaults.itemColors(
                textColor = textColor,
                leadingIconColor = textColor
            )
        )
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(1.5.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.White.copy(alpha = 0.04f),
                                0.15f to Color.White.copy(alpha = 0.18f),
                                0.50f to Color.White.copy(alpha = 0.24f),
                                0.85f to Color.White.copy(alpha = 0.18f),
                                1.00f to Color.White.copy(alpha = 0.04f)
                            )
                        )
                    )
            )
        }
    }
}


