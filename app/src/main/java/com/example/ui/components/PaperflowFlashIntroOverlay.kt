package com.example.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.NothingBrightRed
import com.example.ui.theme.NothingCrimsonRed
import com.example.ui.theme.NothingDotMatrixFamily
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SpaceMonoFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin

private data class GlassDustParticle(
    val baseAngleRad: Float,
    val baseOrbitRadiusFraction: Float,
    val orbitSpeed: Float,
    val radialDriftSpeed: Float,
    val sizeDp: Float,
    val baseAlpha: Float,
    val color: Color
)

private val EaseOutCubic = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1.0f)
private val EaseInOutCubic = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)
private val BrandRedTop = Color(0xFFED1C24)
private val BrandRedHighlight = Color(0xFFFF3B3F)
private val BrandDarkBottom = Color(0xFF151517)
private val BrandObsidianOuter = Color(0xFF060608)

/**
 * Reusable ultra-crisp vector composable of the official Red/Black/White "P↗" App Icon Emblem.
 * Can be placed in headers, avatars, about cards, and splash screens at any resolution without pixelation.
 */
@Composable
fun PaperflowBrandIconBadge(
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    glowEnabled: Boolean = true
) {
    val circleClipPath = remember { Path() }
    val pMonogramPath = remember { Path() }
    val pCounterCutoutPath = remember { Path() }

    Canvas(
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
    ) {
        val minDim = minOf(this.size.width, this.size.height)
        val cx = this.size.width * 0.5f
        val cy = this.size.height * 0.5f
        val radius = minDim * 0.48f

        if (glowEnabled) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BrandRedTop.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy - radius * 0.25f),
                    radius = radius * 1.35f
                ),
                radius = radius * 1.35f,
                center = Offset(cx, cy)
            )
        }

        drawPaperflowRedBlackEmblem(
            cx = cx,
            cy = cy,
            radiusPx = radius,
            horizonRevealProgress = 1f,
            ringSweepProgress = 1f,
            monogramProgress = 1f,
            lightSweepProgress = -1f,
            circleClipPath = circleClipPath,
            pMonogramPath = pMonogramPath,
            pCounterCutoutPath = pCounterCutoutPath
        )
    }
}

/**
 * High-Resolution 165Hz Flash Screen Animation featuring the official Red/Black/White "P↗" App Icon.
 *
 * Timeline (driven by hardware-synchronized `withFrameNanos` for 60–165 Hz frame precision):
 * - 0.00–0.28s: Deep AMOLED obsidian canvas (#060608) with crimson & white glyph particles; central core ignites.
 * - 0.28–0.72s: Dual-hemisphere disc expands with elastic spring physics — vibrant crimson red (#ED1C24) top half
 *               and matte carbon black (#151517) bottom half, surrounded by a precision-sweeping pure white outer ring.
 * - 0.58–1.12s: Bold geometric white "P↗" monogram & diagonal arrow vector lock into the center with crisp anti-aliased precision.
 * - 1.12–1.60s: High-contrast specular light sweep travels across the emblem + subtle 3D spring tilt & depth pulse.
 * - 1.45–2.05s: "PAPERFLOW" title and "NOTHING OS // PDF STUDIO" subtitle reveal below with crimson/white glow.
 * - 1.95–2.45s: Dual crimson + white shockwave ripple & horizontal anamorphic lens flare expand outward.
 * - 2.45–2.95s: Seamless spatial dock — the emblem smoothly scales & glides into the top-left Home header avatar position while the main UI reveals underneath.
 */
