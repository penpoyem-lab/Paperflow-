package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.PaperflowAiService
import com.example.data.AiChatMessageEntity
import com.example.data.AppSettings
import com.example.data.NoteEntity
import com.example.data.PdfDocumentEntity
import com.example.ui.ActiveOverlay
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMagenta
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private data class QuickPromptItem(
    val label: String,
    val prompt: String,
    val icon: ImageVector,
    val accent: Color
)

private val QUICK_PROMPT_CHIPS = listOf(
    QuickPromptItem(
        label = "Summarize this PDF",
        prompt = "Summarize this PDF clearly with key bullet points and main takeaways.",
        icon = Icons.Filled.Summarize,
        accent = LiquidCyan
    ),
    QuickPromptItem(
        label = "Explain this page",
        prompt = "Explain this page in simple, clear terms and break down complex concepts.",
        icon = Icons.Filled.Lightbulb,
        accent = ElectricBlue
    ),
    QuickPromptItem(
        label = "Generate study notes",
        prompt = "Generate structured study notes with headings and key definitions from this material.",
        icon = Icons.Filled.EditNote,
        accent = PrismViolet
    ),
    QuickPromptItem(
        label = "Extract key points",
        prompt = "Extract the most important key points, facts, and arguments.",
        icon = Icons.Filled.AutoAwesome,
        accent = CrystalTeal
    ),
    QuickPromptItem(
        label = "Create quiz questions",
        prompt = "Create 3 active-recall quiz questions based on this content to test my understanding.",
        icon = Icons.Filled.Quiz,
        accent = IridescentPink
    ),
    QuickPromptItem(
        label = "Translate / Rewrite",
        prompt = "Rewrite and polish the text for clarity, structure, and academic tone.",
        icon = Icons.Filled.Spellcheck,
        accent = PrismPurple
    )
)

/**
 * Custom 3D Prismatic Liquid-Glass AI Logo ("Paperflow Prism Star & Orbital Rings")
 * Renders a bespoke multi-layered animated AI emblem with rotating cyan/violet/blue orbital
 * refraction rings, a 4-point crystalline prism star core, and floating micro-sparkles.
 */
