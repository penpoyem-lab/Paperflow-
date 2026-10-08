package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppSettings
import com.example.data.AppStyleOption
import com.example.data.ThemeModeOption
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NothingBrightRed
import com.example.ui.theme.NothingCrimsonRed
import com.example.ui.theme.NothingDotMatrixFamily
import com.example.ui.theme.NothingGlyphWhite
import com.example.ui.theme.NothingObsidianBlack
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.WarmGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val NothingPureBlack = Color(0xFF0A0A0C)
private val NothingCardFill = Color(0xFF17181B)
private val NothingCardBorder = Color(0xFF282A2E)
private val NothingDividerLine = Color(0xFF232529)
private val NothingRedAccent = Color(0xFFE51923)
private val NothingRadioUnselected = Color(0xFF6B6F76)
private val NothingIconGrey = Color(0xFFC9CDD4)
private val NothingHeaderButtonFill = Color(0xFF151619)
private val NothingHeaderButtonBorder = Color(0xFF3A3D42)

/**
 * Full-screen "Theme" page opened when the user taps the Theme button in Settings.
 * Includes the App style selector:
 *   1. Nothing UI — with the embedded "NOTHING OS // LIQUID GLASS" interactive showcase card
 *   2. Apple UI — with the embedded "APPLE VISIONOS // LIQUID GLASS" interactive showcase card
 *   3. Journey Awaits UI — with the embedded "Journey Awaits, Galactic Scholar" photo theme showcase card
 * And the Theme mode selector:
 *   1. System (red 'A' badge)
 *   2. Light (sun rays)
 *   3. Dark (crescent moon)
 */
