package com.example.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
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

/**
 * High-end Apple VisionOS + Liquid Glass 3.0-Second Animated Flash Intro for PAPERFLOW.
 *
 * Timeline (driven by hardware-synchronized `withFrameNanos` for 60–165 Hz frame precision):
 * - 0.00–0.30s: Almost-black/dark-navy screen; tiny soft cyan light appears dead-center with subtle floating glass dust.
 * - 0.30–0.70s: Cyan point expands into a glowing glass sphere; thin translucent glass rings form with elastic motion and cyan -> blue -> violet -> pink refraction.
 * - 0.70–1.15s: Sphere smoothly morphs into the Paperflow translucent glass app icon with crisp white PDF/document symbol, internal refraction, and specular highlights.
 * - 1.15–1.55s: Bright soft light sweep travels across the icon from left to right; icon gently rotates ~6.8° and settles back with subtle spring depth bounce.
 * - 1.55–2.00s: "PAPERFLOW" appears below the icon in clean geometric sans-serif typography with subtle cyan/violet glow, smooth fade, and upward settle.
 * - 2.00–2.45s: Thin glass ripple expands outward from the icon; background navy-to-indigo gradient brightens with a subtle horizontal lens flare and atmospheric bloom.
 * - 2.45–3.00s: Seamless morph transition into the main interface — the glass icon scales down naturally toward the top-left header/logo position while the screen fades in underneath.
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

    // Pre-allocated subtle glass dust particles (no random explosion; gentle orbital drift)
    val dustParticles = remember {
        val palette = listOf(
            LiquidCyan,
            ElectricBlue,
            PrismViolet,
            IridescentPink,
            Color.White
        )
        List(26) { idx ->
            val fraction = idx / 26f
            GlassDustParticle(
                baseAngleRad = (fraction * 2.0 * PI).toFloat() + (idx % 5) * 0.27f,
                baseOrbitRadiusFraction = 0.07f + (idx % 7) * 0.042f,
                orbitSpeed = if (idx % 2 == 0) 0.26f else -0.21f,
                radialDriftSpeed = 0.015f + (idx % 4) * 0.006f,
                sizeDp = 1.4f + (idx % 3) * 0.85f,
                baseAlpha = 0.22f + (idx % 4) * 0.11f,
                color = palette[idx % palette.size]
            )
        }
    }

    val reusableDocPath = remember { Path() }
    val reusableFoldPath = remember { Path() }

    LaunchedEffect(visible) {
        val startNanos = withFrameNanos { it }
        while (true) {
            val nowNanos = withFrameNanos { it }
            val sec = ((nowNanos - startNanos) / 1_000_000_000.0).toFloat()
            elapsedSeconds = sec.coerceAtMost(3.02f)
            if (sec >= 3.00f) {
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
                onClick = {} // Consume touches during the 2.45s intro build-up
            )
            .testTag("paperflow_flash_intro_overlay"),
        contentAlignment = Alignment.Center
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val screenHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val centerX = screenWidthPx * 0.5f
        val centerY = screenHeightPx * 0.45f

        // Target Home header/logo orb coordinates (top-left greeting orb at 18.dp start, 12.dp top + statusBar, 56.dp size)
        val targetOrbCenterX = with(density) { (18.dp + 28.dp).toPx() }
        val targetOrbCenterY = with(density) { (statusBarTopPadding + 12.dp + 28.dp).toPx() }

        val t = elapsedSeconds

        // ==================== PHASE PROGRESS CALCULATIONS ====================

        // Phase 1 (0.00 - 0.30s): Tiny soft cyan light appears in center + glass dust
        val p1 = segmentProgress(t, 0.00f, 0.30f, EaseOutCubic)

        // Phase 2 (0.30 - 0.70s): Expands into glowing glass sphere + elastic translucent glass rings
        val p2 = segmentProgress(t, 0.30f, 0.70f, EaseOutCubic)
        val p2Raw = ((t - 0.30f) / 0.40f).coerceIn(0f, 1f)
        val ringElastic = elasticOutProgress(p2Raw)

        // Phase 3 (0.70 - 1.15s): Sphere smoothly morphs into translucent glass app icon with crisp white PDF symbol
        val p3 = segmentProgress(t, 0.70f, 1.15f, EaseInOutCubic)

        // Phase 4 (1.15 - 1.55s): Bright soft light sweep L->R + 6.8° gentle rotation & subtle depth/bounce settle
        val p4Raw = ((t - 1.15f) / 0.40f).coerceIn(0f, 1f)
        val sweepProgress = EaseInOutCubic.transform(p4Raw)
        // Smooth damped rotation wave: rises to +6.8 deg and settles smoothly back to 0 deg
        val iconTiltDegrees = if (t in 1.15f..1.65f) {
            val u = ((t - 1.15f) / 0.50f).coerceIn(0f, 1f)
            (sin(u * PI * 1.35) * exp(-2.4 * u) * 9.5).toFloat()
        } else 0f
        // Subtle depth/bounce scale pulse during 1.15 - 1.55s
        val p4DepthBounce = if (t in 1.15f..1.60f) {
            val u = ((t - 1.15f) / 0.45f).coerceIn(0f, 1f)
            1f + (sin(u * PI) * 0.055f).toFloat()
        } else 1f

        // Phase 5 (1.55 - 2.00s): App name "PAPERFLOW" fades in + slight upward movement
        val p5 = segmentProgress(t, 1.55f, 2.00f, EaseOutCubic)

        // Phase 6 (2.00 - 2.45s): Thin glass ripple expands outward + background brightens + subtle lens flare
        val p6 = segmentProgress(t, 2.00f, 2.45f, EaseOutCubic)

        // Phase 7 (2.45 - 3.00s): Seamless transition to main interface — icon scales & travels toward header orb, background & text fade
        val p7 = segmentProgress(t, 2.45f, 3.00f, EaseInOutCubic)
        val backdropAlpha = (1f - EaseOutCubic.transform(((t - 2.46f) / 0.52f).coerceIn(0f, 1f))).coerceIn(0f, 1f)

        // Icon spatial position & scale interpolation across all 7 phases
        val currentIconCenterX = lerpFloat(centerX, targetOrbCenterX, p7)
        val currentIconCenterY = lerpFloat(centerY, targetOrbCenterY, p7)

        val baseIconRadiusPx = with(density) { 58.dp.toPx() }
        val targetOrbRadiusPx = with(density) { 28.dp.toPx() }

        val currentObjectRadiusPx = when {
            t < 0.30f -> lerpFloat(with(density) { 3.dp.toPx() }, with(density) { 14.dp.toPx() }, p1)
            t < 0.70f -> lerpFloat(with(density) { 14.dp.toPx() }, with(density) { 44.dp.toPx() }, ringElastic)
            t < 1.15f -> lerpFloat(with(density) { 44.dp.toPx() }, baseIconRadiusPx, p3)
            t < 2.45f -> baseIconRadiusPx * p4DepthBounce
            else -> lerpFloat(baseIconRadiusPx, targetOrbRadiusPx, p7)
        }

        // Corner radius morphs from 100% circle (sphere) during 0.0-0.70s into a 28% squircle (0.70-1.15s),
        // and back into the circular header orb during the final 2.45-3.00s dock
        val cornerFraction = when {
            t < 0.70f -> 1.0f
            t < 1.15f -> lerpFloat(1.0f, 0.46f, p3)
            t < 2.45f -> 0.46f
            else -> lerpFloat(0.46f, 1.0f, p7)
        }

        // Full-Screen 165Hz Canvas for Background, Dust, Rings, Ripple, Lens Flare, and Liquid Glass Icon
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = 1f
                }
        ) {
            val w = size.width
            val h = size.height
            val minDim = minOf(w, h)

            // 1. DARK NAVY-TO-INDIGO GRADIENT BACKGROUND + VOLUMETRIC AMBIENT LIGHT
            if (backdropAlpha > 0.005f) {
                val bgBrightBoost = p6 * 0.22f
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF03050C).copy(alpha = backdropAlpha),
                            Color(0xFF070C20).copy(alpha = backdropAlpha),
                            Color(0xFF0E1436).copy(alpha = backdropAlpha),
                            Color(0xFF050713).copy(alpha = backdropAlpha)
                        )
                    )
                )

                // Ambient volumetric cyan / violet / magenta / electric-blue blooms
                val ambientIntensity = (0.12f + 0.35f * p2 + 0.25f * p3 + bgBrightBoost) * backdropAlpha
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = ambientIntensity * 0.52f),
                            PrismViolet.copy(alpha = ambientIntensity * 0.30f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = minDim * (0.42f + 0.28f * p2 + 0.18f * p6)
                    ),
                    radius = minDim * (0.42f + 0.28f * p2 + 0.18f * p6),
                    center = Offset(centerX, centerY)
                )

                // Secondary subtle magenta/cyan off-axis refraction bloom
                if (t > 0.30f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LiquidCyan.copy(alpha = ambientIntensity * 0.34f),
                                IridescentPink.copy(alpha = ambientIntensity * 0.18f),
                                Color.Transparent
                            ),
                            center = Offset(
                                centerX + cos(t * 0.9f) * minDim * 0.12f,
                                centerY - sin(t * 0.8f) * minDim * 0.10f
                            ),
                            radius = minDim * 0.48f
                        ),
                        radius = minDim * 0.48f,
                        center = Offset(
                            centerX + cos(t * 0.9f) * minDim * 0.12f,
                            centerY - sin(t * 0.8f) * minDim * 0.10f
                        )
                    )
                }
            }

            // 2. SUBTLE FLOATING PARTICLES & GLASS DUST (0.00s - 2.75s)
            if (backdropAlpha > 0.02f) {
                val dustGlobalAlpha = (p1 * backdropAlpha).coerceIn(0f, 1f)
                dustParticles.forEach { particle ->
                    val angle = particle.baseAngleRad + t * particle.orbitSpeed
                    val radius = minDim * (particle.baseOrbitRadiusFraction + t * particle.radialDriftSpeed)
                    val px = centerX + cos(angle) * radius
                    val py = centerY + sin(angle) * radius * 0.86f
                    val pSize = particle.sizeDp.dp.toPx()

                    drawCircle(
                        color = particle.color.copy(alpha = (particle.baseAlpha * dustGlobalAlpha).coerceIn(0f, 0.65f)),
                        radius = pSize,
                        center = Offset(px, py)
                    )
                }
            }

            // 3. ELASTIC TRANSLUCENT GLASS RINGS (0.30s - 1.35s)
            if (t in 0.28f..1.45f && backdropAlpha > 0.01f) {
                val ringFadeOut = 1f - segmentProgress(t, 0.85f, 1.40f, EaseInOutCubic)
                val ringAlpha = (p2 * ringFadeOut * backdropAlpha).coerceIn(0f, 1f)
                val ringBaseRadius = baseIconRadiusPx * (0.75f + 0.68f * ringElastic)

                // Inner Cyan-Blue Refraction Ring
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            LiquidCyan.copy(alpha = 0.72f * ringAlpha),
                            ElectricBlue.copy(alpha = 0.48f * ringAlpha),
                            PrismViolet.copy(alpha = 0.65f * ringAlpha),
                            IridescentPink.copy(alpha = 0.52f * ringAlpha),
                            LiquidCyan.copy(alpha = 0.72f * ringAlpha)
                        ),
                        center = Offset(centerX, centerY)
                    ),
                    radius = ringBaseRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.6.dp.toPx())
                )

                // Outer Frosted Specular Halo Ring
                val outerRingRadius = baseIconRadiusPx * (0.95f + 1.02f * ringElastic)
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f * ringAlpha),
                            LiquidCyan.copy(alpha = 0.28f * ringAlpha),
                            IridescentPink.copy(alpha = 0.32f * ringAlpha),
                            Color.White.copy(alpha = 0.15f * ringAlpha)
                        )
                    ),
                    radius = outerRingRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 1.1.dp.toPx())
                )
            }

            // 4. EXPANDING THIN GLASS RIPPLE & SUBTLE ANAMORPHIC LENS FLARE (2.00s - 2.65s)
            if (t >= 2.00f && backdropAlpha > 0.01f) {
                val rippleRaw = ((t - 2.00f) / 0.58f).coerceIn(0f, 1f)
                val rippleEase = EaseOutCubic.transform(rippleRaw)
                val rippleAlpha = ((1f - rippleEase) * 0.68f * backdropAlpha).coerceIn(0f, 1f)
                val rippleRadius = lerpFloat(baseIconRadiusPx * 1.05f, minDim * 0.68f, rippleEase)

                // Thin refractive glass ripple wave
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            LiquidCyan.copy(alpha = rippleAlpha * 0.42f),
                            Color.White.copy(alpha = rippleAlpha * 0.75f),
                            PrismViolet.copy(alpha = rippleAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = rippleRadius
                    ),
                    radius = rippleRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 2.2.dp.toPx())
                )

                // Subtle horizontal optical lens flare across the icon center
                val flareIntensity = sin(rippleRaw * PI).toFloat().coerceIn(0f, 1f) * backdropAlpha
                if (flareIntensity > 0.01f) {
                    val flareWidth = minDim * (0.45f + 0.35f * rippleEase)
                    val flareHeight = 3.2.dp.toPx()
                    drawOval(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                LiquidCyan.copy(alpha = 0.28f * flareIntensity),
                                Color.White.copy(alpha = 0.68f * flareIntensity),
                                IridescentPink.copy(alpha = 0.28f * flareIntensity),
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

            // 5. CENTER POINT -> GLOWING GLASS SPHERE -> TRANSLUCENT LIQUID GLASS APP ICON
            withTransform({
                rotate(
                    degrees = iconTiltDegrees,
                    pivot = Offset(currentIconCenterX, currentIconCenterY)
                )
            }) {
                drawLiquidGlassMorphingIcon(
                    cx = currentIconCenterX,
                    cy = currentIconCenterY,
                    radiusPx = currentObjectRadiusPx,
                    cornerFraction = cornerFraction,
                    pointPhase = p1,
                    spherePhase = p2,
                    iconMorphPhase = p3,
                    lightSweepProgress = if (t >= 1.15f) sweepProgress else -1f,
                    dockPhase = p7,
                    reusableDocPath = reusableDocPath,
                    reusableFoldPath = reusableFoldPath
                )
            }
        }

        // 6. CLEAN GEOMETRIC TYPOGRAPHY: [APP NAME] ("PAPERFLOW") (1.55s - 2.65s)
        if (t >= 1.52f && backdropAlpha > 0.01f) {
            val textFadeOut = 1f - segmentProgress(t, 2.45f, 2.78f, EaseInOutCubic)
            val textAlpha = (p5 * textFadeOut * backdropAlpha).coerceIn(0f, 1f)
            val textOffsetY = with(density) {
                val baseBelowIcon = 96.dp.toPx()
                val slideUpPx = (1f - p5) * 18.dp.toPx()
                (baseBelowIcon + slideUpPx - (screenHeightPx * 0.05f)).roundToInt()
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(0, textOffsetY) }
                    .graphicsLayer {
                        alpha = textAlpha
                        val settleScale = 0.96f + 0.04f * p5
                        scaleX = settleScale
                        scaleY = settleScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Subtle cyan/violet atmospheric text bloom behind the geometric title
                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = 5.5.sp
                    ),
                    color = LiquidCyan.copy(alpha = 0.36f * textAlpha),
                    modifier = Modifier.shadow(
                        elevation = 18.dp,
                        ambientColor = LiquidCyan,
                        spotColor = PrismViolet
                    )
                )

                Text(
                    text = appName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        letterSpacing = 5.5.sp
                    ),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Renders the continuous photorealistic liquid-glass object:
 * - Tiny soft cyan point (0.00–0.30s)
 * - Glowing glass sphere with cyan -> blue -> violet -> pink internal refraction (0.30–0.70s)
 * - Translucent glass app icon with crisp white PDF/document symbol (0.70–1.15s)
 * - Left-to-right specular light sweep (1.15–1.55s)
 * - Seamless morph into the Home header orb (2.45–3.00s)
 */
