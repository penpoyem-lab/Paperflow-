package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (settings.appTheme) {
        AppThemeOption.LIGHT -> false
        AppThemeOption.DARK -> true
        AppThemeOption.SYSTEM -> systemDark
    }

    // Show the full-screen Paperflow Liquid Glass Authentication experience on initial launch
    // until the user signs in, creates an account, or chooses to continue to their workspace.
    var hasPassedAuthGateInSession by remember { mutableStateOf(false) }

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

    GlassPaperTheme(darkTheme = useDarkTheme) {
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
                            // Main 4 Destinations: HOME | TOOLS | LIBRARY | SETTINGS
                            Crossfade(targetState = currentTab, label = "main_tab_crossfade") { tab ->
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

                            // Floating VisionOS-Inspired Translucent Liquid Glass Bottom Navigation Bar
                            FloatingGlassNavigationBar(
                                currentTab = currentTab,
                                onSelectTab = { viewModel.selectTab(it) },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }

                    // Daily Streak Celebration Modal (triggers ONLY once per calendar day when a new check-in is registered)
                    if (activeOverlay !is ActiveOverlay.Authentication) {
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
}