@Composable
fun ThemeScreen(
    settings: AppSettings,
    onSelectAppStyle: (AppStyleOption) -> Unit,
    onSelectThemeMode: (ThemeModeOption) -> Unit,
    onResetDefaults: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var resetRotationDeg by remember { mutableFloatStateOf(0f) }
    val animatedResetRotation by animateFloatAsState(
        targetValue = resetRotationDeg,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "reset_spin"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingPureBlack)
            .testTag("theme_page_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Top Bar: Circular Back (<) on Left, Circular Refresh/Reset (↻) on Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button Circle
                ThemeCircularTopButton(
                    onClick = onBack,
                    testTag = "theme_page_back_button"
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        val w = size.width
                        val h = size.height
                        val chevronPath = Path().apply {
                            moveTo(w * 0.65f, h * 0.18f)
                            lineTo(w * 0.30f, h * 0.50f)
                            lineTo(w * 0.65f, h * 0.82f)
                        }
                        drawPath(
                            path = chevronPath,
                            color = Color(0xFF9EA3AE),
                            style = Stroke(
                                width = 2.2.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Reset / Refresh Button Circle
                ThemeCircularTopButton(
                    onClick = {
                        resetRotationDeg -= 360f
                        onResetDefaults()
                    },
                    testTag = "theme_page_reset_button"
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(22.dp)
                            .graphicsLayer { rotationZ = animatedResetRotation }
                    ) {
                        val strokePx = 2.1.dp.toPx()
                        val pad = 2.5.dp.toPx()
                        val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
                        drawArc(
                            color = Color(0xFF9EA3AE),
                            startAngle = -65f,
                            sweepAngle = 290f,
                            useCenter = false,
                            topLeft = Offset(pad, pad),
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        val arrowTipX = size.width * 0.50f
                        val arrowTipY = pad
                        val arrowHead = Path().apply {
                            moveTo(arrowTipX - 4.2.dp.toPx(), arrowTipY - 2.8.dp.toPx())
                            lineTo(arrowTipX + 1.2.dp.toPx(), arrowTipY)
                            lineTo(arrowTipX - 3.2.dp.toPx(), arrowTipY + 4.2.dp.toPx())
                        }
                        drawPath(
                            path = arrowHead,
                            color = Color(0xFF9EA3AE),
                            style = Stroke(
                                width = strokePx,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // Section 1: "App style"
            Text(
                text = "App style",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = 0.1.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(start = 2.dp, bottom = 14.dp)
            )

            // App style rounded container card with integrated showcase cards inside each option
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NothingCardFill)
                    .border(1.dp, NothingCardBorder, RoundedCornerShape(24.dp))
            ) {
                // 1. Nothing UI Row + Embedded "NOTHING OS // LIQUID GLASS" Showcase Card
                val isNothingSelected = settings.appStyle == AppStyleOption.NOTHING_UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.14f)),
                            onClick = { onSelectAppStyle(AppStyleOption.NOTHING_UI) }
                        )
                        .testTag("app_style_nothing_ui")
                ) {
                    ThemeHeaderRowContent(
                        title = "Nothing UI",
                        selected = isNothingSelected,
                        rowHeight = 86.dp,
                        leadingIcon = { NothingDottedRingLogo(modifier = Modifier.size(38.dp)) }
                    )

                    NothingOsShowcasePreviewCard(
                        isSelected = isNothingSelected,
                        onSelect = { onSelectAppStyle(AppStyleOption.NOTHING_UI) },
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = NothingDividerLine
                )

                // 2. Apple UI Row + Embedded Apple VisionOS Liquid Glass Showcase Card
                val isAppleSelected = settings.appStyle == AppStyleOption.APPLE_UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.14f)),
                            onClick = { onSelectAppStyle(AppStyleOption.APPLE_UI) }
                        )
                        .testTag("app_style_apple_ui")
                ) {
                    ThemeHeaderRowContent(
                        title = "Apple UI",
                        selected = isAppleSelected,
                        rowHeight = 86.dp,
                        leadingIcon = { AppleSilhouetteIcon(modifier = Modifier.size(34.dp)) }
                    )

                    AppleVisionGlassShowcasePreviewCard(
                        isSelected = isAppleSelected,
                        onSelect = { onSelectAppStyle(AppStyleOption.APPLE_UI) },
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }

                HorizontalDivider(
                    thickness = 1.dp,
                    color = NothingDividerLine
                )

                // 3. Journey Awaits UI Row + Embedded "Journey Awaits, Galactic Scholar" Showcase Card
                val isJourneySelected = settings.appStyle == AppStyleOption.JOURNEY_AWAITS_UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.14f)),
                            onClick = { onSelectAppStyle(AppStyleOption.JOURNEY_AWAITS_UI) }
                        )
                        .testTag("app_style_journey_awaits_ui")
                ) {
                    ThemeHeaderRowContent(
                        title = "Journey Awaits UI",
                        selected = isJourneySelected,
                        rowHeight = 86.dp,
                        leadingIcon = { AndroidRobotHeadIcon(modifier = Modifier.size(36.dp)) }
                    )

                    JourneyAwaitsGalacticShowcasePreviewCard(
                        isSelected = isJourneySelected,
                        onSelect = { onSelectAppStyle(AppStyleOption.JOURNEY_AWAITS_UI) },
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Section 2: "Theme"
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = 0.1.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(start = 2.dp, bottom = 14.dp)
            )

            // Theme rounded container card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NothingCardFill)
                    .border(1.dp, NothingCardBorder, RoundedCornerShape(24.dp))
            ) {
                // Option 1: System
                ThemeSelectionRow(
                    title = "System",
                    selected = settings.themeMode == ThemeModeOption.SYSTEM,
                    onClick = { onSelectThemeMode(ThemeModeOption.SYSTEM) },
                    rowHeight = 76.dp,
                    testTag = "theme_mode_system",
                    leadingIcon = { SystemRedABadgeIcon(modifier = Modifier.size(32.dp)) }
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = NothingDividerLine
                )

                // Option 2: Light
                ThemeSelectionRow(
                    title = "Light",
                    selected = settings.themeMode == ThemeModeOption.LIGHT,
                    onClick = { onSelectThemeMode(ThemeModeOption.LIGHT) },
                    rowHeight = 76.dp,
                    testTag = "theme_mode_light",
                    leadingIcon = { SunRayIcon(modifier = Modifier.size(32.dp)) }
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = NothingDividerLine
                )

                // Option 3: Dark
                ThemeSelectionRow(
                    title = "Dark",
                    selected = settings.themeMode == ThemeModeOption.DARK,
                    onClick = { onSelectThemeMode(ThemeModeOption.DARK) },
                    rowHeight = 76.dp,
                    testTag = "theme_mode_dark",
                    leadingIcon = { CrescentMoonIcon(modifier = Modifier.size(30.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun NothingOsShowcasePreviewCard(
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nothingPreviewShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(nothingPreviewShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        NothingObsidianBlack,
                        Color(0xFF12151E),
                        Color(0xFF1B1014),
                        NothingObsidianBlack
                    )
                )
            )
            .border(
                width = if (isSelected) 2.dp else 1.2.dp,
                brush = Brush.linearGradient(
                    colors = if (isSelected) {
                        listOf(NothingCrimsonRed, Color.White, NothingBrightRed, Color.White)
                    } else {
                        listOf(Color.White.copy(alpha = 0.55f), NothingCrimsonRed.copy(alpha = 0.45f))
                    }
                ),
                shape = nothingPreviewShape
            )
            .clickable(onClick = onSelect)
            .testTag("theme_nothing_os_showcase_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14171F))
                            .border(
                                width = 1.3.dp,
                                brush = Brush.linearGradient(
                                    listOf(Color.White, NothingCrimsonRed, Color.White)
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(NothingBrightRed, NothingCrimsonRed, Color(0xFF1F0709))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "N",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = NothingDotMatrixFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NothingCrimsonRed)
                            )
                            Text(
                                text = "NOTHING OS // LIQUID GLASS",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = NothingDotMatrixFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = NothingGlyphWhite
                            )
                        }
                        Text(
                            text = "Dot-Matrix UI • Smoked Glass • 165Hz Motion",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceMonoFamily,
                                fontSize = 9.5.sp
                            ),
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1A1D26).copy(alpha = 0.86f),
                                NothingCrimsonRed.copy(alpha = 0.32f),
                                Color(0xFF101218).copy(alpha = 0.90f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                NothingCrimsonRed.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.55f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GLYPH INTERFACE • DOT MATRIX EDITION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceMonoFamily,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        ),
                        color = NothingCrimsonRed
                    )
                    Text(
                        text = "Transparent Circuit Glass & Crimson LED Accents",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.20f) else NothingCrimsonRed
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSelected) "Applied" else "Apply Nothing OS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceMonoFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AppleVisionGlassShowcasePreviewCard(
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(previewShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0C182E),
                        Color(0xFF142A4F),
                        Color(0xFF1E1A45),
                        Color(0xFF0A1324)
                    )
                )
            )
            .border(
                width = if (isSelected) 2.dp else 1.2.dp,
                brush = Brush.linearGradient(
                    colors = if (isSelected) {
                        listOf(LiquidCyan, Color.White, ElectricBlue, PrismViolet)
                    } else {
                        listOf(Color.White.copy(alpha = 0.55f), LiquidCyan.copy(alpha = 0.45f))
                    }
                ),
                shape = previewShape
            )
            .clickable(onClick = onSelect)
            .testTag("theme_apple_ui_showcase_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, LiquidCyan, ElectricBlue)
                            )
                        )
                        .border(1.2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF0A192F),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Apple VisionOS • Liquid Glass",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White
                    )
                    Text(
                        text = "Frosted Specular Glass • Iridescent Prisms • Spatial Depth",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.35f),
                                PrismViolet.copy(alpha = 0.35f),
                                LiquidCyan.copy(alpha = 0.28f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                LiquidCyan.copy(alpha = 0.75f)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SPATIAL GLASS INTERFACE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.9.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        ),
                        color = LiquidCyan
                    )
                    Text(
                        text = "Translucent Acrylic Surfaces & Adaptive Light/Dark",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.22f) else ElectricBlue
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSelected) "Applied" else "Apply Apple UI",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyAwaitsGalacticShowcasePreviewCard(
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewShape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(previewShape)
            .border(
                width = if (isSelected) 2.dp else 1.2.dp,
                brush = Brush.linearGradient(
                    colors = if (isSelected) {
                        listOf(WarmGold, Color.White, LiquidCyan, WarmGold)
                    } else {
                        listOf(Color.White.copy(alpha = 0.55f), WarmGold.copy(alpha = 0.45f))
                    }
                ),
                shape = previewShape
            )
            .clickable(onClick = onSelect)
            .testTag("theme_journey_awaits_showcase_card")
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_galactic_codex_1791427754468),
            contentDescription = "Journey Awaits Galactic Scholar Theme Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF050811).copy(alpha = 0.68f),
                            Color(0xFF0B1522).copy(alpha = 0.56f),
                            Color(0xFF1A1208).copy(alpha = 0.82f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                            .border(
                                width = 1.2.dp,
                                brush = Brush.linearGradient(listOf(Color.White, LiquidCyan, PrismViolet)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color.White, LiquidCyan, PrismViolet)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "P",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Journey Awaits, Galactic Scholar",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Read • Organize • Learn",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1)
                            )
                            Icon(
                                imageVector = Icons.Filled.Diamond,
                                contentDescription = null,
                                tint = LiquidCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = WarmGold,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0E3A47).copy(alpha = 0.65f),
                                Color(0xFF451A03).copy(alpha = 0.60f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(WarmGold.copy(alpha = 0.85f), LiquidCyan.copy(alpha = 0.75f))
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GALACTIC CODEX PHOTO THEME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.0.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        ),
                        color = WarmGold
                    )
                    Text(
                        text = "Discover Your Codex • Starfield & Golden Glass Bar",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.22f) else WarmGold
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isSelected) "Applied" else "Apply Theme",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) Color.White else Color(0xFF1E1306)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeCircularTopButton(
    onClick: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(NothingHeaderButtonFill)
            .border(1.3.dp, NothingHeaderButtonBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        content()
    }
}