@Composable
fun PaperflowAiPrismLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    isThinking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_prism_logo_anim")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isThinking) 1800 else 5200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_logo_orbit"
    )
    val starPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isThinking) 520 else 1600,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_logo_pulse"
    )

    Canvas(modifier = modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val r = this.size.minDimension / 2f

        // 1. Soft internal cyan-violet bloom aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidCyan.copy(alpha = 0.55f),
                    ElectricBlue.copy(alpha = 0.30f),
                    PrismViolet.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = r * 1.05f
            ),
            radius = r * 1.05f,
            center = Offset(cx, cy)
        )

        // 2. Rotating dual prismatic glass orbital rings
        rotate(degrees = orbitAngle, pivot = Offset(cx, cy)) {
            drawOval(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        LiquidCyan,
                        ElectricBlue,
                        PrismViolet,
                        IridescentPink,
                        LiquidCyan
                    ),
                    center = Offset(cx, cy)
                ),
                topLeft = Offset(cx - r * 0.82f, cy - r * 0.42f),
                size = Size(r * 1.64f, r * 0.84f),
                style = Stroke(width = r * 0.11f, cap = StrokeCap.Round)
            )
        }

        rotate(degrees = -orbitAngle * 0.75f + 60f, pivot = Offset(cx, cy)) {
            drawOval(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        LiquidCyan.copy(alpha = 0.65f),
                        PrismPurple.copy(alpha = 0.75f),
                        Color.White.copy(alpha = 0.85f)
                    ),
                    center = Offset(cx, cy)
                ),
                topLeft = Offset(cx - r * 0.42f, cy - r * 0.82f),
                size = Size(r * 0.84f, r * 1.64f),
                style = Stroke(width = r * 0.09f, cap = StrokeCap.Round)
            )
        }

        // 3. Central 4-pointed Liquid-Glass Prism Star Core
        val starOuter = r * 0.58f * starPulse
        val starInner = r * 0.19f * starPulse
        val starPath = Path().apply {
            moveTo(cx, cy - starOuter)
            quadraticTo(cx + starInner * 0.45f, cy - starInner * 0.45f, cx + starOuter, cy)
            quadraticTo(cx + starInner * 0.45f, cy + starInner * 0.45f, cx, cy + starOuter)
            quadraticTo(cx - starInner * 0.45f, cy + starInner * 0.45f, cx - starOuter, cy)
            quadraticTo(cx - starInner * 0.45f, cy - starInner * 0.45f, cx, cy - starOuter)
            close()
        }

        drawPath(
            path = starPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFE0F7FF),
                    LiquidCyan,
                    PrismViolet
                ),
                start = Offset(cx - starOuter, cy - starOuter),
                end = Offset(cx + starOuter, cy + starOuter)
            )
        )

        // 4. Crisp white center diamond specular highlight
        drawCircle(
            color = Color.White,
            radius = r * 0.13f,
            center = Offset(cx, cy)
        )

        // 5. Orbiting satellite sparkle nodes
        val rad = (orbitAngle * PI / 180.0)
        val sx1 = cx + (r * 0.68f * cos(rad)).toFloat()
        val sy1 = cy + (r * 0.68f * sin(rad)).toFloat()
        drawCircle(
            color = Color.White,
            radius = r * 0.09f,
            center = Offset(sx1, sy1)
        )

        val sx2 = cx - (r * 0.62f * cos(rad + 1.2)).toFloat()
        val sy2 = cy - (r * 0.62f * sin(rad + 1.2)).toFloat()
        drawCircle(
            color = LiquidCyan,
            radius = r * 0.08f,
            center = Offset(sx2, sy2)
        )
    }
}

/**
 * Global Draggable Floating AI Chatbot Overlay that works across the entire Paperflow app.
 * Features:
 * - Hardware-accelerated free 2D touch drag with zero lag/jitter
 * - Automatic tap vs. drag disambiguation (dragging never accidentally triggers tap)
 * - Magnetic 4-edge snap animation (Left, Right, Top, Bottom) with DataStore persistence
 * - Expandable VisionOS Liquid-Glass AI Chat Panel with PDF & Note context awareness
 */
