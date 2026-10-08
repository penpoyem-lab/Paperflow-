package com.example

import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.auth.PaperflowAuthManager
import com.example.data.AppStyleOption
import com.example.data.AppThemeOption
import com.example.data.GlassPaperDatabase
import com.example.data.GlassPaperRepository
import com.example.data.SettingsDataStore
import com.example.data.ThemeModeOption
import com.example.ui.ActiveOverlay
import com.example.ui.GlassPaperViewModel
import com.example.ui.GlassPaperViewModelFactory
import com.example.ui.MainTab
import com.example.ui.components.FloatingAiChatbotOverlay
import com.example.ui.components.FloatingGlassNavigationBar
import com.example.ui.components.LiquidAmbientBackground
import com.example.ui.components.PaperflowFlashIntroOverlay
import com.example.ui.components.PlayStoreSystemLoadingOverlay
import com.example.ui.screens.AuthenticationScreen
import com.example.ui.screens.BookmarksOverlay
import com.example.ui.screens.GitHubRepositoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NoteEditorOverlay
import com.example.ui.screens.PdfReaderScreen
import com.example.ui.screens.SelectPdfOverlay
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StreakCelebrationModal
import com.example.ui.screens.StreakDetailsOverlay
import com.example.ui.screens.ThemeScreen
import com.example.ui.screens.ToolWorkspaceOverlay
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassPaperTheme

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            GlassPaperDatabase::class.java,
            "paperflow.db"
        ).fallbackToDestructiveMigration(true).build()
    }

    private val repository by lazy {
        GlassPaperRepository(applicationContext, database.dao())
    }

    private val settingsDataStore by lazy {
        SettingsDataStore(applicationContext)
    }

    private val authManager by lazy {
        PaperflowAuthManager(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFormat(PixelFormat.RGBA_8888)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.colorMode = ActivityInfo.COLOR_MODE_DEFAULT
        }
        requestHighRefreshRate165Hz()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: GlassPaperViewModel = viewModel(
                factory = GlassPaperViewModelFactory(
                    applicationContext,
                    repository,
                    settingsDataStore,
                    authManager
                )
            )
            GlassPaperApp(viewModel = vm)
        }
    }

    /**
     * Proactively requests the highest display refresh rate (120Hz / 144Hz / 165Hz)
     * supported by the device or streaming display pipeline.
     */
    private fun requestHighRefreshRate165Hz() {
        try {
            val params = window.attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = display
                val modes = display?.supportedModes
                val highestMode = modes?.maxByOrNull { it.refreshRate }
                if (highestMode != null) {
                    params.preferredDisplayModeId = highestMode.modeId
                    params.preferredRefreshRate = highestMode.refreshRate.coerceAtLeast(165f)
                } else {
                    params.preferredRefreshRate = 165f
                }
            } else {
                params.preferredRefreshRate = 165f
            }
            window.attributes = params
        } catch (_: Exception) {
            // Safely ignore on displays that lock refresh rate
        }
    }
}