@Composable
private fun ThemeHeaderRowContent(
    title: String,
    selected: Boolean,
    rowHeight: androidx.compose.ui.unit.Dp,
    leadingIcon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            leadingIcon()
        }

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                letterSpacing = 0.1.sp
            ),
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        NothingRedRadioIndicator(selected = selected)
    }
}

@Composable
private fun ThemeSelectionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    rowHeight: androidx.compose.ui.unit.Dp,
    testTag: String,
    leadingIcon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.16f)),
                onClick = onClick
            )
            .padding(horizontal = 22.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            leadingIcon()
        }

        Spacer(modifier = Modifier.width(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 19.sp,
                letterSpacing = 0.1.sp
            ),
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        NothingRedRadioIndicator(selected = selected)
    }
}

/**
 * Custom Radio Button matching the reference screenshot:
 * - Selected: Crimson Red outer ring + Crimson Red inner filled circle
 * - Unselected: Subtle grey hollow ring
 */
@Composable
private fun NothingRedRadioIndicator(selected: Boolean) {
    val ringColor by animateColorAsState(
        targetValue = if (selected) NothingRedAccent else NothingRadioUnselected,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "radio_ring_color"
    )
    val innerScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "radio_inner_scale"
    )

    Canvas(modifier = Modifier.size(28.dp)) {
        val radius = (size.minDimension / 2f) - 2.dp.toPx()
        drawCircle(
            color = ringColor,
            radius = radius,
            style = Stroke(width = 2.3.dp.toPx())
        )
        if (innerScale > 0.01f) {
            drawCircle(
                color = NothingRedAccent,
                radius = radius * 0.56f * innerScale
            )
        }
    }
}