@Composable
fun FloatingAiChatbotOverlay(
    settings: AppSettings,
    activeOverlay: ActiveOverlay,
    activeReaderPageIndex: Int,
    documents: List<PdfDocumentEntity>,
    notes: List<NoteEntity>,
    messages: List<AiChatMessageEntity>,
    isChatOpen: Boolean,
    isChatMaximized: Boolean,
    isAiThinking: Boolean,
    onToggleChat: () -> Unit,
    onCloseChat: () -> Unit,
    onToggleMaximize: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRegenerateLast: () -> Unit,
    onClearConversation: () -> Unit,
    onSaveResponseToNote: (String, String) -> Unit,
    onSaveButtonPosition: (Float, Float) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Respect Settings: Enable/disable or hide while reading PDFs
    val isReadingPdf = activeOverlay is ActiveOverlay.PdfReader
    if (!settings.aiAssistantEnabled) return
    if (isReadingPdf && settings.aiHideWhileReadingPdf) return

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val statusBarTopPx = with(density) { WindowInsets.statusBars.asPaddingValues().calculateTopPadding().toPx() }
    val navBarBottomPx = with(density) { WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().toPx() }

    val buttonSizeDp = settings.aiButtonSize.sizeDp.dp
    val buttonSizePx = with(density) { buttonSizeDp.toPx() }
    val edgeMarginPx = with(density) { 14.dp.toPx() }
    // Keep above the bottom floating navigation bar on main tabs
    val bottomExtraClearancePx = with(density) {
        if (activeOverlay is ActiveOverlay.None) 92.dp.toPx() else 20.dp.toPx()
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val containerWidthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val containerHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        val minX = edgeMarginPx
        val maxX = (containerWidthPx - buttonSizePx - edgeMarginPx).coerceAtLeast(minX)
        val minY = (statusBarTopPx + edgeMarginPx)
        val maxY = (containerHeightPx - buttonSizePx - navBarBottomPx - bottomExtraClearancePx).coerceAtLeast(minY)

        val rangeX = (maxX - minX).coerceAtLeast(1f)
        val rangeY = (maxY - minY).coerceAtLeast(1f)

        val targetInitX = (minX + settings.aiButtonNormalizedX.coerceIn(0f, 1f) * rangeX).coerceIn(minX, maxX)
        val targetInitY = (minY + settings.aiButtonNormalizedY.coerceIn(0f, 1f) * rangeY).coerceIn(minY, maxY)

        val offsetX = remember { Animatable(targetInitX) }
        val offsetY = remember { Animatable(targetInitY) }
        var isDragging by remember { mutableStateOf(false) }

        // Keep button inside safe screen bounds when orientation, size, or persisted settings change
        LaunchedEffect(settings.aiButtonNormalizedX, settings.aiButtonNormalizedY, minX, maxX, minY, maxY, settings.aiButtonSize) {
            if (!isDragging) {
                val clampedX = (minX + settings.aiButtonNormalizedX.coerceIn(0f, 1f) * rangeX).coerceIn(minX, maxX)
                val clampedY = (minY + settings.aiButtonNormalizedY.coerceIn(0f, 1f) * rangeY).coerceIn(minY, maxY)
                launch {
                    offsetX.animateTo(
                        targetValue = clampedX,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
                launch {
                    offsetY.animateTo(
                        targetValue = clampedY,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }
            }
        }

        val currentMinX by rememberUpdatedState(minX)
        val currentMaxX by rememberUpdatedState(maxX)
        val currentMinY by rememberUpdatedState(minY)
        val currentMaxY by rememberUpdatedState(maxY)
        val currentOnToggleChat by rememberUpdatedState(onToggleChat)
        val currentOnSavePos by rememberUpdatedState(onSaveButtonPosition)

        // Subtle breathing & scale-up while dragging
        val infiniteTransition = rememberInfiniteTransition(label = "floating_ai_btn_breath")
        val breathingScale by infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ai_btn_breath_scale"
        )
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.78f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ai_btn_glow_alpha"
        )

        val dragScale by animateFloatAsState(
            targetValue = if (isDragging) 1.15f else breathingScale,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "ai_btn_drag_scale"
        )

        // Active Document / Note Context Info for Header Pill
        val activeDoc = remember(activeOverlay, documents) {
            when (activeOverlay) {
                is ActiveOverlay.PdfReader -> documents.firstOrNull { it.id == activeOverlay.documentId }
                is ActiveOverlay.NoteEditor -> activeOverlay.attachedDocId?.let { id -> documents.firstOrNull { it.id == id } }
                else -> null
            }
        }
        val activeNote = remember(activeOverlay, notes) {
            when (activeOverlay) {
                is ActiveOverlay.NoteEditor -> activeOverlay.noteId?.let { id -> notes.firstOrNull { it.id == id } }
                else -> null
            }
        }

        // 1. Expandable Liquid-Glass AI Chat Panel
        val panelOriginX = ((offsetX.value + buttonSizePx / 2f) / containerWidthPx).coerceIn(0.1f, 0.9f)
        val panelOriginY = ((offsetY.value + buttonSizePx / 2f) / containerHeightPx).coerceIn(0.1f, 0.9f)

        AnimatedVisibility(
            visible = isChatOpen,
            enter = fadeIn(tween(220)) + scaleIn(
                initialScale = 0.72f,
                transformOrigin = TransformOrigin(panelOriginX, panelOriginY),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = fadeOut(tween(180)) + scaleOut(
                targetScale = 0.72f,
                transformOrigin = TransformOrigin(panelOriginX, panelOriginY),
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            BackHandler(enabled = isChatOpen) {
                onCloseChat()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCloseChat
                    ),
                contentAlignment = if (isChatMaximized) Alignment.Center else Alignment.BottomCenter
            ) {
                ExpandableLiquidGlassAiChatWindow(
                    messages = messages,
                    activeDocument = activeDoc,
                    activePageIndex = activeReaderPageIndex,
                    activeNote = activeNote,
                    isMaximized = isChatMaximized,
                    isAiThinking = isAiThinking,
                    onMinimize = onCloseChat,
                    onClose = onCloseChat,
                    onToggleMaximize = onToggleMaximize,
                    onSendMessage = onSendMessage,
                    onRegenerateLast = onRegenerateLast,
                    onClearConversation = onClearConversation,
                    onSaveResponseToNote = onSaveResponseToNote,
                    onShowSnackbar = onShowSnackbar,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {} // Consume clicks inside the chat panel
                        )
                )
            }
        }

        // 2. Draggable Circular Liquid-Glass Floating AI Assistant Button
        if (!isChatOpen) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = offsetX.value.roundToInt(),
                            y = offsetY.value.roundToInt()
                        )
                    }
                    .size(buttonSizeDp)
                    .graphicsLayer {
                        scaleX = dragScale
                        scaleY = dragScale
                        alpha = settings.aiButtonOpacity.coerceIn(0.35f, 1f)
                    }
                    .testTag("floating_ai_assistant_button")
                    .pointerInput(settings.aiButtonSize) {
                        val touchSlopPx = viewConfiguration.touchSlop
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var totalDx = 0f
                            var totalDy = 0f
                            var dragStarted = false

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val pointer = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!pointer.pressed) {
                                    pointer.consume()
                                    break
                                }

                                val delta = pointer.positionChange()
                                if (delta != Offset.Zero) {
                                    totalDx += delta.x
                                    totalDy += delta.y

                                    if (!dragStarted && hypot(totalDx, totalDy) > touchSlopPx * 0.85f) {
                                        dragStarted = true
                                        isDragging = true
                                    }

                                    if (dragStarted) {
                                        pointer.consume()
                                        val nextX = (offsetX.value + delta.x).coerceIn(currentMinX, currentMaxX)
                                        val nextY = (offsetY.value + delta.y).coerceIn(currentMinY, currentMaxY)
                                        coroutineScope.launch {
                                            offsetX.snapTo(nextX)
                                            offsetY.snapTo(nextY)
                                        }
                                    }
                                }
                            }

                            if (dragStarted) {
                                isDragging = false
                                // Magnetic 4-Edge Snap Calculation (Left, Right, Top, Bottom)
                                val curX = offsetX.value
                                val curY = offsetY.value
                                val distLeft = curX - currentMinX
                                val distRight = currentMaxX - curX
                                val distTop = curY - currentMinY
                                val distBottom = currentMaxY - curY

                                val minDist = minOf(distLeft, distRight, distTop, distBottom)
                                val snappedX: Float
                                val snappedY: Float

                                when (minDist) {
                                    distLeft -> {
                                        snappedX = currentMinX
                                        snappedY = curY.coerceIn(currentMinY, currentMaxY)
                                    }
                                    distRight -> {
                                        snappedX = currentMaxX
                                        snappedY = curY.coerceIn(currentMinY, currentMaxY)
                                    }
                                    distTop -> {
                                        snappedX = curX.coerceIn(currentMinX, currentMaxX)
                                        snappedY = currentMinY
                                    }
                                    else -> {
                                        snappedX = curX.coerceIn(currentMinX, currentMaxX)
                                        snappedY = currentMaxY
                                    }
                                }

                                val safeRangeX = (currentMaxX - currentMinX).coerceAtLeast(1f)
                                val safeRangeY = (currentMaxY - currentMinY).coerceAtLeast(1f)
                                val normX = ((snappedX - currentMinX) / safeRangeX).coerceIn(0f, 1f)
                                val normY = ((snappedY - currentMinY) / safeRangeY).coerceIn(0f, 1f)

                                currentOnSavePos(normX, normY)

                                coroutineScope.launch {
                                    launch {
                                        offsetX.animateTo(
                                            targetValue = snappedX,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                    launch {
                                        offsetY.animateTo(
                                            targetValue = snappedY,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            } else {
                                // Clean tap gesture — open the AI Chat Panel
                                currentOnToggleChat()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer breathing cyan/violet halo glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LiquidCyan.copy(alpha = 0.52f * glowAlpha),
                                ElectricBlue.copy(alpha = 0.34f * glowAlpha),
                                PrismViolet.copy(alpha = 0.22f * glowAlpha),
                                Color.Transparent
                            ),
                            radius = size.minDimension * 0.72f
                        ),
                        radius = size.minDimension * 0.72f
                    )
                }

                // Liquid-Glass Translucent Orb Surface
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .shadow(
                            elevation = if (isDragging) 20.dp else 12.dp,
                            shape = CircleShape,
                            ambientColor = LiquidCyan,
                            spotColor = PrismViolet
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0E1B38).copy(alpha = 0.92f),
                                    Color(0xFF1B2854).copy(alpha = 0.88f),
                                    Color(0xFF2B1C54).copy(alpha = 0.92f)
                                )
                            )
                        )
                        .border(
                            width = 1.6.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.90f),
                                    LiquidCyan.copy(alpha = 0.85f),
                                    ElectricBlue.copy(alpha = 0.75f),
                                    PrismViolet.copy(alpha = 0.85f),
                                    IridescentPink.copy(alpha = 0.70f),
                                    Color.White.copy(alpha = 0.90f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Top-left liquid glass specular highlight arc
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawOval(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.36f),
                                    Color.Transparent
                                )
                            ),
                            topLeft = Offset(size.width * 0.16f, size.height * 0.07f),
                            size = Size(size.width * 0.68f, size.height * 0.38f)
                        )
                    }

                    // Bespoke Animated Prismatic AI Logo
                    PaperflowAiPrismLogo(
                        size = (buttonSizeDp.value * 0.62f).dp,
                        isThinking = isAiThinking
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableLiquidGlassAiChatWindow(
    messages: List<AiChatMessageEntity>,
    activeDocument: PdfDocumentEntity?,
    activePageIndex: Int,
    activeNote: NoteEntity?,
    isMaximized: Boolean,
    isAiThinking: Boolean,
    onMinimize: () -> Unit,
    onClose: () -> Unit,
    onToggleMaximize: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRegenerateLast: () -> Unit,
    onClearConversation: () -> Unit,
    onSaveResponseToNote: (String, String) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val context = LocalContext.current
    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message or thinking indicator
    LaunchedEffect(messages.size, isAiThinking) {
        val totalItems = messages.size + (if (isAiThinking) 1 else 0) + 1
        if (totalItems > 1) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    val panelModifier = if (isMaximized) {
        modifier
            .fillMaxSize()
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp,
                start = 10.dp,
                end = 10.dp
            )
    } else {
        modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .fillMaxHeight(0.78f)
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 12.dp
            )
    }

    Surface(
        modifier = panelModifier
            .imePadding()
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = LiquidCyan,
                spotColor = PrismViolet
            )
            .clip(RoundedCornerShape(30.dp))
            .border(
                width = 1.4.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.65f),
                        LiquidCyan.copy(alpha = 0.55f),
                        PrismViolet.copy(alpha = 0.50f),
                        ElectricBlue.copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            )
            .testTag("ai_chatbot_window"),
        color = Color(0xFF0B1326).copy(alpha = 0.95f),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Header Bar with Animated Logo, Status Pill & Window Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                LiquidCyan.copy(alpha = 0.16f),
                                ElectricBlue.copy(alpha = 0.12f),
                                PrismViolet.copy(alpha = 0.16f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
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
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF13254C),
                                        Color(0xFF281B52)
                                    )
                                )
                            )
                            .border(1.dp, LiquidCyan.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PaperflowAiPrismLogo(
                            size = 28.dp,
                            isThinking = isAiThinking
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Paperflow AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.2.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isAiThinking) LiquidCyan else EmeraldGreen)
                            )
                        }
                        val statusSubtitle = when {
                            isAiThinking -> "Synthesizing insights..."
                            activeDocument != null -> "Reading ${activeDocument.title} • P.${activePageIndex + 1}"
                            activeNote != null -> "Editing Note • ${activeNote.title}"
                            PaperflowAiService.isLiveGeminiKeyConfigured() -> "Gemini 3.5 Flash • Ready"
                            else -> "Document Intelligence • Ready"
                        }
                        Text(
                            text = statusSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Header Action Buttons: Regenerate, Clear, Maximize/Restore, Minimize, Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (messages.any { it.role == "user" }) {
                        IconButton(
                            onClick = onRegenerateLast,
                            enabled = !isAiThinking,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("ai_chat_regenerate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Regenerate Last Response",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (messages.isNotEmpty()) {
                        IconButton(
                            onClick = onClearConversation,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("ai_chat_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteSweep,
                                contentDescription = "Clear Conversation",
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onToggleMaximize,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("ai_chat_maximize_button")
                    ) {
                        Icon(
                            imageVector = if (isMaximized) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                            contentDescription = if (isMaximized) "Restore Compact Window" else "Expand Fullscreen",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("ai_chat_minimize_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Minimize AI Chat",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("ai_chat_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close AI Chat",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Active Context Banner (when a PDF or Study Note is open)
            if (activeDocument != null || activeNote != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF13203D))
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (activeDocument != null) Icons.Filled.Description else Icons.Filled.EditNote,
                        contentDescription = null,
                        tint = LiquidCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeDocument != null) {
                            "Context attached: ${activeDocument.title} (Page ${activePageIndex + 1} of ${activeDocument.pageCount})"
                        } else {
                            "Context attached: Study Note \"${activeNote?.title.orEmpty()}\""
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = LiquidCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 3. Quick Prompt Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QUICK_PROMPT_CHIPS.forEach { chip ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(chip.accent.copy(alpha = 0.14f))
                            .border(1.dp, chip.accent.copy(alpha = 0.45f), RoundedCornerShape(50))
                            .clickable(enabled = !isAiThinking) {
                                onSendMessage(chip.prompt)
                            }
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = chip.icon,
                            contentDescription = null,
                            tint = chip.accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = chip.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // 4. Scrollable Conversation List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        AiWelcomeEmptyCard(
                            activeDocument = activeDocument,
                            activePageIndex = activePageIndex,
                            onSelectPrompt = { onSendMessage(it) }
                        )
                    }
                } else {
                    items(messages, key = { it.id }) { msg ->
                        AiChatMessageBubble(
                            message = msg,
                            onCopy = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("Paperflow AI", text))
                                onShowSnackbar("Copied AI response to clipboard")
                            },
                            onSaveToNote = {
                                onSaveResponseToNote(msg.content, msg.contextBadge)
                            }
                        )
                    }
                }

                if (isAiThinking) {
                    item {
                        AiThinkingBubble()
                    }
                }
            }

            // 5. Bottom Liquid-Glass Composer Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1A34).copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 110.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF192749))
                        .border(1.dp, LiquidCyan.copy(alpha = 0.38f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputPrompt.isEmpty()) {
                        Text(
                            text = if (activeDocument != null) {
                                "Ask about ${activeDocument.title} (P.${activePageIndex + 1})..."
                            } else {
                                "Ask Paperflow AI anything..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = inputPrompt,
                        onValueChange = { inputPrompt = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        cursorBrush = SolidColor(LiquidCyan),
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_chat_input_field")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val canSend = inputPrompt.isNotBlank() && !isAiThinking
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) {
                                Brush.linearGradient(listOf(LiquidCyan, ElectricBlue, PrismViolet))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF1E293B)))
                            }
                        )
                        .clickable(enabled = canSend) {
                            val toSend = inputPrompt
                            inputPrompt = ""
                            onSendMessage(toSend)
                        }
                        .testTag("ai_chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = if (canSend) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiWelcomeEmptyCard(
    activeDocument: PdfDocumentEntity?,
    activePageIndex: Int,
    onSelectPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF142242).copy(alpha = 0.85f),
                        Color(0xFF1B193E).copy(alpha = 0.85f)
                    )
                )
            )
            .border(1.dp, LiquidCyan.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PaperflowAiPrismLogo(size = 48.dp)
        Text(
            text = "Paperflow Liquid-Glass AI",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = if (activeDocument != null) {
                "Connected to \"${activeDocument.title}\" (Page ${activePageIndex + 1}). Ask me to summarize this page, explain dense paragraphs, generate study notes, or build a quick quiz."
            } else {
                "Your intelligent PDF, Study Note & Research companion. Open any PDF or tap a prompt chip above to summarize, explain, or generate study notes."
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1)
        )
    }
}