private fun DrawScope.drawLiquidGlassMorphingIcon(
    cx: Float,
    cy: Float,
    radiusPx: Float,
    cornerFraction: Float,
    pointPhase: Float,
    spherePhase: Float,
    iconMorphPhase: Float,
    lightSweepProgress: Float,
    dockPhase: Float,
    reusableDocPath: Path,
    reusableFoldPath: Path
) {
    val diameter = (radiusPx * 2f).coerceAtLeast(4f)
    val topLeft = Offset(cx - radiusPx, cy - radiusPx)
    val boxSize = Size(diameter, diameter)
    val cornerRadiusPx = (radiusPx * cornerFraction).coerceIn(8f, radiusPx)
    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

    // Outer soft cyan/violet bloom halo around the glass object
    val haloRadius = radiusPx * (1.85f - 0.35f * dockPhase)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                LiquidCyan.copy(alpha = 0.45f * pointPhase * (1f - 0.3f * dockPhase)),
                ElectricBlue.copy(alpha = 0.28f * spherePhase),
                PrismPurple.copy(alpha = 0.20f * iconMorphPhase),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = haloRadius.coerceAtLeast(6f)
        ),
        radius = haloRadius.coerceAtLeast(6f),
        center = Offset(cx, cy)
    )

    // Translucent Liquid-Glass Body Base
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF0E1D42).copy(alpha = 0.72f + 0.16f * dockPhase),
                ElectricBlue.copy(alpha = 0.38f + 0.38f * dockPhase),
                PrismViolet.copy(alpha = 0.36f + 0.38f * dockPhase),
                IridescentPink.copy(alpha = 0.28f * spherePhase * (1f - 0.4f * dockPhase))
            ),
            start = topLeft,
            end = Offset(cx + radiusPx, cy + radiusPx)
        ),
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = cornerRadius
    )

    // Internal Cyan -> Blue -> Violet -> Pink Caustic Refraction Orb
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.78f * (1f - 0.55f * iconMorphPhase + 0.35f * dockPhase)),
                LiquidCyan.copy(alpha = 0.65f),
                ElectricBlue.copy(alpha = 0.38f),
                PrismViolet.copy(alpha = 0.28f),
                Color.Transparent
            ),
            center = Offset(cx - radiusPx * 0.24f, cy - radiusPx * 0.26f),
            radius = diameter * 0.78f
        ),
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = cornerRadius
    )

    // Bottom-right Magenta/Pink Internal Subsurface Refraction
    if (spherePhase > 0.05f) {
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    IridescentPink.copy(alpha = 0.42f * spherePhase),
                    PrismPurple.copy(alpha = 0.24f * spherePhase),
                    Color.Transparent
                ),
                center = Offset(cx + radiusPx * 0.38f, cy + radiusPx * 0.42f),
                radius = diameter * 0.65f
            ),
            topLeft = topLeft,
            size = boxSize,
            cornerRadius = cornerRadius
        )
    }

    // Top-left Specular Glass Crest Highlight
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.42f),
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            ),
            startY = topLeft.y,
            endY = cy + radiusPx * 0.2f
        ),
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = cornerRadius
    )

    // Crisp White PDF / Document Symbol inside the Translucent Glass Icon (0.70s - 2.90s)
    val symbolAlpha = (iconMorphPhase * (1f - dockPhase * 0.85f)).coerceIn(0f, 1f)
    if (symbolAlpha > 0.01f) {
        val docW = radiusPx * 0.88f
        val docH = radiusPx * 1.12f
        val docLeft = cx - docW * 0.5f
        val docTop = cy - docH * 0.5f
        val docRight = cx + docW * 0.5f
        val docBottom = cy + docH * 0.5f
        val foldSize = docW * 0.30f
        val docCorner = docW * 0.16f

        // Frosted inner glass document plate
        reusableDocPath.reset()
        reusableDocPath.apply {
            moveTo(docLeft + docCorner, docTop)
            lineTo(docRight - foldSize, docTop)
            lineTo(docRight, docTop + foldSize)
            lineTo(docRight, docBottom - docCorner)
            quadraticTo(docRight, docBottom, docRight - docCorner, docBottom)
            lineTo(docLeft + docCorner, docBottom)
            quadraticTo(docLeft, docBottom, docLeft, docBottom - docCorner)
            lineTo(docLeft, docTop + docCorner)
            quadraticTo(docLeft, docTop, docLeft + docCorner, docTop)
            close()
        }

        // Subtle inner cyan/white fill inside the PDF page symbol
        drawPath(
            path = reusableDocPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.26f * symbolAlpha),
                    LiquidCyan.copy(alpha = 0.14f * symbolAlpha)
                ),
                startY = docTop,
                endY = docBottom
            )
        )

        // Crisp white anti-aliased document outline
        val strokeW = (2.2.dp.toPx() * (0.75f + 0.25f * iconMorphPhase)).coerceAtLeast(1.5f)
        drawPath(
            path = reusableDocPath,
            color = Color.White.copy(alpha = 0.96f * symbolAlpha),
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Folded corner flap
        reusableFoldPath.reset()
        reusableFoldPath.apply {
            moveTo(docRight - foldSize, docTop)
            lineTo(docRight - foldSize, docTop + foldSize)
            lineTo(docRight, docTop + foldSize)
        }
        drawPath(
            path = reusableFoldPath,
            color = Color.White.copy(alpha = 0.90f * symbolAlpha),
            style = Stroke(width = strokeW * 0.88f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Clean geometric document lines inside the PDF symbol
        val lineLeft = docLeft + docW * 0.22f
        val lineRightFull = docRight - docW * 0.22f
        val lineRightShort = docLeft + docW * 0.58f
        val line1Y = docTop + docH * 0.46f
        val line2Y = docTop + docH * 0.63f
        val line3Y = docTop + docH * 0.79f

        drawLine(
            color = LiquidCyan.copy(alpha = 0.92f * symbolAlpha),
            start = Offset(lineLeft, line1Y),
            end = Offset(lineRightShort, line1Y),
            strokeWidth = strokeW * 0.85f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.92f * symbolAlpha),
            start = Offset(lineLeft, line2Y),
            end = Offset(lineRightFull, line2Y),
            strokeWidth = strokeW * 0.85f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.78f * symbolAlpha),
            start = Offset(lineLeft, line3Y),
            end = Offset(lineRightFull * 0.92f + lineLeft * 0.08f, line3Y),
            strokeWidth = strokeW * 0.85f,
            cap = StrokeCap.Round
        )
    }

    // Left-to-Right Soft Specular Light Sweep across the Glass Icon (1.15s - 1.55s)
    if (lightSweepProgress in 0.0f..1.0f) {
        val sweepCenterX = lerpFloat(topLeft.x - radiusPx * 0.4f, topLeft.x + diameter + radiusPx * 0.4f, lightSweepProgress)
        val sweepAlpha = sin(lightSweepProgress * PI).toFloat().coerceIn(0f, 1f)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.55f * sweepAlpha),
                    LiquidCyan.copy(alpha = 0.30f * sweepAlpha),
                    Color.Transparent
                ),
                start = Offset(sweepCenterX - radiusPx * 0.55f, topLeft.y),
                end = Offset(sweepCenterX + radiusPx * 0.55f, topLeft.y + diameter)
            ),
            topLeft = topLeft,
            size = boxSize,
            cornerRadius = cornerRadius
        )
    }

    // Thin Specular White + Iridescent Glass Rim Border
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.88f),
                LiquidCyan.copy(alpha = 0.65f),
                PrismViolet.copy(alpha = 0.55f),
                IridescentPink.copy(alpha = 0.52f),
                Color.White.copy(alpha = 0.72f)
            ),
            start = topLeft,
            end = Offset(cx + radiusPx, cy + radiusPx)
        ),
        topLeft = topLeft,
        size = boxSize,
        cornerRadius = cornerRadius,
        style = Stroke(width = 1.8.dp.toPx())
    )
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
    // Gentle liquid-glass elastic overshoot (damped sinusoid)
    return (1.0 - exp(-5.2 * clamped) * cos(1.85 * PI * clamped)).toFloat().coerceIn(0f, 1.08f)
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}
