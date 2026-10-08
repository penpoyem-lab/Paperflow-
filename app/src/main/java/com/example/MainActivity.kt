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
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.auth.PaperflowAuthManager
import com.example.data.AppThemeOption
import com.example.data.GlassPaperDatabase
import com.example.data.GlassPaperRepository
import com.example.data.SettingsDataStore
import com.example.ui.ActiveOverlay
import com.example.ui.GlassPaperViewModel
import com.example.ui.GlassPaperViewModelFactory
import com.example.ui.MainTab
import com.example.ui.components.FloatingGlassNavigationBar
import com.example.ui.components.LiquidAmbientBackground
import com.example.ui.components.PaperflowFlashIntroOverlay
import com.example.ui.components.PlayStoreSystemLoadingOverlay
import com.example.ui.screens.AuthenticationScreen
import com.example.ui.screens.BookmarksOverlay
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NoteEditorOverlay
import com.example.ui.screens.PdfReaderScreen
import com.example.ui.screens.SelectPdfOverlay
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StreakCelebrationModal
import com.example.ui.screens.StreakDetailsOverlay
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
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val isGalacticCodex = settings.appTheme == AppThemeOption.GALACTIC_CODEX
    val useDarkTheme = when (settings.appTheme) {
        AppThemeOption.LIGHT -> false
        AppThemeOption.DARK -> true
        AppThemeOption.SYSTEM -> systemDark
        AppThemeOption.GALACTIC_CODEX -> true
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

    var shouldRenderMainTree by remember { mutableStateOf(false) }
    LaunchedEffect(isAppStartingLoading) {
        if (!isAppStartingLoading) {
            shouldRenderMainTree = true
        } else {
            kotlinx.coroutines.delay(180)
            shouldRenderMainTree = true
        }
    }

    GlassPaperTheme(darkTheme = useDarkTheme, isGalacticCodex = isGalacticCodex) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (shouldRenderMainTree) {
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
                        Box(modifier = Modifier.fillMaxSize()) {
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
                                    var scrollCompression by remember { mutableFloatStateOf(0f) }
                                    var lastScrollTimeMs by remember { mutableLongStateOf(0L) }

                                    val nestedScrollConnection = remember {
                                        object : NestedScrollConnection {
                                            override fun onPostScroll(
                                                consumed: Offset,
                                                available: Offset,
                                                source: NestedScrollSource
                                            ): Offset {
                                                if (kotlin.math.abs(consumed.y) > 1.5f) {
                                                    lastScrollTimeMs = System.currentTimeMillis()
                                                    scrollCompression = if (consumed.y < 0f) {
                                                        // Scrolling down -> subtle downward float & slightly softer opacity
                                                        (scrollCompression + (-consumed.y / 180f)).coerceIn(0f, 1f)
                                                    } else {
                                                        // Scrolling up -> smoothly return toward rest
                                                        (scrollCompression - (consumed.y / 140f)).coerceIn(0f, 1f)
                                                    }
                                                }
                                                return Offset.Zero
                                            }
                                        }
                                    }

                                    // Smoothly return the floating navigation bar to its resting state when scrolling stops
                                    LaunchedEffect(lastScrollTimeMs) {
                                        if (lastScrollTimeMs > 0L) {
                                            kotlinx.coroutines.delay(260)
                                            scrollCompression = 0f
                                        }
                                    }

                                    // Main 4 Destinations: HOME | TOOLS | LIBRARY | SETTINGS
                                    // Smooth directional horizontal movement + fade transition while keeping the navigation bar stable
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .nestedScroll(nestedScrollConnection)
                                    ) {
                                        AnimatedContent(
                                            targetState = currentTab,
                                            transitionSpec = {
                                                val forward = targetState.ordinal >= initialState.ordinal
                                                val slideDistance = { fullWidth: Int -> (fullWidth * 0.065f).toInt() }
                                                (fadeIn(
                                                    animationSpec = tween(durationMillis = 190, easing = FastOutSlowInEasing)
                                                ) +
                                                    slideInHorizontally(
                                                        animationSpec = spring(
                                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                                            stiffness = Spring.StiffnessMedium
                                                        ),
                                                        initialOffsetX = { fullWidth ->
                                                            if (forward) slideDistance(fullWidth) else -slideDistance(fullWidth)
                                                        }
                                                    ))
                                                    .togetherWith(
                                                        fadeOut(
                                                            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
                                                        ) +
                                                            slideOutHorizontally(
                                                                animationSpec = spring(
                                                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                                                    stiffness = Spring.StiffnessMedium
                                                                ),
                                                                targetOffsetX = { fullWidth ->
                                                                    if (forward) -slideDistance(fullWidth) else slideDistance(fullWidth)
                                                                }
                                                            )
                                                    )
                                                    .using(SizeTransform(clip = false))
                                            },
                                            label = "main_tab_liquid_transition"
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
                                        onSelectTab = { viewModel.selectTab(it) },
                                        scrollCompressionProgress = scrollCompression,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }

                            // Daily Streak Celebration Modal (triggers ONLY once per calendar day after the Flash Intro finishes)
                            if (activeOverlay !is ActiveOverlay.Authentication && !isAppStartingLoading && !showFlashIntro) {
                                streakCelebrationEvent?.let { event ->
                                    StreakCelebrationModal(
                                        event = event,
                                        hapticEnabled = settings.hapticFeedback,
                                        onContinue = { viewModel.dismissStreakCelebration() },
                                        onViewDetails = { viewModel.openStreakDetails() }
                                    )
                                }
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

            // Full-Screen Play Store / Android System Organic Scalloped Blob Loading Screen (#101010) for heavy PDF tools
            PlayStoreSystemLoadingOverlay(
                visible = toolState.isRunning
            )
        }
    }
}