@Composable
private fun AiChatMessageBubble(
    message: AiChatMessageEntity,
    onCopy: (String) -> Unit,
    onSaveToNote: () -> Unit
) {
    val isUser = message.role == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (message.contextBadge.isNotBlank()) {
            Text(
                text = message.contextBadge,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 6.dp,
                        bottomEnd = if (isUser) 6.dp else 20.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.linearGradient(
                            colors = listOf(ElectricBlue, PrismViolet)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF162447), Color(0xFF1F1C44))
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) LiquidCyan.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 6.dp,
                        bottomEnd = if (isUser) 6.dp else 20.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FormattedAiText(
                    rawText = message.content,
                    textColor = Color.White
                )

                // Assistant Action Bar: Copy & Save to Study Notes
                if (!isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { onCopy(message.content) }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Copy response",
                                tint = LiquidCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE2E8F0)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(LiquidCyan.copy(alpha = 0.16f))
                                .clickable { onSaveToNote() }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkAdd,
                                contentDescription = "Save to Notes",
                                tint = LiquidCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Save to Notes",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = LiquidCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiThinkingBubble() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF162447))
            .border(1.dp, LiquidCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaperflowAiPrismLogo(size = 20.dp, isThinking = true)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Paperflow AI is analyzing...",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = LiquidCyan
        )
    }
}

