package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnnotationEntity
import com.example.data.PageLayoutOption
import com.example.data.PdfDocumentEntity
import com.example.data.ReaderThemeOption
import com.example.pdf.PdfEngine
import com.example.ui.screens.AnnotationTool
import com.example.ui.theme.AnnotYellow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.NothingCrimsonRed
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.WarmGold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * State controller for [PdfDocumentViewer], managing zoom scale, pan offset,
 * current page index, and smooth physics-based transitions.
 */
@Stable
class PdfDocumentViewerState(
    initialPage: Int,
    val pageCount: Int,
    val listState: LazyListState,
    val pagerState: PagerState,
    private val scope: CoroutineScope
) {
    val zoomAnimatable = Animatable(1f)
    val panAnimatable = Animatable(Offset.Zero, Offset.VectorConverter)

    var viewportSize by mutableStateOf(IntSize.Zero)
    var isPinchGestureActive by mutableStateOf(false)

    val zoomScale: Float
        get() = zoomAnimatable.value.coerceIn(MIN_ZOOM, MAX_ZOOM)

    val panOffset: Offset
        get() = panAnimatable.value

    val isZoomed: Boolean
        get() = zoomScale > 1.04f

    fun currentPageIndex(layoutMode: PageLayoutOption): Int {
        return if (layoutMode == PageLayoutOption.SINGLE_PAGE) {
            pagerState.currentPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        } else {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                listState.firstVisibleItemIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val closestItem = visibleItems.minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    abs(itemCenter - viewportCenter)
                }
                (closestItem?.index ?: listState.firstVisibleItemIndex)
                    .coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            }
        }
    }

    fun clampOffsetForScale(rawOffset: Offset, scale: Float): Offset {
        if (scale <= 1.01f || viewportSize.width <= 0 || viewportSize.height <= 0) {
            return Offset.Zero
        }
        val maxPanX = (viewportSize.width * (scale - 1f)) / 2f
        val maxPanY = (viewportSize.height * (scale - 1f)) / 1.45f
        return Offset(
            x = rawOffset.x.coerceIn(-maxPanX, maxPanX),
            y = rawOffset.y.coerceIn(-maxPanY, maxPanY)
        )
    }

    fun updateTransformImmediate(
        zoomChange: Float,
        panChange: Offset,
        centroid: Offset
    ) {
        val oldScale = zoomScale
        val targetScale = (oldScale * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
        val effectiveScaleRatio = if (oldScale > 0f) targetScale / oldScale else 1f

        val viewportCenter = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
        val focusShift = if (viewportSize.width > 0 && abs(zoomChange - 1f) > 0.001f) {
            (centroid - viewportCenter) * (1f - effectiveScaleRatio)
        } else {
            Offset.Zero
        }

        val rawNewOffset = (panOffset * effectiveScaleRatio) + panChange + focusShift
        val clampedOffset = clampOffsetForScale(rawNewOffset, targetScale)

        scope.launch {
            zoomAnimatable.snapTo(targetScale)
            panAnimatable.snapTo(clampedOffset)
        }
    }

    fun animateZoomTo(
        targetScale: Float,
        focalPoint: Offset? = null
    ) {
        val clampedScale = targetScale.coerceIn(MIN_ZOOM, MAX_ZOOM)
        val oldScale = zoomScale
        val targetOffset = if (clampedScale <= 1.02f) {
            Offset.Zero
        } else if (focalPoint != null && viewportSize.width > 0 && viewportSize.height > 0) {
            val viewportCenter = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
            val ratio = clampedScale / oldScale.coerceAtLeast(1f)
            val candidate = (panOffset * ratio) + (viewportCenter - focalPoint) * (clampedScale - 1f) * 0.65f
            clampOffsetForScale(candidate, clampedScale)
        } else {
            clampOffsetForScale(panOffset, clampedScale)
        }

        scope.launch {
            launch {
                zoomAnimatable.animateTo(
                    targetValue = clampedScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                panAnimatable.animateTo(
                    targetValue = targetOffset,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    fun resetZoom() {
        animateZoomTo(1f)
    }

    fun stepZoomCycle() {
        val nextScale = when {
            zoomScale < 1.4f -> 1.75f
            zoomScale < 2.3f -> 2.5f
            zoomScale < 3.2f -> 3.5f
            else -> 1f
        }
        animateZoomTo(nextScale)
    }

    fun scrollToPage(pageIndex: Int, layoutMode: PageLayoutOption, animate: Boolean = true) {
        val target = pageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        scope.launch {
            if (isZoomed) {
                zoomAnimatable.snapTo(1f)
                panAnimatable.snapTo(Offset.Zero)
            }
            if (layoutMode == PageLayoutOption.SINGLE_PAGE) {
                if (animate) pagerState.animateScrollToPage(target)
                else pagerState.scrollToPage(target)
            } else {
                if (animate) listState.animateScrollToItem(target)
                else listState.scrollToItem(target)
            }
        }
    }

    companion object {
        const val MIN_ZOOM = 1f
        const val MAX_ZOOM = 4.0f
    }
}

@Composable
fun rememberPdfDocumentViewerState(
    documentId: Long,
    initialPage: Int,
    pageCount: Int
): PdfDocumentViewerState {
    val safeInitial = initialPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = safeInitial)
    val pagerState = rememberPagerState(
        initialPage = safeInitial,
        pageCount = { pageCount.coerceAtLeast(1) }
    )
    val scope = rememberCoroutineScope()
    return remember(documentId, pageCount) {
        PdfDocumentViewerState(
            initialPage = safeInitial,
            pageCount = pageCount,
            listState = listState,
            pagerState = pagerState,
            scope = scope
        )
    }
}

/**
 * Reusable Compose PDF Document Viewer supporting smooth multi-touch pinch-to-zoom,
 * focal-point double-tap zoom, 2D bounded panning, vertical continuous scroll,
 * horizontal single-page swipe, interactive fast-scroll scrubber, and progressive
 * HD bitmap rendering when zoomed in.
 */
@Composable
fun PdfDocumentViewer(
    document: PdfDocumentEntity,
    viewerState: PdfDocumentViewerState,
    layoutMode: PageLayoutOption,
    readerTheme: ReaderThemeOption,
    annotations: List<AnnotationEntity>,
    selectedTool: AnnotationTool,
    selectedColorHex: Long,
    matchingPageIndices: List<Int>,
    pageTexts: List<String>,
    searchQuery: String,
    contentPadding: PaddingValues = PaddingValues(
        top = 112.dp,
        bottom = 175.dp,
        start = 14.dp,
        end = 14.dp
    ),
    onSingleTap: () -> Unit,
    onPageChanged: (Int) -> Unit,
    onAddAnnotation: (pageIndex: Int, toolType: String, colorHex: Long, strokeWidth: Float, pointsSerialized: String) -> Unit,
    onRequestTextNote: (pageIndex: Int) -> Unit,
    onClearPageAnnotations: (pageIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val currentPageIndex by remember(layoutMode, viewerState) {
        derivedStateOf { viewerState.currentPageIndex(layoutMode) }
    }

    LaunchedEffect(currentPageIndex) {
        onPageChanged(currentPageIndex)
    }

    // Sync listState and pagerState when layoutMode switches
    LaunchedEffect(layoutMode) {
        viewerState.scrollToPage(currentPageIndex, layoutMode, animate = false)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewerState.viewportSize = it }
            .testTag("pdf_document_viewer")
    ) {
        val isAnnotationActive = selectedTool != AnnotationTool.NONE

        // Multi-touch Pinch-to-Zoom & 2D Pan Gesture Detector that coexists with scrolling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isAnnotationActive) {
                    if (!isAnnotationActive) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            var zoom = 1f
                            var pan = Offset.Zero
                            var pastTouchSlop = false
                            val touchSlop = viewConfiguration.touchSlop

                            do {
                                val event = awaitPointerEvent()
                                val canceled = event.changes.any { it.isConsumed }
                                if (!canceled) {
                                    val pointerCount = event.changes.count { it.pressed }
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()

                                    // Intercept when 2+ fingers are pinching OR when already zoomed in
                                    if (pointerCount >= 2 || viewerState.isZoomed) {
                                        if (!pastTouchSlop) {
                                            zoom *= zoomChange
                                            pan += panChange
                                            val centroidSize = event.calculateCentroidSize(useCurrent = false)
                                            val zoomMotion = abs(1f - zoom) * centroidSize
                                            val panMotion = pan.getDistance()

                                            if (zoomMotion > touchSlop ||
                                                (viewerState.isZoomed && panMotion > touchSlop) ||
                                                pointerCount >= 2
                                            ) {
                                                pastTouchSlop = true
                                                viewerState.isPinchGestureActive = true
                                            }
                                        }

                                        if (pastTouchSlop) {
                                            val centroid = event.calculateCentroid(useCurrent = false)
                                            if (zoomChange != 1f || panChange != Offset.Zero) {
                                                viewerState.updateTransformImmediate(
                                                    zoomChange = zoomChange,
                                                    panChange = panChange,
                                                    centroid = centroid
                                                )
                                            }
                                            event.changes.forEach { change ->
                                                if (change.positionChanged()) {
                                                    change.consume()
                                                }
                                            }
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            viewerState.isPinchGestureActive = false
                        }
                    }
                }
                .pointerInput(isAnnotationActive) {
                    if (!isAnnotationActive) {
                        detectTapGestures(
                            onTap = { onSingleTap() },
                            onDoubleTap = { tapOffset ->
                                val nextScale = when {
                                    viewerState.zoomScale < 1.5f -> 2.25f
                                    viewerState.zoomScale < 2.8f -> 3.25f
                                    else -> 1f
                                }
                                viewerState.animateZoomTo(nextScale, focalPoint = tapOffset)
                            }
                        )
                    }
                }
        ) {
            if (layoutMode == PageLayoutOption.SINGLE_PAGE) {
                HorizontalPager(
                    state = viewerState.pagerState,
                    userScrollEnabled = !isAnnotationActive && !viewerState.isZoomed && !viewerState.isPinchGestureActive,
                    contentPadding = contentPadding,
                    pageSpacing = 18.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = viewerState.zoomScale
                            scaleY = viewerState.zoomScale
                            translationX = viewerState.panOffset.x
                            translationY = viewerState.panOffset.y
                        }
                        .testTag("pdf_horizontal_pager")
                ) { pageIdx ->
                    PdfPageItemContent(
                        document = document,
                        pageIdx = pageIdx,
                        zoomScale = viewerState.zoomScale,
                        readerTheme = readerTheme,
                        annotations = annotations,
                        selectedTool = selectedTool,
                        selectedColorHex = selectedColorHex,
                        matchingPageIndices = matchingPageIndices,
                        pageTexts = pageTexts,
                        searchQuery = searchQuery,
                        onAddAnnotation = onAddAnnotation,
                        onRequestTextNote = onRequestTextNote,
                        onClearPageAnnotations = onClearPageAnnotations
                    )
                }
            } else {
                LazyColumn(
                    state = viewerState.listState,
                    userScrollEnabled = !isAnnotationActive && !viewerState.isZoomed && !viewerState.isPinchGestureActive,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = viewerState.zoomScale
                            scaleY = viewerState.zoomScale
                            translationX = viewerState.panOffset.x
                            translationY = viewerState.panOffset.y
                        }
                        .testTag("pdf_vertical_lazy_column"),
                    contentPadding = contentPadding,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    items(count = document.pageCount, key = { "pdf_page_$it" }) { pageIdx ->
                        PdfPageItemContent(
                            document = document,
                            pageIdx = pageIdx,
                            zoomScale = viewerState.zoomScale,
                            readerTheme = readerTheme,
                            annotations = annotations,
                            selectedTool = selectedTool,
                            selectedColorHex = selectedColorHex,
                            matchingPageIndices = matchingPageIndices,
                            pageTexts = pageTexts,
                            searchQuery = searchQuery,
                            onAddAnnotation = onAddAnnotation,
                            onRequestTextNote = onRequestTextNote,
                            onClearPageAnnotations = onClearPageAnnotations
                        )
                    }
                }
            }
        }

        // Interactive Vertical Fast-Scroll Scrubber Track (Right Edge)
        if (document.pageCount > 1 && !viewerState.isZoomed) {
            PdfFastScrollScrubber(
                currentPageIndex = currentPageIndex,
                pageCount = document.pageCount,
                onScrubToPage = { targetPage ->
                    viewerState.scrollToPage(targetPage, layoutMode, animate = false)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 132.dp, bottom = 190.dp, end = 6.dp)
            )
        }

        // Floating Interactive Zoom HUD Pill (Visible when zoomed > 1.04x)
        AnimatedVisibility(
            visible = viewerState.isZoomed,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 114.dp, end = 18.dp)
        ) {
            val accentColor = when {
                glass.isNothingOs -> NothingCrimsonRed
                glass.isGalacticCodex -> WarmGold
                else -> ElectricBlue
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (glass.isDark) Color(0xE60F172A) else Color(0xF0FFFFFF)
                    )
                    .border(1.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("pdf_zoom_hud_pill")
            ) {
                Icon(
                    imageVector = Icons.Filled.ZoomOut,
                    contentDescription = "Zoom out",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            viewerState.animateZoomTo((viewerState.zoomScale - 0.5f).coerceAtLeast(1f))
                        }
                )

                Text(
                    text = String.format(Locale.US, "%d%%", (viewerState.zoomScale * 100).roundToInt()),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = if (glass.isNothingOs) SpaceMonoFamily else MaterialTheme.typography.labelMedium.fontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Icon(
                    imageVector = Icons.Filled.ZoomIn,
                    contentDescription = "Zoom in",
                    tint = glass.textPrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            viewerState.animateZoomTo((viewerState.zoomScale + 0.5f).coerceAtMost(4f))
                        }
                )

                Spacer(modifier = Modifier.width(2.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accentColor.copy(alpha = 0.18f))
                        .clickable { viewerState.resetZoom() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .testTag("pdf_zoom_reset_chip")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CenterFocusStrong,
                            contentDescription = "Reset zoom to fit screen",
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfFastScrollScrubber(
    currentPageIndex: Int,
    pageCount: Int,
    onScrubToPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val density = LocalDensity.current
    var trackHeightPx by remember { mutableIntStateOf(1) }
    var isDraggingScrubber by remember { mutableStateOf(false) }

    val progress = remember(currentPageIndex, pageCount) {
        if (pageCount <= 1) 0f
        else (currentPageIndex.toFloat() / (pageCount - 1).toFloat()).coerceIn(0f, 1f)
    }

    val thumbScale by animateFloatAsState(
        targetValue = if (isDraggingScrubber) 1.15f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "scrubber_scale"
    )

    val accentColor = when {
        glass.isNothingOs -> NothingCrimsonRed
        glass.isGalacticCodex -> WarmGold
        else -> ElectricBlue
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxHeight()
    ) {
        // Floating page callout bubble when actively dragging the scrubber
        AnimatedVisibility(
            visible = isDraggingScrubber,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .shadow(10.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (glass.isDark) Color(0xEE0F172A) else Color(0xF4FFFFFF))
                    .border(1.dp, accentColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Page ${currentPageIndex + 1} / $pageCount",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = accentColor
                )
            }
        }

        // Scrubber Track & Handle
        Box(
            modifier = Modifier
                .width(26.dp)
                .fillMaxHeight()
                .onSizeChanged { trackHeightPx = it.height.coerceAtLeast(1) }
                .pointerInput(pageCount) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            isDraggingScrubber = true
                            val fraction = (startOffset.y / trackHeightPx.toFloat()).coerceIn(0f, 1f)
                            val targetPage = (fraction * (pageCount - 1)).roundToInt()
                            onScrubToPage(targetPage)
                        },
                        onDragEnd = { isDraggingScrubber = false },
                        onDragCancel = { isDraggingScrubber = false },
                        onDrag = { change, _ ->
                            change.consume()
                            val fraction = (change.position.y / trackHeightPx.toFloat()).coerceIn(0f, 1f)
                            val targetPage = (fraction * (pageCount - 1)).roundToInt()
                            onScrubToPage(targetPage)
                        }
                    )
                }
                .testTag("pdf_fast_scroll_scrubber")
        ) {
            // Subtle vertical rail line
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        if (glass.isDark) Color.White.copy(alpha = 0.14f)
                        else Color(0xFF0F172A).copy(alpha = 0.12f)
                    )
            )

            val thumbHeightDp = 42.dp
            val thumbHeightPx = with(density) { thumbHeightDp.toPx() }
            val availableTrackPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(0f)
            val thumbOffsetY = (availableTrackPx * progress).roundToInt()

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset { IntOffset(x = 0, y = thumbOffsetY) }
                    .align(Alignment.TopCenter)
                    .size(width = 24.dp, height = thumbHeightDp)
                    .graphicsLayer {
                        scaleX = thumbScale
                        scaleY = thumbScale
                    }
                    .shadow(6.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isDraggingScrubber) accentColor
                        else if (glass.isDark) Color(0xCC1E293B) else Color(0xE6FFFFFF)
                    )
                    .border(
                        width = 1.2.dp,
                        color = if (isDraggingScrubber) Color.White else accentColor.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.UnfoldMore,
                    contentDescription = "Fast scroll pages",
                    tint = if (isDraggingScrubber) Color.White else accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PdfPageItemContent(
    document: PdfDocumentEntity,
    pageIdx: Int,
    zoomScale: Float,
    readerTheme: ReaderThemeOption,
    annotations: List<AnnotationEntity>,
    selectedTool: AnnotationTool,
    selectedColorHex: Long,
    matchingPageIndices: List<Int>,
    pageTexts: List<String>,
    searchQuery: String,
    onAddAnnotation: (pageIndex: Int, toolType: String, colorHex: Long, strokeWidth: Float, pointsSerialized: String) -> Unit,
    onRequestTextNote: (pageIndex: Int) -> Unit,
    onClearPageAnnotations: (pageIndex: Int) -> Unit
) {
    val pageAnnotations = remember(annotations, pageIdx) {
        annotations.filter { it.pageIndex == pageIdx }
    }
    val hasSearchMatch = matchingPageIndices.contains(pageIdx)
    val matchedSnippet = remember(pageIdx, searchQuery, pageTexts) {
        if (!hasSearchMatch || searchQuery.isBlank()) null
        else {
            val txt = pageTexts.getOrNull(pageIdx) ?: ""
            val pos = txt.indexOf(searchQuery, ignoreCase = true)
            if (pos >= 0) {
                val start = (pos - 35).coerceAtLeast(0)
                val end = (pos + searchQuery.length + 55).coerceAtMost(txt.length)
                "..." + txt.substring(start, end).replace("\n", " ") + "..."
            } else null
        }
    }

    PdfPageSurfaceCard(
        doc = document,
        pageIndex = pageIdx,
        zoomScale = zoomScale,
        readerTheme = readerTheme,
        pageAnnotations = pageAnnotations,
        selectedTool = selectedTool,
        selectedColorHex = selectedColorHex,
        matchedSnippet = matchedSnippet,
        onAddAnnotation = { toolType, colorHex, strokeW, ptsSerialized ->
            onAddAnnotation(pageIdx, toolType, colorHex, strokeW, ptsSerialized)
        },
        onRequestTextNote = { onRequestTextNote(pageIdx) },
        onClearPageAnnotations = { onClearPageAnnotations(pageIdx) }
    )
}

@Composable
private fun PdfPageSurfaceCard(
    doc: PdfDocumentEntity,
    pageIndex: Int,
    zoomScale: Float,
    readerTheme: ReaderThemeOption,
    pageAnnotations: List<AnnotationEntity>,
    selectedTool: AnnotationTool,
    selectedColorHex: Long,
    matchedSnippet: String?,
    onAddAnnotation: (toolType: String, colorHex: Long, strokeWidth: Float, pointsSerialized: String) -> Unit,
    onRequestTextNote: () -> Unit,
    onClearPageAnnotations: () -> Unit
) {
    val context = LocalContext.current
    val glass = LocalGlassColors.current

    // Progressive crisp rendering: upgrade bitmap resolution when user zooms in past 1.6x
    val targetRenderWidth = if (zoomScale > 1.65f) 1920 else 1180

    val baseBitmap by produceState<Bitmap?>(initialValue = null, key1 = doc.filePath, key2 = pageIndex) {
        value = PdfEngine.renderPageHighRes(context, doc.filePath, pageIndex, targetWidth = 1180)
    }

    val hiResBitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = doc.filePath,
        key2 = pageIndex,
        key3 = targetRenderWidth
    ) {
        if (targetRenderWidth > 1180) {
            value = PdfEngine.renderPageHighRes(context, doc.filePath, pageIndex, targetWidth = targetRenderWidth)
        } else {
            value = null
        }
    }

    val activeBitmap = hiResBitmap ?: baseBitmap

    val aspectRatio = remember(activeBitmap) {
        val bmp = activeBitmap
        if (bmp != null && bmp.height > 0 && bmp.width > 0) {
            (bmp.width.toFloat() / bmp.height.toFloat()).coerceIn(0.45f, 2.2f)
        } else {
            595f / 842f
        }
    }

    val currentStrokePoints = remember { mutableStateListOf<Offset>() }

    // Reader Theme Filter (Light, Sepia, Dark)
    val colorFilter = remember(readerTheme) {
        when (readerTheme) {
            ReaderThemeOption.LIGHT -> null
            ReaderThemeOption.SEPIA -> {
                val sepiaMatrix = ColorMatrix(
                    floatArrayOf(
                        0.94f, 0.04f, 0.0f, 0f, 12f,
                        0.02f, 0.88f, 0.02f, 0f, 6f,
                        0.0f, 0.04f, 0.76f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(sepiaMatrix)
            }
            ReaderThemeOption.DARK -> {
                val invertMatrix = ColorMatrix(
                    floatArrayOf(
                        -0.88f, 0f, 0f, 0f, 240f,
                        0f, -0.88f, 0f, 0f, 242f,
                        0f, 0f, -0.85f, 0f, 250f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                ColorFilter.colorMatrix(invertMatrix)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Search Match Highlight Banner above page if matched
        if (matchedSnippet != null) {
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 14.dp,
                tintColor = AnnotYellow
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = SolarAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Match on Page ${pageIndex + 1}: $matchedSnippet",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = glass.textPrimary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .shadow(14.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
        ) {
            if (activeBitmap != null) {
                Image(
                    bitmap = activeBitmap.asImageBitmap(),
                    contentDescription = "Page ${pageIndex + 1} of ${doc.title}",
                    contentScale = ContentScale.FillBounds,
                    colorFilter = colorFilter,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    PlayStoreOrganicBlobSpinner(indicatorSize = 44.dp)
                }
            }

            // Subtle Page Number Corner Watermark Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x990F172A))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${pageIndex + 1} / ${doc.pageCount}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            // Interactive Annotation Drawing & Rendering Overlay
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (selectedTool != AnnotationTool.NONE) {
                            Modifier.pointerInput(selectedTool, selectedColorHex, pageIndex) {
                                if (selectedTool == AnnotationTool.TEXT_NOTE) {
                                    detectTapGestures { onRequestTextNote() }
                                } else if (selectedTool == AnnotationTool.ERASER) {
                                    detectTapGestures { onClearPageAnnotations() }
                                } else {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentStrokePoints.clear()
                                            currentStrokePoints.add(
                                                Offset(
                                                    (offset.x / size.width).coerceIn(0f, 1f),
                                                    (offset.y / size.height).coerceIn(0f, 1f)
                                                )
                                            )
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            currentStrokePoints.add(
                                                Offset(
                                                    (change.position.x / size.width).coerceIn(0f, 1f),
                                                    (change.position.y / size.height).coerceIn(0f, 1f)
                                                )
                                            )
                                        },
                                        onDragEnd = {
                                            if (currentStrokePoints.size >= 2) {
                                                val serialized = currentStrokePoints.joinToString(";") {
                                                    String.format(Locale.US, "%.4f,%.4f", it.x, it.y)
                                                }
                                                val strokeW = when (selectedTool) {
                                                    AnnotationTool.HIGHLIGHT -> 26f
                                                    AnnotationTool.MARKER -> 18f
                                                    AnnotationTool.PEN -> 5.5f
                                                    AnnotationTool.UNDERLINE, AnnotationTool.STRIKETHROUGH -> 4.5f
                                                    else -> 6f
                                                }
                                                onAddAnnotation(
                                                    selectedTool.name,
                                                    selectedColorHex,
                                                    strokeW,
                                                    serialized
                                                )
                                            }
                                            currentStrokePoints.clear()
                                        }
                                    )
                                }
                            }
                        } else Modifier
                    )
            ) {
                val w = size.width
                val h = size.height

                // Saved annotations on this page
                pageAnnotations.forEach { annot ->
                    val rawColor = Color(annot.colorHex)
                    val alpha = if (annot.toolType == AnnotationTool.HIGHLIGHT.name || annot.toolType == AnnotationTool.MARKER.name) 0.42f else 0.92f
                    val pts = annot.pointsSerialized.split(";").mapNotNull { token ->
                        val parts = token.split(",")
                        if (parts.size == 2) {
                            val nx = parts[0].toFloatOrNull()
                            val ny = parts[1].toFloatOrNull()
                            if (nx != null && ny != null) Offset(nx * w, ny * h) else null
                        } else null
                    }
                    if (pts.size >= 2) {
                        if (annot.toolType == AnnotationTool.UNDERLINE.name || annot.toolType == AnnotationTool.STRIKETHROUGH.name) {
                            val first = pts.first()
                            val last = pts.last()
                            drawLine(
                                color = rawColor.copy(alpha = alpha),
                                start = Offset(first.x, first.y),
                                end = Offset(last.x, first.y),
                               strokeWidth = annot.strokeWidth,
                                cap = StrokeCap.Round
                            )
                        } else {
                            val path = Path().apply {
                                pts.forEachIndexed { idx, pt ->
                                    if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = rawColor.copy(alpha = alpha),
                                style = Stroke(
                                    width = annot.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // Live stroke being drawn
                if (currentStrokePoints.size >= 2) {
                    val liveColor = Color(selectedColorHex)
                    val alpha = if (selectedTool == AnnotationTool.HIGHLIGHT || selectedTool == AnnotationTool.MARKER) 0.42f else 0.92f
                    val livePath = Path().apply {
                        currentStrokePoints.forEachIndexed { idx, pt ->
                            if (idx == 0) moveTo(pt.x * w, pt.y * h) else lineTo(pt.x * w, pt.y * h)
                        }
                    }
                    drawPath(
                        path = livePath,
                        color = liveColor.copy(alpha = alpha),
                        style = Stroke(
                            width = if (selectedTool == AnnotationTool.HIGHLIGHT) 26f else 7f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Sticky Text Note Badges on this page
            val textNotes = pageAnnotations.filter { it.toolType == AnnotationTool.TEXT_NOTE.name && it.textNoteContent.isNotBlank() }
            if (textNotes.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    textNotes.forEach { tn ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(tn.colorHex).copy(alpha = 0.92f))
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "📌 ${tn.textNoteContent}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }
    }
}
