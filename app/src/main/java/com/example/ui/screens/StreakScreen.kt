package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsDataStore
import com.example.data.StreakCheckInEvent
import com.example.data.StreakData
import com.example.ui.components.GlassCircularIconButton
import com.example.ui.components.LiquidGlassPanel
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMagenta
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.WarmGold
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class StreakDayEntry(
    val isoDate: String,
    val dayOfMonth: Int,
    val narrowDayName: String,
    val shortDayName: String,
    val isToday: Boolean
)

private fun buildRecentStreakDays(
    count: Int,
    anchorIsoDate: String? = null
): List<StreakDayEntry> {
    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val shortFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val baseCalendar = Calendar.getInstance()
    if (!anchorIsoDate.isNullOrBlank()) {
        runCatching {
            isoFormat.parse(anchorIsoDate)?.let { parsed ->
                baseCalendar.time = parsed
            }
        }
    }
    val todayIso = isoFormat.format(baseCalendar.time)
    return (-(count - 1)..0).map { offset ->
        val cal = (baseCalendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, offset)
        }
        val iso = isoFormat.format(cal.time)
        val shortName = shortFormat.format(cal.time)
        val narrowName = shortName.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        StreakDayEntry(
            isoDate = iso,
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
            narrowDayName = narrowName,
            shortDayName = shortName,
            isToday = iso == todayIso
        )
    }
}