/**
 * Cleanly formats markdown-style headings (`###`) and bold spans (`**bold**`) inside AI responses.
 */
@Composable
private fun FormattedAiText(
    rawText: String,
    textColor: Color
) {
    val annotated = remember(rawText, textColor) {
        buildAnnotatedString {
            val lines = rawText.lines()
            lines.forEachIndexed { idx, line ->
                val trimmed = line.trimStart()
                if (trimmed.startsWith("### ")) {
                    withStyle(
                        SpanStyle(
                            color = LiquidCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    ) {
                        append(trimmed.removePrefix("### "))
                    }
                } else {
                    var cursor = 0
                    while (cursor < line.length) {
                        val boldStart = line.indexOf("**", cursor)
                        if (boldStart == -1) {
                            append(line.substring(cursor))
                            break
                        }
                        if (boldStart > cursor) {
                            append(line.substring(cursor, boldStart))
                        }
                        val boldEnd = line.indexOf("**", boldStart + 2)
                        if (boldEnd == -1) {
                            append(line.substring(boldStart))
                            break
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFE0F2FE))) {
                            append(line.substring(boldStart + 2, boldEnd))
                        }
                        cursor = boldEnd + 2
                    }
                }
                if (idx < lines.lastIndex) append("\n")
            }
        }
    }

    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            lineHeight = 20.sp
        )
    )
}