/**
 * 1. "Nothing UI" Icon: Concentric rings of crimson-red dots forming a hollow circular badge.
 */
@Composable
private fun NothingDottedRingLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension / 2f

        val rings = listOf(
            Triple(maxR * 0.92f, 24, 1.45.dp.toPx()),
            Triple(maxR * 0.72f, 20, 1.35.dp.toPx()),
            Triple(maxR * 0.53f, 15, 1.25.dp.toPx())
        )

        rings.forEachIndexed { ringIdx, (ringRadius, dotCount, dotRadius) ->
            val phaseOffset = ringIdx * 7.5f
            for (i in 0 until dotCount) {
                val angleRad = ((i * (360f / dotCount) + phaseOffset) * PI / 180.0)
                val dx = center.x + (ringRadius * cos(angleRad)).toFloat()
                val dy = center.y + (ringRadius * sin(angleRad)).toFloat()
                drawCircle(
                    color = NothingRedAccent,
                    radius = dotRadius,
                    center = Offset(dx, dy)
                )
            }
        }
    }
}

/**
 * 2. "Apple UI" Icon: Crisp silver-grey Apple silhouette with leaf and right-side bite.
 */
@Composable
private fun AppleSilhouetteIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val bodyPath = Path().apply {
            moveTo(w * 0.50f, h * 0.30f)
            cubicTo(
                w * 0.38f, h * 0.25f,
                w * 0.16f, h * 0.29f,
                w * 0.16f, h * 0.52f
            )
            cubicTo(
                w * 0.16f, h * 0.73f,
                w * 0.31f, h * 0.95f,
                w * 0.41f, h * 0.95f
            )
            cubicTo(
                w * 0.45f, h * 0.95f,
                w * 0.47f, h * 0.91f,
                w * 0.50f, h * 0.91f
            )
            cubicTo(
                w * 0.53f, h * 0.91f,
                w * 0.55f, h * 0.95f,
                w * 0.60f, h * 0.95f
            )
            cubicTo(
                w * 0.71f, h * 0.95f,
                w * 0.85f, h * 0.73f,
                w * 0.85f, h * 0.52f
            )
            cubicTo(
                w * 0.85f, h * 0.29f,
                w * 0.62f, h * 0.25f,
                w * 0.50f, h * 0.30f
            )
            close()
        }

        val bitePath = Path().apply {
            addOval(
                Rect(
                    center = Offset(w * 0.87f, h * 0.52f),
                    radius = w * 0.16f
                )
            )
        }

        val finalAppleBody = Path().apply {
            op(bodyPath, bitePath, PathOperation.Difference)
        }

        drawPath(path = finalAppleBody, color = NothingIconGrey)

        val leafPath = Path().apply {
            moveTo(w * 0.49f, h * 0.26f)
            cubicTo(
                w * 0.48f, h * 0.14f,
                w * 0.57f, h * 0.05f,
                w * 0.67f, h * 0.05f
            )
            cubicTo(
                w * 0.68f, h * 0.16f,
                w * 0.59f, h * 0.26f,
                w * 0.49f, h * 0.26f
            )
            close()
        }
        drawPath(path = leafPath, color = NothingIconGrey)
    }
}