@Composable
fun GlassPaperApp(viewModel: GlassPaperViewModel) {
    val authSession by viewModel.authSession.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val streakData by viewModel.streakData.collectAsStateWithLifecycle()
    val streakCelebrationEvent by viewModel.streakCelebrationEvent.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeOverlay by viewModel.activeOverlay.collectAsStateWithLifecycle()
    val libraryCategory by viewModel.libraryCategory.collectAsStateWithLifecycle()
    val librarySortOrder by viewModel.librarySortOrder.collectAsStateWithLifecycle()
    val librarySearchQuery by viewModel.librarySearchQuery.collectAsStateWithLifecycle()
    val toolsSearchQuery by viewModel.toolsSearchQuery.collectAsStateWithLifecycle()
    val importPreview by viewModel.importPreview.collectAsStateWithLifecycle()
    val toolState by viewModel.toolState.collectAsStateWithLifecycle()
    val isAppStartingLoading by viewModel.isAppStartingLoading.collectAsStateWithLifecycle()
    val isPageRefreshing by viewModel.isPageRefreshing.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val aiChatMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val isAiChatPanelOpen by viewModel.isAiChatPanelOpen.collectAsStateWithLifecycle()
    val isAiChatMaximized by viewModel.isAiChatMaximized.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val activeReaderPageIndex by viewModel.activeReaderPageIndex.collectAsStateWithLifecycle()

    // Google Play Store–style transparent pull-down-to-refresh gesture detector
    val density = LocalDensity.current
    val pullTriggerThresholdPx = remember(density) { with(density) { 82.dp.toPx() } }
    val topPullZoneHeightPx = remember(density) { with(density) { 260.dp.toPx() } }
    var overscrollPullPx by remember { mutableFloatStateOf(0f) }

    val animatedPullProgress by animateFloatAsState(
        targetValue = (overscrollPullPx / pullTriggerThresholdPx).coerceIn(0f, 1.35f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pull_to_refresh_progress"
    )

    val pullRefreshNestedScroll = remember(activeOverlay, isPageRefreshing, isAppStartingLoading, pullTriggerThresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // If user pushes back up while pulling down, reduce the pull distance smoothly
                if (source == NestedScrollSource.UserInput && available.y < 0f && overscrollPullPx > 0f) {
                    val consumedY = available.y.coerceAtLeast(-overscrollPullPx)
                    overscrollPullPx = (overscrollPullPx + consumedY).coerceAtLeast(0f)
                    return Offset(0f, consumedY)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (activeOverlay is ActiveOverlay.PdfReader || activeOverlay is ActiveOverlay.NoteEditor) {
                    return Offset.Zero
                }
                if (source == NestedScrollSource.UserInput && available.y > 0f) {
                    // Accumulate unconsumed downward drag at the top of any LazyColumn / screen
                    overscrollPullPx = (overscrollPullPx + available.y * 0.85f).coerceAtMost(pullTriggerThresholdPx * 1.6f)
                    if (overscrollPullPx >= pullTriggerThresholdPx && !isPageRefreshing && !isAppStartingLoading) {
                        overscrollPullPx = 0f
                        viewModel.refreshAppPage()
                    }
                } else if (consumed.y < -2f) {
                    overscrollPullPx = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (overscrollPullPx >= pullTriggerThresholdPx * 0.72f && !isPageRefreshing && !isAppStartingLoading) {
                    overscrollPullPx = 0f
                    viewModel.refreshAppPage()
                } else {
                    overscrollPullPx = 0f
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                overscrollPullPx = 0f
                return Velocity.Zero
            }
        }
    }

    val systemDark = isSystemInDarkTheme()
    val isGalacticCodex = settings.appStyle == AppStyleOption.JOURNEY_AWAITS_UI ||
        settings.appTheme == AppThemeOption.GALACTIC_CODEX
    val isNothingOs = settings.appStyle == AppStyleOption.NOTHING_UI ||
        settings.appTheme == AppThemeOption.NOTHING_OS_GLASS
    val isDesertDuneClay = settings.appStyle == AppStyleOption.DESERT_DUNE_CLAY_UI ||
        settings.appTheme == AppThemeOption.DESERT_DUNE_CLAY
    val isEnchantedForestCodex = settings.appStyle == AppStyleOption.ENCHANTED_FOREST_UI ||
        settings.appTheme == AppThemeOption.ENCHANTED_FOREST_CODEX
    val useDarkTheme = when {
        isDesertDuneClay -> false
        isEnchantedForestCodex -> true
        isNothingOs -> true
        isGalacticCodex -> true
        else -> when (settings.themeMode) {
            ThemeModeOption.LIGHT -> false
            ThemeModeOption.DARK -> true
            ThemeModeOption.SYSTEM -> systemDark
        }
    }

    // Show the full-screen Paperflow Liquid Glass Authentication experience when requested
    var hasPassedAuthGateInSession by remember { mutableStateOf(true) }
    var showFlashIntro by remember { mutableStateOf(true) }

    var pendingSourceLabel by remember { mutableStateOf("Device") }

    // Zero-permission Android Storage Access Framework (SAF) PDF Document Picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onSystemPdfSelected(uri, pendingSourceLabel)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Handle back navigation when on a non-Home tab without an active overlay
    BackHandler(enabled = activeOverlay is ActiveOverlay.None && currentTab != MainTab.HOME) {
        viewModel.selectTab(MainTab.HOME)
    }

    GlassPaperTheme(
        darkTheme = useDarkTheme,
        isGalacticCodex = isGalacticCodex,
        isNothingOs = isNothingOs,
        isDesertDuneClay = isDesertDuneClay,
        isEnchantedForestCodex = isEnchantedForestCodex
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!authSession.isAuthenticated && !hasPassedAuthGateInSession) {
                AuthenticationScreen(
                    authManager = viewModel.authManager,
                    sessionState = authSession,
                    onAuthCompleted = {
                        hasPassedAuthGateInSession = true
                        viewModel.closeOverlay()
                    },
                    onCloseOrDismiss = {
                        hasPassedAuthGateInSession = true
                    }
                )
            } else {
                    LiquidAmbientBackground {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(pullRefreshNestedScroll)
                                .pointerInput(activeOverlay, isPageRefreshing, isAppStartingLoading, pullTriggerThresholdPx, topPullZoneHeightPx) {
                                    if (activeOverlay is ActiveOverlay.PdfReader || activeOverlay is ActiveOverlay.NoteEditor) {
                                        return@pointerInput
                                    }
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                        // Allow pulling down from the top header / status / hero area of any screen
                                        if (down.position.y > topPullZoneHeightPx) return@awaitEachGesture
                                        var dragDy = 0f
                                        var dragDx = 0f
                                        var trackingVerticalPull = false

                                        while (true) {
                                            val event = awaitPointerEvent(PointerEventPass.Initial)
                                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                            if (!change.pressed) {
                                                if (overscrollPullPx >= pullTriggerThresholdPx * 0.72f && !isPageRefreshing && !isAppStartingLoading) {
                                                    overscrollPullPx = 0f
                                                    viewModel.refreshAppPage()
                                                } else {
                                                    overscrollPullPx = 0f
                                                }
                                                break
                                            }
                                            val delta = change.position - change.previousPosition
                                            dragDy += delta.y
                                            dragDx += abs(delta.x)

                                            if (!trackingVerticalPull && dragDy > 14f && dragDy > dragDx * 1.25f) {
                                                trackingVerticalPull = true
                                            }
                                            if (trackingVerticalPull) {
                                                if (dragDy > 0f) {
                                                    overscrollPullPx = (dragDy * 0.78f).coerceIn(0f, pullTriggerThresholdPx * 1.55f)
                                                    if (overscrollPullPx >= pullTriggerThresholdPx * 1.05f && !isPageRefreshing && !isAppStartingLoading) {
                                                        overscrollPullPx = 0f
                                                        viewModel.refreshAppPage()
                                                        break
                                                    }
                                                } else {
                                                    overscrollPullPx = 0f
                                                }
                                            }
                                        }
                                    }
                                }
                        ) {
                            when (val overlay = activeOverlay) {
                                is ActiveOverlay.Authentication -> {
                                    AuthenticationScreen(
                                        authManager = viewModel.authManager,
                                        sessionState = authSession,
                                        onAuthCompleted = {
                                            hasPassedAuthGateInSession = true
                                            viewModel.closeOverlay()
                                        },
                                        onCloseOrDismiss = {
                                            viewModel.closeOverlay()
                                        }
                                    )
                                }

                                is ActiveOverlay.PdfReader -> {
                                    val targetDoc = documents.firstOrNull { it.id == overlay.documentId }
                                    PdfReaderScreen(
                                        viewModel = viewModel,
                                        document = targetDoc,
                                        initialPage = overlay.initialPage,
                                        bookmarks = bookmarks,
                                        settings = settings,
                                        onBack = { viewModel.closeOverlay() },
                                        onCreateNoteFromPage = { docId, docTitle, pageNum, snippet ->
                                            viewModel.openNoteEditor(
                                                attachedDocId = docId,
                                                attachedDocTitle = docTitle,
                                                attachedPage = pageNum,
                                                prefilledSelectedText = snippet
                                            )
                                        }
                                    )
                                }

                                is ActiveOverlay.NoteEditor -> {
                                    val existing = notes.firstOrNull { it.id == overlay.noteId }
                                    NoteEditorOverlay(
                                        viewModel = viewModel,
                                        existingNote = existing,
                                        attachedDocId = overlay.attachedDocId,
                                        attachedDocTitle = overlay.attachedDocTitle,
                                        attachedPage = overlay.attachedPage,
                                        prefilledSnippet = overlay.prefilledSelectedText,
                                        documents = documents,
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                is ActiveOverlay.SelectPdfSheet -> {
                                    SelectPdfOverlay(
                                        documents = documents,
                                        importPreview = importPreview,
                                        onPickFromSystem = { sourceLabel ->
                                            pendingSourceLabel = sourceLabel
                                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                        },
                                        onOpenPdf = { doc ->
                                            viewModel.openPdfReader(doc.id)
                                        },
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                is ActiveOverlay.BookmarksSheet -> {
                                    BookmarksOverlay(
                                        bookmarks = bookmarks,
                                        documents = documents,
                                        onOpenBookmark = { docId, pageIdx ->
                                            viewModel.openPdfReader(docId, pageIdx)
                                        },
                                        onDeleteBookmark = { bmId ->
                                            viewModel.deleteBookmark(bmId)
                                        },
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                is ActiveOverlay.StreakDetails -> {
                                    StreakDetailsOverlay(
                                        streakData = streakData,
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                is ActiveOverlay.GitHubRepository -> {
                                    GitHubRepositoryScreen(
                                        onBack = { viewModel.closeOverlay() },
                                        onShowMessage = { msg -> viewModel.showMessage(msg) }
                                    )
                                }

                                is ActiveOverlay.ThemePage -> {
                                    ThemeScreen(
                                        settings = settings,
                                        onSelectAppStyle = { style -> viewModel.setAppStyle(style) },
                                        onSelectThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                        onResetDefaults = { viewModel.resetThemeToDefaults() },
                                        onBack = { viewModel.closeOverlay() }
                                    )
                                }

                                is ActiveOverlay.ToolWorkspace -> {
                                    ToolWorkspaceOverlay(
                                        viewModel = viewModel,
                                        toolId = overlay.toolId,
                                        documents = documents,
                                        toolState = toolState,
                                        onBack = { viewModel.closeOverlay() },
                                        onOpenResultDocument = { doc ->
                                            viewModel.openPdfReader(doc.id)
                                        }
                                    )
                                }

                                ActiveOverlay.None -> {
                                    // Main 4 Destinations: HOME | TOOLS | LIBRARY | SETTINGS
                                    // Ultra-fast 110ms transition for instant responsiveness
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Crossfade(
                                            targetState = currentTab,
                                            animationSpec = tween(durationMillis = 110, easing = FastOutSlowInEasing),
                                            label = "main_tab_fast_transition"
                                        ) { tab ->
                                            when (tab) {
                                                MainTab.HOME -> {
                                                    HomeScreen(
                                                        viewModel = viewModel,
                                                        documents = documents,
                                                        streakData = streakData,
                                                        authSession = authSession,
                                                        onOpenSelectPdf = { viewModel.openSelectPdfSheet() },
                                                        onOpenPdf = { doc -> viewModel.openPdfReader(doc.id) },
                                                        onOpenNotes = { viewModel.openNoteEditor() },
                                                        onOpenBookmarks = { viewModel.openBookmarksSheet() },
                                                        onOpenStreakDetails = { viewModel.openStreakDetails() },
                                                        onOpenAuth = { viewModel.openAuthentication() },
                                                        onOpenGitHubRepo = { viewModel.openGitHubRepository() },
                                                        onReplayFlashIntro = { showFlashIntro = true },
                                                        onRefreshPage = { viewModel.refreshAppPage() },
                                                        onNavigateToLibrary = { category ->
                                                            viewModel.setLibraryCategory(category)
                                                            viewModel.selectTab(MainTab.LIBRARY)
                                                        }
                                                    )
                                                }

                                                MainTab.TOOLS -> {
                                                    ToolsScreen(
                                                        viewModel = viewModel,
                                                        searchQuery = toolsSearchQuery,
                                                        onSelectTool = { tool ->
                                                            viewModel.openToolWorkspace(tool.id)
                                                        }
                                                    )
                                                }

                                                MainTab.LIBRARY -> {
                                                    LibraryScreen(
                                                        viewModel = viewModel,
                                                        documents = documents,
                                                        notes = notes,
                                                        selectedCategory = libraryCategory,
                                                        sortOrder = librarySortOrder,
                                                        searchQuery = librarySearchQuery,
                                                        isGridView = settings.isGridViewInLibrary,
                                                        onImportPdf = { viewModel.openSelectPdfSheet() },
                                                        onScanDocument = { viewModel.openToolWorkspace("image_to_pdf") },
                                                        onCreateNote = { viewModel.openNoteEditor() },
                                                        onOpenPdf = { doc -> viewModel.openPdfReader(doc.id) },
                                                        onOpenNote = { note -> viewModel.openNoteEditor(noteId = note.id) }
                                                    )
                                                }

                                                MainTab.SETTINGS -> {
                                                    SettingsScreen(
                                                        viewModel = viewModel,
                                                        settings = settings,
                                                        documents = documents,
                                                        authSession = authSession
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Floating VisionOS-Inspired Translucent Liquid Glass Bottom Navigation Bar
                                    FloatingGlassNavigationBar(
                                        currentTab = currentTab,
                                        onSelectTab = { selected ->
                                            if (selected == currentTab) {
                                                // Tapping the already active tab refreshes the page with the Android #101010 loading screen
                                                viewModel.refreshAppPage()
                                            } else {
                                                viewModel.selectTab(selected)
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }

                            // Daily Streak Celebration Modal (triggers ONLY once per calendar day after startup loading finishes)
                            if (activeOverlay !is ActiveOverlay.Authentication && !isAppStartingLoading && !isPageRefreshing && !showFlashIntro) {
                                streakCelebrationEvent?.let { event ->
                                    StreakCelebrationModal(
                                        event = event,
                                        hapticEnabled = settings.hapticFeedback,
                                        onContinue = { viewModel.dismissStreakCelebration() },
                                        onViewDetails = { viewModel.openStreakDetails() }
                                    )
                                }
                            }

                            // Global Draggable Floating Liquid-Glass AI Chatbot Assistant
                            // Works across Home, Tools, Library, Settings, PDF Reader, Notes, and Overlays
                            if (!isAppStartingLoading && !isPageRefreshing && !showFlashIntro) {
                                FloatingAiChatbotOverlay(
                                    settings = settings,
                                    activeOverlay = activeOverlay,
                                    activeReaderPageIndex = activeReaderPageIndex,
                                    documents = documents,
                                    notes = notes,
                                    messages = aiChatMessages,
                                    isChatOpen = isAiChatPanelOpen,
                                    isChatMaximized = isAiChatMaximized,
                                    isAiThinking = isAiThinking,
                                    onToggleChat = { viewModel.toggleAiChatPanel() },
                                    onCloseChat = { viewModel.closeAiChatPanel() },
                                    onToggleMaximize = { viewModel.toggleAiChatMaximized() },
                                    onSendMessage = { prompt -> viewModel.sendAiChatMessage(prompt) },
                                    onRegenerateLast = { viewModel.regenerateLastAiResponse() },
                                    onClearConversation = { viewModel.clearAiChatConversation() },
                                    onSaveResponseToNote = { content, badge ->
                                        viewModel.saveAiResponseToNote(content, badge)
                                    },
                                    onSaveButtonPosition = { normX, normY ->
                                        viewModel.setAiButtonPosition(normX, normY)
                                    },
                                    onShowSnackbar = { msg -> viewModel.showMessage(msg) }
                                )
                            }

                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 104.dp, start = 16.dp, end = 16.dp)
                            ) { data ->
                                Snackbar(
                                    containerColor = ElectricBlue,
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                ) {
                                    Text(data.visuals.message)
                                }
                            }
                        }
                    }
            }

            // Ultra-smooth 3.0-second Apple VisionOS + Liquid Glass Flash Intro
            // Seamlessly morphs the translucent glass icon into the Home screen's top-left header orb
            PaperflowFlashIntroOverlay(
                visible = showFlashIntro,
                appName = "PAPERFLOW",
                onIntroFinished = {
                    showFlashIntro = false
                }
            )

            // Full-Screen Transparent Google Play Store Organic Scalloped Blob Loading & Pull-to-Refresh Overlay
            // Triggers on app startup, page refresh (pull-to-refresh / menu refresh / re-tapping active tab), and heavy content loading
            PlayStoreSystemLoadingOverlay(
                visible = isAppStartingLoading || isPageRefreshing || toolState.isRunning,
                pullProgress = animatedPullProgress
            )
        }
    }
}
