package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AppSettings
import com.example.data.BookmarkEntity
import com.example.data.PageLayoutOption
import com.example.data.PdfDocumentEntity
import com.example.data.ReaderThemeOption
import com.example.pdf.PdfEngine
import com.example.ui.GlassPaperViewModel
import com.example.ui.components.GlassCircularIconButton
import com.example.ui.components.LiquidGlassDropdownMenu
import com.example.ui.components.LiquidGlassDropdownMenuItem
import com.example.ui.components.LiquidGlassPanel
import com.example.ui.components.PdfDocumentViewer
import com.example.ui.components.rememberPdfDocumentViewerState
import com.example.ui.theme.AnnotBlue
import com.example.ui.theme.AnnotGreen
import com.example.ui.theme.AnnotPink
import com.example.ui.theme.AnnotPurple
import com.example.ui.theme.AnnotYellow
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMagenta
import com.example.ui.theme.LocalGlassColors
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber
import java.util.Locale

enum class AnnotationTool(val label: String) {
    NONE("Read"),
    HIGHLIGHT("Highlight"),
    UNDERLINE("Underline"),
    STRIKETHROUGH("Strike"),
    PEN("Pen"),
    MARKER("Marker"),
    TEXT_NOTE("Text Note"),
    ERASER("Eraser")
}