@Composable
fun PaperflowFlashIntroOverlay(
    visible: Boolean,
    appName: String = "PAPERFLOW",
    onIntroFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    var elapsedSeconds by remember { mutableFloatStateOf(0f) }

    val dustParticles = remember {
        val palette = listOf(
            BrandRedTop,
            Color.White,
            LiquidCyan,
            NothingBrightRed,
            Color(0xFFE2E8F0)
        )
        List(28) { idx ->
            val fraction = idx / 28f
            GlassDustParticle(
                baseAngleRad = (fraction * 2.0 * PI).toFloat() + (idx % 5) * 0.25f,
                baseOrbitRadiusFraction = 0.08f + (idx % 7) * 0.044f,
                orbitSpeed = if (idx % 2 == 0) 0.30f else -0.24f,
                radialDriftSpeed = 0.016f + (idx % 4) * 0.006f,
                sizeDp = 1.5f + (idx % 3) * 0.9f,
                baseAlpha = 0.26f + (idx % 4) * 0.12f,
                color = palette[idx % palette.size]
            )
        }
    }

    val circleClipPath = remember { Path() }
    val pMonogramPath = remember { Path() }
    val pCounterCutoutPath = remember { Path() }

    LaunchedEffect(visible) {
        val startNanos = withFrameNanos { it }
        while (true) {
            val nowNanos = withFrameNanos { it }
            val sec = ((nowNanos - startNanos) / 1_000_000_000.0).toFloat()
            elapsedSeconds = sec.coerceAtMost(2.96f)
            if (sec >= 2.92f) {
                onIntroFinished()
                break
            }
        }
    }

    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    // Allow user to tap after 1.6s if they want to skip directly into the app
                    if (elapsedSeconds > 1.6f) {
                        onIntroFinished()
                    }
                }
            )
            .testTag("paperflow_flash_intro_overlay"),
        contentAlignment = Alignment.Center
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val screenHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val centerX = screenWidthPx * 0.5f
        val centerY = screenHeightPx * 0.43f

        // Target Home header/logo orb coordinates (top-left greeting badge at 18.dp start, 12.dp top + statusBar, 46.dp size)
        val targetOrbCenterX = with(density) { (18.dp + 23.dp).toPx() }
        val targetOrbCenterY = with(density) { (statusBarTopPadding + 12.dp + 23.dp).toPx() }

        val t = elapsedSeconds

        // Phase 1 (0.00 - 0.28s): Ignition core
        val p1 = segmentProgress(t, 0.00f, 0.28f, EaseOutCubic)

        // Phase 2 (0.20 - 0.72s): Elastic expansion of Red/Black hemisphere & White outer ring
        val p2 = segmentProgress(t, 0.20f, 0.72f, EaseOutCubic)
        val p2Raw = ((t - 0.20f) / 0.52f).coerceIn(0f, 1f)
        val discElastic = elasticOutProgress(p2Raw)

        // Phase 3 (0.52 - 1.10s): Bold geometric White "P↗" monogram & arrow lock-in
        val p3 = segmentProgress(t, 0.52f, 1.10f, EaseInOutCubic)
        val monogramElastic = elasticOutProgress(((t - 0.52f) / 0.58f).coerceIn(0f, 1f))

        // Phase 4 (1.10 - 1.60s): Specular light sweep + subtle rotational kinetic impulse
        val p4Raw = ((t - 1.10f) / 0.50f).coerceIn(0f, 1f)
        val sweepProgress = EaseInOutCubic.transform(p4Raw)
        val iconTiltDegrees = if (t in 1.05f..1.65f) {
            val u = ((t - 1.05f) / 0.60f).coerceIn(0f, 1f)
            (sin(u * PI * 1.4) * exp(-2.5 * u) * 7.5).toFloat()
        } else 0f
        val p4DepthBounce = if (t in 1.08f..1.60f) {
            val u = ((t - 1.08f) / 0.52f).coerceIn(0f, 1f)
            1f + (sin(u * PI) * 0.065f).toFloat()
        } else 1f

        // Phase 5 (1.42 - 1.98s): Brand Typography reveal
        val p5 = segmentProgress(t, 1.42f, 1.98f, EaseOutCubic)

        // Phase 6 (1.92 - 2.45s): Dual Crimson + White Shockwave Ripple & Anamorphic Flare
        val p6 = segmentProgress(t, 1.92f, 2.45f, EaseOutCubic)

        // Phase 7 (2.42 - 2.92s): Seamless spatial dock into top-left Home avatar badge
        val p7 = segmentProgress(t, 2.42f, 2.92f, EaseInOutCubic)
        val backdropAlpha = (1f - EaseOutCubic.transform(((t - 2.42f) / 0.48f).coerceIn(0f, 1f))).coerceIn(0f, 1f)

        val currentIconCenterX = lerpFloat(centerX, targetOrbCenterX, p7)
        val currentIconCenterY = lerpFloat(centerY, targetOrbCenterY, p7)

        val baseIconRadiusPx = with(density) { 68.dp.toPx() }
        val targetOrbRadiusPx = with(density) { 23.dp.toPx() }

        val currentObjectRadiusPx = when {
            t < 0.20f -> lerpFloat(with(density) { 4.dp.toPx() }, with(density) { 18.dp.toPx() }, p1)
            t < 0.72f -> lerpFloat(with(density) { 18.dp.toPx() }, baseIconRadiusPx, discElastic)
            t < 2.42f -> baseIconRadiusPx * p4DepthBounce
            else -> lerpFloat(baseIconRadiusPx, targetOrbRadiusPx, p7)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
        ) {
            val w = size.width
            val h = size.height
            val minDim = minOf(w, h)

            // 1. DEEP AMOLED OBSIDIAN BACKGROUND + CRIMSON / CARBON ATMOSPHERIC GLOW
            if (backdropAlpha > 0.005f) {
                val bgBrightBoost = p6 * 0.18f
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BrandObsidianOuter.copy(alpha = backdropAlpha),
                            Color(0xFF0B0B10).copy(alpha = backdropAlpha),
                            Color(0xFF12090D).copy(alpha = backdropAlpha),
                            BrandObsidianOuter.copy(alpha = backdropAlpha)
                        )
                    )
                )

                // Subtle Nothing OS dot-matrix grid in the flash background
                val gridStep = 26.dp.toPx()
                val dotAlpha = (0.07f * p2 * backdropAlpha).coerceIn(0f, 0.12f)
                if (dotAlpha > 0.005f) {
                    var gx = gridStep
                    while (gx < w) {
                        var gy = gridStep
                        while (gy < h) {
                            drawCircle(
                                color = Color.White.copy(alpha = dotAlpha),
                                radius = 1.05.dp.toPx(),
                                center = Offset(gx, gy)
                            )
                            gy += gridStep
                        }
                        gx += gridStep
                    }
                }

                // Ambient volumetric Crimson Red + Pure White Halo behind the emblem
                val ambientIntensity = (0.16f + 0.38f * p2 + 0.24f * p3 + bgBrightBoost) * backdropAlpha
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandRedTop.copy(alpha = ambientIntensity * 0.56f),
                            NothingCrimsonRed.copy(alpha = ambientIntensity * 0.26f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY - baseIconRadiusPx * 0.2f),
                        radius = minDim * (0.44f + 0.24f * p2 + 0.16f * p6)
                    ),
                    radius = minDim * (0.44f + 0.24f * p2 + 0.16f * p6),
                    center = Offset(centerX, centerY - baseIconRadiusPx * 0.2f)
                )
            }

            // 2. ORBITAL GLYPH PARTICLES
            if (backdropAlpha > 0.02f) {
                val dustGlobalAlpha = (p1 * backdropAlpha).coerceIn(0f, 1f)
                dustParticles.forEach { particle ->
                    val angle = particle.baseAngleRad + t * particle.orbitSpeed
                    val radius = minDim * (particle.baseOrbitRadiusFraction + t * particle.radialDriftSpeed)
                    val px = centerX + cos(angle) * radius
                    val py = centerY + sin(angle) * radius * 0.88f
                    val pSize = particle.sizeDp.dp.toPx()

                    drawCircle(
                        color = particle.color.copy(alpha = (particle.baseAlpha * dustGlobalAlpha).coerceIn(0f, 0.72f)),
                        radius = pSize,
                        center = Offset(px, py)
                    )
                }
            }

            // 3. PRECISION ORBITAL RINGS (0.22s - 1.45s)
            if (t in 0.22f..1.48f && backdropAlpha > 0.01f) {
                val ringFadeOut = 1f - segmentProgress(t, 0.90f, 1.45f, EaseInOutCubic)
                val ringAlpha = (p2 * ringFadeOut * backdropAlpha).coerceIn(0f, 1f)
                val ringBaseRadius = baseIconRadiusPx * (0.85f + 0.55f * discElastic)

                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            BrandRedTop.copy(alpha = 0.80f * ringAlpha),
                            Color.White.copy(alpha = 0.65f * ringAlpha),
                            BrandRedTop.copy(alpha = 0.45f * ringAlpha),
                            Color.White.copy(alpha = 0.75f * ringAlpha),
                            BrandRedTop.copy(alpha = 0.80f * ringAlpha)
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    radius = ringBaseRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                val outerRingRadius = baseIconRadiusPx * (1.05f + 0.92f * discElastic)
                drawCircle(
                    color = Color.White.copy(alpha = 0.30f * ringAlpha),
                    radius = outerRingRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.1.dp.toPx())
                )
            }

            // 4. EXPANDING SHOCKWAVE RIPPLE & ANAMORPHIC LENS FLARE (1.92s - 2.60s)
            if (t >= 1.92f && backdropAlpha > 0.01f) {
                val rippleRaw = ((t - 1.92f) / 0.62f).coerceIn(0f, 1f)
                val rippleEase = EaseOutCubic.transform(rippleRaw)
                val rippleAlpha = ((1f - rippleEase) * 0.75f * backdropAlpha).coerceIn(0f, 1f)
                val rippleRadius = lerpFloat(baseIconRadiusPx * 1.04f, minDim * 0.70f, rippleEase)

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            BrandRedTop.copy(alpha = rippleAlpha * 0.50f),
                            Color.White.copy(alpha = rippleAlpha * 0.85f),
                            BrandRedTop.copy(alpha = rippleAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = rippleRadius
                    ),
                    radius = rippleRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.4.dp.toPx())
                )

                val flareIntensity = sin(rippleRaw * PI).toFloat().coerceIn(0f, 1f) * backdropAlpha
                if (flareIntensity > 0.01f) {
                    val flareWidth = minDim * (0.50f + 0.36f * rippleEase)
                    val flareHeight = 3.4.dp.toPx()
                    drawOval(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BrandRedTop.copy(alpha = 0.45f * flareIntensity),
                                Color.White.copy(alpha = 0.85f * flareIntensity),
                                BrandRedTop.copy(alpha = 0.45f * flareIntensity),
                                Color.Transparent
                            ),
                            startX = centerX - flareWidth * 0.5f,
                            endX = centerX + flareWidth * 0.5f
                        ),
                        topLeft = Offset(centerX - flareWidth * 0.5f, centerY - flareHeight * 0.5f),
                        size = Size(flareWidth, flareHeight)
                    )
                }
            }

            // 5. OFFICIAL PAPERFLOW RED/BLACK/WHITE "P↗" EMBLEM
            withTransform({
                rotate(
                    degrees = iconTiltDegrees,
                    pivot = Offset(currentIconCenterX, currentIconCenterY)
                )
            }) {
                // Outer ambient glow around the emblem
                val haloRadius = currentObjectRadiusPx * (1.75f - 0.35f * p7)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandRedTop.copy(alpha = 0.48f * p1 * (1f - 0.3f * p7)),
                            Color.White.copy(alpha = 0.22f * p2 * (1f - 0.5f * p7)),
                            Color.Transparent
                        ),
                        center = Offset(currentIconCenterX, currentIconCenterY),
                        radius = haloRadius.coerceAtLeast(8f)
                    ),
                    radius = haloRadius.coerceAtLeast(8f),
                    center = Offset(currentIconCenterX, currentIconCenterY)
                )

                drawPaperflowRedBlackEmblem(
                    cx = currentIconCenterX,
                    cy = currentIconCenterY,
                    radiusPx = currentObjectRadiusPx,
                    horizonRevealProgress = p2,
                    ringSweepProgress = p2,
                    monogramProgress = (p3 * monogramElastic.coerceAtMost(1.04f)).coerceIn(0f, 1.04f),
                    lightSweepProgress = if (t >= 1.10f) sweepProgress else -1f,
                    circleClipPath = circleClipPath,
                    pMonogramPath = pMonogramPath,
                    pCounterCutoutPath = pCounterCutoutPath
                )
            }
        }

        // 6. BRAND TYPOGRAPHY REVEAL ("PAPERFLOW" + "LIQUID GLASS PDF STUDIO")
        if (t >= 1.40f && backdropAlpha > 0.01f) {
            val textFadeOut = 1f - segmentProgress(t, 2.38f, 2.75f, EaseInOutCubic)
            val textAlpha = (p5 * textFadeOut * backdropAlpha).coerceIn(0f, 1f)
            val textOffsetY = with(density) {
                val baseBelowIcon = 112.dp.toPx()
                val slideUpPx = (1f - p5) * 20.dp.toPx()
                (baseBelowIcon + slideUpPx - (screenHeightPx * 0.04f)).roundToInt()
            }

            Column(
                modifier = Modifier
                    .offset { IntOffset(0, textOffsetY) }
                    .graphicsLayer {
                        alpha = textAlpha
                        val settleScale = 0.94f + 0.06f * p5
                        scaleX = settleScale
                        scaleY = settleScale
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = NothingDotMatrixFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            letterSpacing = 6.sp
                        ),
                        color = BrandRedTop.copy(alpha = 0.45f * textAlpha),
                        modifier = Modifier.shadow(
                            elevation = 20.dp,
                            ambientColor = BrandRedTop,
                            spotColor = Color.White
                        )
                    )

                    Text(
                        text = appName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = NothingDotMatrixFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            letterSpacing = 6.sp
                        ),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "NOTHING OS // LIQUID GLASS PDF",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 2.4.sp
                    ),
                    color = BrandRedHighlight.copy(alpha = 0.92f * textAlpha)
                )
            }
        }
    }
}