@Composable
fun StreakCelebrationModal(
    event: StreakCheckInEvent,
    hapticEnabled: Boolean,
    onContinue: () -> Unit,
    onViewDetails: () -> Unit
) {
    BackHandler(onBack = onContinue)

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Animated counting number from previousStreak -> newStreak
    var displayedStreak by remember(event) { mutableIntStateOf(event.previousStreak) }
    val numberScale = remember { Animatable(0.75f) }
    val burstProgress = remember { Animatable(0f) }
    val cardEntrance = remember { Animatable(0.84f) }

    LaunchedEffect(event) {
        if (hapticEnabled) {
            runCatching {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val vibrator = context.getSystemService(Vibrator::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        }
        cardEntrance.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        delay(220)
        if (event.previousStreak < event.newStreak) {
            val steps = (event.newStreak - event.previousStreak).coerceIn(1, 20)
            val stepDelay = (420L / steps).coerceAtLeast(25L)
            for (v in (event.previousStreak + 1)..event.newStreak) {
                displayedStreak = v
                delay(stepDelay)
            }
        } else {
            displayedStreak = event.newStreak
        }
        // Final number pop + particle burst
        if (hapticEnabled) {
            runCatching { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
        }
        numberScale.animateTo(
            targetValue = 1.16f,
            animationSpec = tween(durationMillis = 190, easing = FastOutSlowInEasing)
        )
        burstProgress. snapTo(0.01f)
        burstProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
        )
        numberScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "streak_celebration_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ray_rotation"
    )
    val shimmerSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_sweep"
    )

    // Full-screen translucent liquid-glass celebration backdrop (tap outside to dismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9060A17))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onContinue
            )
            .testTag("streak_celebration_modal"),
        contentAlignment = Alignment.Center
    ) {
        // Flowing multicolor ambient light rays, glass orbs & particle burst Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.44f
            val maxR = size.minDimension * 0.62f

            // Ambient multicolor liquid refraction orbs
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.36f), Color.Transparent),
                    center = Offset(cx - maxR * 0.35f, cy - maxR * 0.25f),
                    radius = maxR * 1.05f * glowPulse
                ),
                radius = maxR * 1.05f * glowPulse,
                center = Offset(cx - maxR * 0.35f, cy - maxR * 0.25f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrismPurple.copy(alpha = 0.34f), IridescentPink.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(cx + maxR * 0.35f, cy + maxR * 0.15f),
                    radius = maxR * 0.98f * glowPulse
                ),
                radius = maxR * 0.98f * glowPulse,
                center = Offset(cx + maxR * 0.35f, cy + maxR * 0.15f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SolarAmber.copy(alpha = if (event.isMilestone) 0.42f else 0.28f), LiquidCyan.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(cx, cy - maxR * 0.1f),
                    radius = maxR * 0.85f * glowPulse
                ),
                radius = maxR * 0.85f * glowPulse,
                center = Offset(cx, cy - maxR * 0.1f)
            )

            // Soft rotating light rays behind the glass card
            val rayCount = if (event.isMilestone) 16 else 12
            for (i in 0 until rayCount) {
                val angleRad = ((i * (360f / rayCount) + rayRotation) * (PI / 180.0)).toFloat()
                val endX = cx + cos(angleRad) * maxR * 1.15f
                val endY = cy + sin(angleRad) * maxR * 1.15f
                val rayColor = when (i % 4) {
                    0 -> LiquidCyan.copy(alpha = 0.14f)
                    1 -> SolarAmber.copy(alpha = 0.16f)
                    2 -> IridescentPink.copy(alpha = 0.14f)
                    else -> ElectricBlue.copy(alpha = 0.14f)
                }
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(rayColor, Color.Transparent),
                        start = Offset(cx, cy),
                        end = Offset(endX, endY)
                    ),
                    start = Offset(cx, cy),
                    end = Offset(endX, endY),
                    strokeWidth = if (event.isMilestone) 24f else 16f,
                    cap = StrokeCap.Round
                )
            }

            // Subtle floating glass particles + radial burst when the final streak number locks in
            val particlePalette = listOf(SolarAmber, LiquidCyan, IridescentPink, PrismViolet, ElectricBlue, WarmGold)
            val totalParticles = if (event.isMilestone) 28 else 18
            for (i in 0 until totalParticles) {
                val baseAngle = (i * (360f / totalParticles) + shimmerSweep * 25f) * (PI / 180.0)
                val burstDist = (0.28f + 0.65f * burstProgress.value) * maxR * (0.65f + (i % 3) * 0.22f)
                val px = cx + cos(baseAngle).toFloat() * burstDist
                val py = cy + sin(baseAngle).toFloat() * burstDist
                val pColor = particlePalette[i % particlePalette.size]
                val alpha = ((1f - burstProgress.value * 0.55f).coerceIn(0.25f, 0.9f))

                drawCircle(
                    color = pColor.copy(alpha = alpha),
                    radius = if (i % 3 == 0) 6.5f else 4.2f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.7f),
                    radius = 2.2f,
                    center = Offset(px, py)
                )
            }
        }

        // Main Translucent Liquid-Glass Celebration Card (consumes clicks so tapping inside doesn't accidentally dismiss)
        val cardShape = RoundedCornerShape(36.dp)
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 400.dp)
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = cardEntrance.value,
                    scaleY = cardEntrance.value
                )
                .shadow(
                    elevation = 28.dp,
                    shape = cardShape,
                    ambientColor = if (event.isMilestone) SolarAmber else ElectricBlue,
                    spotColor = IridescentPink
                )
                .clip(cardShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E294B).copy(alpha = 0.84f),
                            Color(0xFF17183B).copy(alpha = 0.88f),
                            Color(0xFF121830).copy(alpha = 0.90f)
                        )
                    )
                )
                .drawBehind {
                    // Flowing glass reflection highlight across the card
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width * shimmerSweep, size.height * 0.18f),
                            radius = size.width * 0.7f
                        )
                    )
                }
                .border(
                    width = 1.8.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            LiquidCyan.copy(alpha = 0.75f),
                            SolarAmber.copy(alpha = 0.75f),
                            IridescentPink.copy(alpha = 0.75f),
                            Color.White.copy(alpha = 0.90f)
                        )
                    ),
                    shape = cardShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(horizontal = 26.dp, vertical = 30.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Milestone Special Badge if unlocked (3, 7, 14, 30, 50, 100, 365 days)
                if (event.isMilestone && event.milestoneTitle != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(SolarAmber.copy(alpha = 0.32f), IridescentPink.copy(alpha = 0.32f))
                                )
                            )
                            .border(1.2.dp, WarmGold, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = WarmGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = event.milestoneTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.1.sp
                            ),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Glowing 3D Liquid Glass Flame Orb
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .graphicsLayer(
                            scaleX = glowPulse,
                            scaleY = glowPulse
                        )
                        .shadow(24.dp, CircleShape, ambientColor = SolarAmber, spotColor = IridescentPink)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    WarmGold,
                                    SolarAmber,
                                    IridescentPink,
                                    PrismPurple
                                )
                            )
                        )
                        .border(
                            width = 2.2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White, WarmGold, LiquidCyan)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = "Streak Flame",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Large Animated Counting Streak Number (Snapchat-style 🔥 + number)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = numberScale.value,
                            scaleY = numberScale.value
                        )
                ) {
                    Text(
                        text = "\uD83D\uDD25",
                        fontSize = 52.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$displayedStreak",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 68.sp,
                            lineHeight = 72.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        modifier = Modifier.testTag("streak_celebration_number")
                    )
                }

                Text(
                    text = "${displayedStreak} DAY STREAK!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = LiquidCyan,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (event.newStreak >= 7) "You're on fire! ${event.subtitleMessage}" else event.subtitleMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.88f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                // 7-Day Mini Glass Week Progress Row inside the modal
                WeeklyStreakMiniStrip(
                    currentStreak = event.newStreak,
                    lastCheckInDate = event.checkInDate
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary "Continue" Liquid Glass Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ElectricBlue, PrismViolet, IridescentPink)
                            )
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(50))
                        .testTag("streak_continue_button")
                        .clickable(onClick = onContinue)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "View Streak Calendar & Milestones",
                    style = MaterialTheme.typography.labelLarge,
                    color = LiquidCyan.copy(alpha = 0.9f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onViewDetails)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun WeeklyStreakMiniStrip(
    currentStreak: Int,
    lastCheckInDate: String
) {
    val days = remember(lastCheckInDate) {
        buildRecentStreakDays(count = 7, anchorIsoDate = lastCheckInDate)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { idx, date ->
            val daysAgo = 6 - idx
            val isActive = daysAgo < currentStreak
            val isToday = daysAgo == 0
            val dayLabel = date.narrowDayName

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) WarmGold else Color.White.copy(alpha = 0.7f)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) {
                                Brush.linearGradient(listOf(SolarAmber, IridescentPink))
                            } else {
                                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.06f)))
                            }
                        )
                        .border(
                            width = if (isToday) 1.5.dp else 1.dp,
                            color = if (isToday) Color.White else Color.White.copy(alpha = 0.25f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${date.dayOfMonth}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
    }
}