@Composable
fun PdfReaderScreen(
    viewModel: GlassPaperViewModel,
    document: PdfDocumentEntity?,
    initialPage: Int,
    bookmarks: List<BookmarkEntity>,
    settings: AppSettings,
    onBack: () -> Unit,
    onCreateNoteFromPage: (docId: Long, docTitle: String, pageNumber: Int, snippet: String) -> Unit
) {
    BackHandler(onBack = onBack)

    val glass = LocalGlassColors.current
    val context = LocalContext.current

    if (document == null) {
        // Beautiful Glass Error State
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                tintColor = LiquidMagenta
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.error_open_pdf_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.error_open_pdf_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(onClick = onBack) {
                        Text("Back to Library")
                    }
                }
            }
        }
        return
    }

    val annotations by viewModel.getAnnotationsForDoc(document.id).collectAsState(initial = emptyList())

    var isPasswordUnlocked by remember(document.id, document.passwordProtectionHash) {
        mutableStateOf(document.passwordProtectionHash.isBlank())
    }
    var passwordInput by remember(document.id) { mutableStateOf("") }
    var passwordError by remember(document.id) { mutableStateOf<String?>(null) }

    if (!isPasswordUnlocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 30.dp,
                tintColor = PrismViolet
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrismViolet.copy(alpha = 0.2f))
                            .border(1.5.dp, PrismViolet.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Protected PDF",
                            tint = PrismViolet,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Text(
                        text = "Protected PDF Document",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Enter the password to open \"${document.title}\" in Paperflow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = null
                        },
                        label = { Text("Document Password") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reader_password_input")
                    )
                    passwordError?.let { err ->
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = LiquidMagenta
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val enteredHash = PdfEngine.hashDocumentPassword(passwordInput)
                                if (enteredHash == document.passwordProtectionHash) {
                                    isPasswordUnlocked = true
                                } else {
                                    passwordError = "Incorrect password. Please try again."
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reader_unlock_button")
                        ) {
                            Text("Unlock PDF")
                        }
                    }
                }
            }
        }
        return
    }

    val startPage = remember(document.id, initialPage) {
        if (initialPage >= 0) initialPage.coerceIn(0, (document.pageCount - 1).coerceAtLeast(0))
        else document.lastReadPage.coerceIn(0, (document.pageCount - 1).coerceAtLeast(0))
    }

    val viewerState = rememberPdfDocumentViewerState(
        documentId = document.id,
        initialPage = startPage,
        pageCount = document.pageCount
    )

    val layoutMode = settings.pageLayout
    val currentPageIndex by remember(layoutMode, viewerState) {
        derivedStateOf { viewerState.currentPageIndex(layoutMode) }
    }

    var controlsVisible by remember { mutableStateOf(true) }
    var showThumbnailsStrip by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentMatchCursor by remember { mutableIntStateOf(0) }

    var showAnnotationBar by remember { mutableStateOf(false) }
    var selectedTool by remember { mutableStateOf(AnnotationTool.NONE) }
    var selectedColorHex by remember { mutableStateOf(0xFFFACC15L) } // Yellow default

    var showMoreMenu by remember { mutableStateOf(false) }
    var showAddTextDialogForPage by remember { mutableStateOf<Int?>(null) }
    var textAnnotationInput by remember { mutableStateOf("") }

    // Search matches calculation across document pages
    val pageTexts = remember(document.searchableText) {
        if (document.searchableText.isBlank()) emptyList()
        else document.searchableText.split("||PAGE||")
    }

    val matchingPageIndices = remember(searchQuery, pageTexts) {
        if (searchQuery.isBlank() || pageTexts.isEmpty()) emptyList()
        else pageTexts.mapIndexedNotNull { idx, pageText ->
            if (pageText.contains(searchQuery, ignoreCase = true)) idx else null
        }
    }

    val isCurrentPageBookmarked = remember(bookmarks, document.id, currentPageIndex) {
        bookmarks.any { it.documentId == document.id && it.pageIndex == currentPageIndex }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pdf_reader_screen")
    ) {
        // 1. Main Reusable PDF Document Viewer Component (Zoom, Pan, Scroll & Pager)
        PdfDocumentViewer(
            document = document,
            viewerState = viewerState,
            layoutMode = layoutMode,
            readerTheme = settings.readerTheme,
            annotations = annotations,
            selectedTool = selectedTool,
            selectedColorHex = selectedColorHex,
            matchingPageIndices = matchingPageIndices,
            pageTexts = pageTexts,
            searchQuery = searchQuery,
            onSingleTap = {
                if (selectedTool == AnnotationTool.NONE) {
                    controlsVisible = !controlsVisible
                }
            },
            onPageChanged = { pageIdx ->
                viewModel.updateReadingProgress(document.id, pageIdx)
            },
            onAddAnnotation = { pageIdx, toolType, colorHex, strokeW, ptsSerialized ->
                viewModel.addAnnotation(
                    docId = document.id,
                    pageIndex = pageIdx,
                    toolType = toolType,
                    colorHex = colorHex,
                    strokeWidth = strokeW,
                    pointsSerialized = ptsSerialized
                )
            },
            onRequestTextNote = { pageIdx ->
                showAddTextDialogForPage = pageIdx
                textAnnotationInput = ""
            },
            onClearPageAnnotations = { pageIdx ->
                viewModel.clearPageAnnotations(document.id, pageIdx)
            }
        )

        // 2. Top Floating Glass Toolbar + Optional Search Bar
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidGlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    tintColor = ElectricBlue,
                    shadowElevation = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassCircularIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            onClick = onBack,
                            size = 42.dp,
                            modifier = Modifier.testTag("reader_back_button")
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = document.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val modeLabel = if (layoutMode == PageLayoutOption.SINGLE_PAGE) "Single Page" else "Continuous"
                            val zoomLabel = if (viewerState.isZoomed) {
                                String.format(Locale.US, "%.1fx Zoom", viewerState.zoomScale)
                            } else {
                                "Fit Width"
                            }
                            Text(
                                text = "Page ${currentPageIndex + 1} of ${document.pageCount} • $modeLabel • $zoomLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = glass.textSecondary
                            )
                        }

                        // Quick Scroll Direction Toggle (Vertical Continuous vs Single-Page Horizontal)
                        IconButton(
                            onClick = {
                                val nextLayout = if (layoutMode == PageLayoutOption.CONTINUOUS) {
                                    PageLayoutOption.SINGLE_PAGE
                                } else {
                                    PageLayoutOption.CONTINUOUS
                                }
                                viewModel.setPageLayout(nextLayout)
                            },
                            modifier = Modifier.testTag("reader_layout_mode_button")
                        ) {
                            Icon(
                                imageVector = if (layoutMode == PageLayoutOption.SINGLE_PAGE) {
                                    Icons.Filled.SwapHoriz
                                } else {
                                    Icons.Filled.SwapVert
                                },
                                contentDescription = "Toggle scroll mode",
                                tint = glass.textPrimary
                            )
                        }

                        IconButton(
                            onClick = { showSearchBar = !showSearchBar },
                            modifier = Modifier.testTag("reader_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Search in document",
                                tint = if (showSearchBar) ElectricBlue else glass.textPrimary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleBookmark(document, currentPageIndex) },
                            modifier = Modifier.testTag("reader_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isCurrentPageBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark page",
                                tint = if (isCurrentPageBookmarked) SolarAmber else glass.textPrimary
                            )
                        }

                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More reader options",
                                    tint = glass.textPrimary
                                )
                            }
                            LiquidGlassDropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                LiquidGlassDropdownMenuItem(
                                    text = if (showThumbnailsStrip) "Hide Page Thumbnails"
                                    else "Show Page Thumbnails",
                                    onClick = {
                                        showThumbnailsStrip = !showThumbnailsStrip
                                        showMoreMenu = false
                                    }
                                )
                                LiquidGlassDropdownMenuItem(
                                    text = if (layoutMode == PageLayoutOption.CONTINUOUS) "Switch to Single-Page Swipe"
                                    else "Switch to Continuous Scroll",
                                    onClick = {
                                        val nextLayout = if (layoutMode == PageLayoutOption.CONTINUOUS) {
                                            PageLayoutOption.SINGLE_PAGE
                                        } else {
                                            PageLayoutOption.CONTINUOUS
                                        }
                                        viewModel.setPageLayout(nextLayout)
                                        showMoreMenu = false
                                    }
                                )
                                LiquidGlassDropdownMenuItem(
                                    text = "Fit Width (1.0x)",
                                    onClick = {
                                        viewerState.resetZoom()
                                        showMoreMenu = false
                                    }
                                )
                                LiquidGlassDropdownMenuItem(
                                    text = "Reader Theme: ${settings.readerTheme.name}",
                                    onClick = {
                                        val next = when (settings.readerTheme) {
                                            ReaderThemeOption.LIGHT -> ReaderThemeOption.SEPIA
                                            ReaderThemeOption.SEPIA -> ReaderThemeOption.DARK
                                            ReaderThemeOption.DARK -> ReaderThemeOption.LIGHT
                                        }
                                        viewModel.setReaderTheme(next)
                                        showMoreMenu = false
                                    }
                                )
                                LiquidGlassDropdownMenuItem(
                                    text = "Create Study Note from Page",
                                    onClick = {
                                        showMoreMenu = false
                                        val pageSnippet = pageTexts.getOrNull(currentPageIndex)?.take(180) ?: ""
                                        onCreateNoteFromPage(
                                            document.id,
                                            document.title,
                                            currentPageIndex + 1,
                                            pageSnippet
                                        )
                                    }
                                )
                                LiquidGlassDropdownMenuItem(
                                    text = "Share PDF",
                                    showDivider = false,
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.shareDocument(context, document)
                                    }
                                )
                            }
                        }
                    }
                }

                // Expandable PDF Search Panel
                AnimatedVisibility(visible = showSearchBar) {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        tintColor = PrismViolet
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.search_in_document),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = glass.textMuted
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = {
                                            searchQuery = it
                                            currentMatchCursor = 0
                                        },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = glass.textPrimary),
                                        cursorBrush = SolidColor(ElectricBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (matchingPageIndices.isNotEmpty()) {
                                    Text(
                                        text = "${currentMatchCursor + 1}/${matchingPageIndices.size}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ElectricBlue
                                    )
                                    IconButton(
                                        onClick = {
                                            if (matchingPageIndices.isNotEmpty()) {
                                                currentMatchCursor = (currentMatchCursor - 1 + matchingPageIndices.size) % matchingPageIndices.size
                                                viewerState.scrollToPage(matchingPageIndices[currentMatchCursor], layoutMode)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Previous result", tint = glass.textPrimary)
                                    }
                                    IconButton(
                                        onClick = {
                                            if (matchingPageIndices.isNotEmpty()) {
                                                currentMatchCursor = (currentMatchCursor + 1) % matchingPageIndices.size
                                                viewerState.scrollToPage(matchingPageIndices[currentMatchCursor], layoutMode)
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Next result", tint = glass.textPrimary)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        showSearchBar = false
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close search", tint = glass.textSecondary)
                                }
                            }

                            if (document.isScannedOnly && document.searchableText.isBlank()) {
                                Text(
                                    text = stringResource(R.string.scanned_pdf_notice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SolarAmber
                                )
                            } else if (searchQuery.isNotBlank() && matchingPageIndices.isEmpty()) {
                                Text(
                                    text = "No matching pages found for \"$searchQuery\"",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LiquidMagenta
                                )
                            } else if (matchingPageIndices.isNotEmpty()) {
                                Text(
                                    text = "Matches found on pages: ${matchingPageIndices.joinToString(", ") { (it + 1).toString() }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CrystalTeal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Bottom Floating Glass Controls + Thumbnail Strip + Annotation Palette
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Optional Page Thumbnails Strip
                AnimatedVisibility(visible = showThumbnailsStrip) {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        tintColor = LiquidCyan
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(document.pageCount) { idx ->
                                val isCurrent = idx == currentPageIndex
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isCurrent) 2.dp else 1.dp,
                                            color = if (isCurrent) ElectricBlue else Color.White.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(Color.White.copy(alpha = if (isCurrent) 0.9f else 0.5f))
                                        .clickable {
                                            viewerState.scrollToPage(idx, layoutMode)
                                        }
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "P.${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isCurrent) ElectricBlue else Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    }
                }

                // Expandable PDF Annotation Studio Toolbar
                AnimatedVisibility(visible = showAnnotationBar) {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        tintColor = PrismPurple,
                        shadowElevation = 14.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Tool Selector Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnnotationTool.entries.forEach { tool ->
                                    val active = selectedTool == tool
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (active) ElectricBlue else Color.White.copy(alpha = if (glass.isDark) 0.12f else 0.65f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (active) Color.White else Color.White.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { selectedTool = tool }
                                            .padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = tool.label,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (active) Color.White else glass.textPrimary
                                        )
                                    }
                                }
                            }

                            // 5 Annotation Colors Palette: Yellow, Green, Blue, Pink, Purple + Clear Page
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    val colors = listOf(
                                        0xFFFACC15L to AnnotYellow,
                                        0xFF22C55EL to AnnotGreen,
                                        0xFF3B82F6L to AnnotBlue,
                                        0xFFEC4899L to AnnotPink,
                                        0xFFA855F7L to AnnotPurple
                                    )
                                    colors.forEach { (hex, composeColor) ->
                                        val selected = selectedColorHex == hex
                                        Box(
                                            modifier = Modifier
                                                .size(if (selected) 32.dp else 26.dp)
                                                .clip(CircleShape)
                                                .background(composeColor)
                                                .border(
                                                    width = if (selected) 2.5.dp else 1.dp,
                                                    color = Color.White,
                                                    shape = CircleShape
                                                )
                                                .clickable { selectedColorHex = hex }
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LiquidMagenta.copy(alpha = 0.16f))
                                        .clickable {
                                            viewModel.clearPageAnnotations(document.id, currentPageIndex)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteSweep,
                                        contentDescription = "Clear annotations",
                                        tint = LiquidMagenta,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Clear Page",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = LiquidMagenta
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Floating Glass Capsule: Previous | "5 / 28" | Next | Zoom | Bookmark | Annotation
                LiquidGlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 34.dp,
                    tintColor = ElectricBlue,
                    shadowElevation = 18.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page
                        IconButton(
                            onClick = {
                                val target = (currentPageIndex - 1).coerceAtLeast(0)
                                viewerState.scrollToPage(target, layoutMode)
                            },
                            enabled = currentPageIndex > 0
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous page",
                                tint = if (currentPageIndex > 0) glass.textPrimary else glass.textMuted
                            )
                        }

                        // Page Indicator Capsule e.g. "1 / 4"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(ElectricBlue.copy(alpha = 0.18f))
                                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                                .clickable { showThumbnailsStrip = !showThumbnailsStrip }
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${currentPageIndex + 1} / ${document.pageCount}",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = glass.textPrimary
                            )
                        }

                        // Next Page
                        IconButton(
                            onClick = {
                                val target = (currentPageIndex + 1).coerceAtMost(document.pageCount - 1)
                                viewerState.scrollToPage(target, layoutMode)
                            },
                            enabled = currentPageIndex < document.pageCount - 1
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next page",
                                tint = if (currentPageIndex < document.pageCount - 1) glass.textPrimary else glass.textMuted
                            )
                        }

                        // Smooth Spring Zoom Cycle
                        IconButton(
                            onClick = { viewerState.stepZoomCycle() },
                            modifier = Modifier.testTag("reader_zoom_button")
                        ) {
                            Icon(
                                imageVector = if (viewerState.isZoomed) Icons.Filled.ZoomOut else Icons.Filled.ZoomIn,
                                contentDescription = "Zoom",
                                tint = if (viewerState.isZoomed) ElectricBlue else glass.textPrimary
                            )
                        }

                        // Bookmark Toggle
                        IconButton(onClick = { viewModel.toggleBookmark(document, currentPageIndex) }) {
                            Icon(
                                imageVector = if (isCurrentPageBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isCurrentPageBookmarked) SolarAmber else glass.textPrimary
                            )
                        }

                        // Annotation Toggle
                        IconButton(
                            onClick = {
                                showAnnotationBar = !showAnnotationBar
                                if (!showAnnotationBar) selectedTool = AnnotationTool.NONE
                                else if (selectedTool == AnnotationTool.NONE) selectedTool = AnnotationTool.HIGHLIGHT
                            },
                            modifier = Modifier.testTag("reader_annotate_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Draw,
                                contentDescription = "Annotate PDF",
                                tint = if (showAnnotationBar) IridescentPink else glass.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // Text Annotation Dialog
    showAddTextDialogForPage?.let { targetPageIdx ->
        AlertDialog(
            onDismissRequest = { showAddTextDialogForPage = null },
            title = { Text("Add Page Annotation Note") },
            text = {
                OutlinedTextField(
                    value = textAnnotationInput,
                    onValueChange = { textAnnotationInput = it },
                    label = { Text("Annotation comment") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textAnnotationInput.isNotBlank()) {
                            viewModel.addAnnotation(
                                docId = document.id,
                                pageIndex = targetPageIdx,
                                toolType = AnnotationTool.TEXT_NOTE.name,
                                colorHex = selectedColorHex,
                                strokeWidth = 4f,
                                pointsSerialized = "0.15,0.15",
                                textNoteContent = textAnnotationInput.trim()
                            )
                        }
                        showAddTextDialogForPage = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialogForPage = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