/**
 * Draws the exact high-resolution vector recreation of the user's uploaded app icon:
 * - Thick pure white outer circular border ring (#FFFFFF)
 * - Inner circle split horizontally:
 *   - Top half: vibrant crimson red (#ED1C24)
 *   - Bottom half: deep matte black (#151517)
 * - Centered bold geometric white "P" monogram:
 *   - Left vertical stem extending down-left with a sharp 45° angled point at the bottom-left
 *   - Rounded right loop with a sharp triangular/slanted inner cutout
 *   - Upper-right diagonal arrow projecting seamlessly from the loop into the red upper-right quadrant
 */
private fun DrawScope.drawPaperflowRedBlackEmblem(
    cx: Float,
    cy: Float,
    radiusPx: Float,
    horizonRevealProgress: Float,
    ringSweepProgress: Float,
    monogramProgress: Float,
    lightSweepProgress: Float,
    circleClipPath: Path,
    pMonogramPath: Path,
    pCounterCutoutPath: Path
) {
    if (radiusPx <= 2f) return

    val innerRadius = radiusPx * 0.845f
    val ringStrokeWidth = (radiusPx * 0.145f).coerceAtLeast(2f)
    val ringCenterRadius = radiusPx - ringStrokeWidth * 0.5f

    // Build circular clip path for the inner split hemisphere
    circleClipPath.reset()
    circleClipPath.addOval(
        Rect(
            left = cx - innerRadius,
            top = cy - innerRadius,
            right = cx + innerRadius,
            bottom = cy + innerRadius
        )
    )

    clipPath(circleClipPath) {
        // Bottom half (Matte Carbon Black #151517)
        drawRect(
            color = BrandDarkBottom,
            topLeft = Offset(cx - innerRadius, cy - innerRadius),
            size = Size(innerRadius * 2f, innerRadius * 2f)
        )

        // Top half (Vibrant Crimson Red #ED1C24 -> #E1141C)
        val redHeight = innerRadius * horizonRevealProgress.coerceIn(0f, 1f)
        if (redHeight > 0.5f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BrandRedHighlight,
                        BrandRedTop,
                        Color(0xFFD9141C)
                    ),
                    startY = cy - innerRadius,
                    endY = cy
                ),
                topLeft = Offset(cx - innerRadius, cy - redHeight),
                size = Size(innerRadius * 2f, redHeight)
            )
        }

        // Bold Geometric White "P↗" Monogram with Integrated Up-Right Arrow
        if (monogramProgress > 0.01f) {
            val mScale = monogramProgress.coerceIn(0f, 1.04f)
            val r = innerRadius * mScale

            // Outer contour of the "P" + integrated upper-right arrow
            pMonogramPath.reset()
            pMonogramPath.apply {
                // Start at top-left corner of the 'P' stem
                moveTo(cx - 0.52f * r, cy - 0.48f * r)
                // Top horizontal bar moving right into the curved bowl
                lineTo(cx + 0.06f * r, cy - 0.48f * r)
                cubicTo(
                    cx + 0.22f * r, cy - 0.48f * r,
                    cx + 0.34f * r, cy - 0.43f * r,
                    cx + 0.41f * r, cy - 0.37f * r
                )
                // Neck going up-right to arrow left wing
                lineTo(cx + 0.47f * r, cy - 0.43f * r)
                // Arrow left/top barb wing
                lineTo(cx + 0.26f * r, cy - 0.56f * r)
                // Sharp Arrow Tip in top-right red quadrant
                lineTo(cx + 0.65f * r, cy - 0.68f * r)
                // Arrow right/bottom barb wing
                lineTo(cx + 0.58f * r, cy - 0.23f * r)
                // Neck returning from arrow wing into outer right curve of the 'P' bowl
                lineTo(cx + 0.45f * r, cy - 0.36f * r)
                // Outer right curve of the 'P' loop sweeping down into the black bottom half
                cubicTo(
                    cx + 0.56f * r, cy - 0.20f * r,
                    cx + 0.52f * r, cy + 0.05f * r,
                    cx + 0.36f * r, cy + 0.26f * r
                )
                // Angled bottom leg of the 'P' bowl (matching the reference photo's diagonal cut)
                lineTo(cx + 0.21f * r, cy + 0.45f * r)
                lineTo(cx - 0.14f * r, cy + 0.45f * r)
                // Slanted inner leg rising up-right toward the center of the bowl
                lineTo(cx + 0.35f * r, cy - 0.22f * r)
                // Horizontal inner roof of the 'P' counter going left to the inner stem
                lineTo(cx - 0.26f * r, cy - 0.22f * r)
                // Inner vertical stem going down into the bottom-left quadrant
                lineTo(cx - 0.26f * r, cy + 0.62f * r)
                // Sharp 45-degree diagonal point at the bottom-left of the 'P' stem (touches the white ring)
                lineTo(cx - 0.52f * r, cy + 0.89f * r)
                close()
            }

            drawPath(
                path = pMonogramPath,
                color = Color.White
            )
        }

        // Specular Light Sweep across the Emblem (during flash animation)
        if (lightSweepProgress in 0.0f..1.0f) {
            val sweepX = lerpFloat(cx - innerRadius * 1.4f, cx + innerRadius * 1.4f, lightSweepProgress)
            val sweepAlpha = sin(lightSweepProgress * PI).toFloat().coerceIn(0f, 1f)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.45f * sweepAlpha),
                        Color.Transparent
                    ),
                    start = Offset(sweepX - innerRadius * 0.45f, cy - innerRadius),
                    end = Offset(sweepX + innerRadius * 0.45f, cy + innerRadius)
                ),
                topLeft = Offset(cx - innerRadius, cy - innerRadius),
                size = Size(innerRadius * 2f, innerRadius * 2f)
            )
        }
    }

    // Thick Pure White Outer Circular Ring (#FFFFFF)
    val sweepAngle = (360f * ringSweepProgress.coerceIn(0f, 1f))
    if (sweepAngle >= 359.5f) {
        drawCircle(
            color = Color.White,
            radius = ringCenterRadius,
            center = Offset(cx, cy),
            style = Stroke(width = ringStrokeWidth)
        )
    } else if (sweepAngle > 1f) {
        drawArc(
            color = Color.White,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(cx - ringCenterRadius, cy - ringCenterRadius),
            size = Size(ringCenterRadius * 2f, ringCenterRadius * 2f),
            style = Stroke(width = ringStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

private fun segmentProgress(
    timeSec: Float,
    startSec: Float,
    endSec: Float,
    easing: androidx.compose.animation.core.Easing = LinearEasing
): Float {
    if (timeSec <= startSec) return 0f
    if (timeSec >= endSec) return 1f
    val raw = ((timeSec - startSec) / (endSec - startSec)).coerceIn(0f, 1f)
    return easing.transform(raw)
}

private fun elasticOutProgress(x: Float): Float {
    val clamped = x.coerceIn(0f, 1f)
    if (clamped == 0f || clamped == 1f) return clamped
    return (1.0 - exp(-5.2 * clamped) * cos(1.85 * PI * clamped)).toFloat().coerceIn(0f, 1.08f)
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}