/**
 * 3. "Journey Awaits UI" Icon: Silver-grey Android robot head dome with antennae and eye cutouts.
 */
@Composable
private fun AndroidRobotHeadIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strokeW = 2.2.dp.toPx()
        drawLine(
            color = NothingIconGrey,
            start = Offset(w * 0.32f, h * 0.42f),
            end = Offset(w * 0.20f, h * 0.22f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        drawLine(
            color = NothingIconGrey,
            start = Offset(w * 0.68f, h * 0.42f),
            end = Offset(w * 0.80f, h * 0.22f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        val domeRect = Rect(
            left = w * 0.12f,
            top = h * 0.34f,
            right = w * 0.88f,
            bottom = h * 1.10f
        )
        drawArc(
            color = NothingIconGrey,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = domeRect.topLeft,
            size = domeRect.size
        )

        val eyeRadius = w * 0.055f
        val eyeY = h * 0.56f
        drawCircle(
            color = NothingCardFill,
            radius = eyeRadius,
            center = Offset(w * 0.35f, eyeY)
        )
        drawCircle(
            color = NothingCardFill,
            radius = eyeRadius,
            center = Offset(w * 0.65f, eyeY)
        )
    }
}

/**
 * "System" Theme Icon: Red rounded square with bold white "A" inside.
 */
@Composable
private fun SystemRedABadgeIcon(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(NothingRedAccent)
    ) {
        Text(
            text = "A",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            ),
            color = Color.White
        )
    }
}

/**
 * "Light" Theme Icon: Outlined sun circle with 8 radiating rays.
 */
@Composable
private fun SunRayIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val circleR = size.minDimension * 0.23f
        val innerRayR = size.minDimension * 0.34f
        val outerRayR = size.minDimension * 0.46f
        val strokePx = 2.0.dp.toPx()

        drawCircle(
            color = NothingIconGrey,
            radius = circleR,
            center = center,
            style = Stroke(width = strokePx)
        )

        for (i in 0 until 8) {
            val angleRad = (i * 45.0) * PI / 180.0
            val sx = center.x + (innerRayR * cos(angleRad)).toFloat()
            val sy = center.y + (innerRayR * sin(angleRad)).toFloat()
            val ex = center.x + (outerRayR * cos(angleRad)).toFloat()
            val ey = center.y + (outerRayR * sin(angleRad)).toFloat()
            drawLine(
                color = NothingIconGrey,
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * "Dark" Theme Icon: Filled silver-grey crescent moon.
 */
@Composable
private fun CrescentMoonIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val outerCircle = Path().apply {
            addOval(
                Rect(
                    center = Offset(w * 0.48f, h * 0.52f),
                    radius = size.minDimension * 0.40f
                )
            )
        }
        val cutoutCircle = Path().apply {
            addOval(
                Rect(
                    center = Offset(w * 0.67f, h * 0.36f),
                    radius = size.minDimension * 0.35f
                )
            )
        }
        val crescent = Path().apply {
            op(outerCircle, cutoutCircle, PathOperation.Difference)
        }
        drawPath(path = crescent, color = NothingIconGrey)
    }
}