// ==================== STREAK DETAILS & LIQUID GLASS HEATMAP SCREEN ====================

@Composable
fun StreakDetailsOverlay(
    streakData: StreakData,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val glass = LocalGlassColors.current

    // Last 7 days for Weekly Activity
    val currentWeekDays = remember {
        buildRecentStreakDays(count = 7)
    }

    // Last 28 days (4 weeks) for Liquid Glass Calendar Heatmap
    val last28Days = remember {
        buildRecentStreakDays(count = 28)
    }

    val activeStreakCount = streakData.currentStreak.coerceAtLeast(1)
    val longestStreakCount = maxOf(streakData.longestStreak, activeStreakCount)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp)
            .testTag("streak_details_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GlassCircularIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Streaks",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Keep your daily reading streak alive! \uD83D\uDD25",
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary
                        )
                    }
                }

                // Snapchat-style Streak Pill in Header (🔥 + Number)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (glass.isDark || glass.isGalacticCodex) {
                                Color(0xFF1F232B).copy(alpha = 0.94f)
                            } else {
                                Color(0xFFFFF7ED)
                            }
                        )
                        .border(
                            width = 1.2.dp,
                            color = SolarAmber.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\uD83D\uDD25",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "$activeStreakCount",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        ),
                        color = if (glass.isDark || glass.isGalacticCodex) Color.White else Color(0xFF1E293B)
                    )
                }
            }
        }

        // Hero Current Streak & Longest Streak Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassPanel(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 26.dp,
                    tintColor = SolarAmber,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(listOf(SolarAmber, IridescentPink))
                                )
                                .border(1.2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "$activeStreakCount",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = if (activeStreakCount == 1) "Day Current Streak" else "Days Current Streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = glass.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                LiquidGlassPanel(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 26.dp,
                    tintColor = PrismViolet,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(listOf(ElectricBlue, PrismViolet))
                                )
                                .border(1.2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "$longestStreakCount",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = if (longestStreakCount == 1) "Day Longest Streak" else "Days Longest Streak",
                            style = MaterialTheme.typography.labelMedium,
                            color = glass.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Weekly Activity Strip
        item {
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = ElectricBlue
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Activity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Last 7 Days",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricBlue
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        currentWeekDays.forEach { date ->
                            val iso = date.isoDate
                            val checkedIn = streakData.checkInHistoryDates.contains(iso) || date.isToday
                            val isToday = date.isToday
                            val dayName = date.shortDayName

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isToday) ElectricBlue else glass.textSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (checkedIn) {
                                                Brush.linearGradient(listOf(SolarAmber, IridescentPink))
                                            } else {
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color.White.copy(alpha = if (glass.isDark) 0.10f else 0.55f),
                                                        Color.White.copy(alpha = if (glass.isDark) 0.05f else 0.35f)
                                                    )
                                                )
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 2.dp else 1.dp,
                                            color = if (isToday) ElectricBlue else Color.White.copy(alpha = 0.6f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (checkedIn) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalFireDepartment,
                                            contentDescription = "Checked in",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${date.dayOfMonth}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = glass.textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Liquid-Glass 28-Day Calendar / Heatmap
        item {
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = CrystalTeal
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = CrystalTeal,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recent Check-In Heatmap (28 Days)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = glass.textPrimary
                        )
                    }

                    last28Days.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            week.forEach { date ->
                                val iso = date.isoDate
                                val active = streakData.checkInHistoryDates.contains(iso) || date.isToday
                                val isToday = date.isToday

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (active) {
                                                Brush.linearGradient(listOf(CrystalTeal, EmeraldGreen))
                                            } else {
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color.White.copy(alpha = if (glass.isDark) 0.08f else 0.50f),
                                                        Color.White.copy(alpha = if (glass.isDark) 0.04f else 0.30f)
                                                    )
                                                )
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 1.8.dp else 1.dp,
                                            color = if (isToday) Color.White else Color.White.copy(alpha = 0.45f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${date.dayOfMonth}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (active || isToday) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (active) Color.White else glass.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Streak Milestones Progression (3, 7, 14, 30, 50, 100, 365 days)
        item {
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tintColor = IridescentPink
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Streak Milestones",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )

                    SettingsDataStore.MILESTONE_DAYS.forEach { targetDays ->
                        val unlocked = longestStreakCount >= targetDays || streakData.unlockedMilestones.contains(targetDays)
                        val title = SettingsDataStore.getMilestoneTitle(targetDays) ?: "$targetDays DAYS"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (unlocked) SolarAmber.copy(alpha = 0.18f)
                                    else Color.White.copy(alpha = if (glass.isDark) 0.06f else 0.4f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (unlocked) SolarAmber else Color.White.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (unlocked) SolarAmber else glass.textMuted.copy(alpha = 0.25f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (unlocked) Icons.Filled.CheckCircle else Icons.Filled.Whatshot,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = glass.textPrimary
                                    )
                                    Text(
                                        text = if (unlocked) "Unlocked!" else "${(targetDays - activeStreakCount).coerceAtLeast(1)} more days to unlock",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (unlocked) EmeraldGreen else glass.textSecondary
                                    )
                                }
                            }

                            Text(
                                text = "${targetDays}d",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (unlocked) SolarAmber else glass.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
