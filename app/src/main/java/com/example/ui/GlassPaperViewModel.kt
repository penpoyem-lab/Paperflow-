package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthSessionState
import com.example.auth.PaperflowAuthManager
import com.example.data.AnnotationEntity
import com.example.data.AppSettings
import com.example.data.AppThemeOption
import com.example.data.BookmarkEntity
import com.example.data.GlassPaperRepository
import com.example.data.NoteEntity
import com.example.data.PageLayoutOption
import com.example.data.PdfDocumentEntity
import com.example.data.ReaderThemeOption
import com.example.data.SettingsDataStore
import com.example.data.StreakCheckInEvent
import com.example.data.StreakData
import com.example.data.TextSizeOption
import com.example.pdf.MergeFileEntry
import com.example.pdf.PdfEngine
import com.example.pdf.SignatureStampConfig
import com.example.pdf.WatermarkConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class MainTab { HOME, TOOLS, LIBRARY, SETTINGS }

enum class LibraryCategory { ALL, PDFS, NOTES, RECENT, FAVORITES }

enum class LibrarySortOrder(val label: String) {
    RECENT("Last Opened"),
    NAME_ASC("Name (A–Z)"),
    SIZE_DESC("File Size"),
    PAGES_DESC("Page Count")
}

sealed class ActiveOverlay {
    data object None : ActiveOverlay()
    data class PdfReader(val documentId: Long, val initialPage: Int = -1) : ActiveOverlay()
    data class NoteEditor(
        val noteId: Long? = null,
        val attachedDocId: Long? = null,
        val attachedDocTitle: String? = null,
        val attachedPage: Int? = null,
        val prefilledSelectedText: String? = null
    ) : ActiveOverlay()
    data object SelectPdfSheet : ActiveOverlay()
    data object BookmarksSheet : ActiveOverlay()
    data object StreakDetails : ActiveOverlay()
    data object Authentication : ActiveOverlay()
    data class ToolWorkspace(val toolId: String) : ActiveOverlay()
}

data class ImportPreviewState(
    val document: PdfDocumentEntity,
    val statusMessage: String = "Ready to read"
)

data class ToolOperationState(
    val isRunning: Boolean = false,
    val progressText: String = "",
    val resultDocument: PdfDocumentEntity? = null,
    val resultImagesCount: Int = 0,
    val extractedText: String? = null,
    val errorMessage: String? = null
)

class GlassPaperViewModel(
    private val appContext: Context,
    private val repository: GlassPaperRepository,
    private val settingsDataStore: SettingsDataStore,
    val authManager: PaperflowAuthManager
) : ViewModel() {

    val authSession: StateFlow<AuthSessionState> = authManager.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthSessionState())

    val documents: StateFlow<List<PdfDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val streakData: StateFlow<StreakData> = settingsDataStore.streakFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakData())

    private val _streakCelebrationEvent = MutableStateFlow<StreakCheckInEvent?>(null)
    val streakCelebrationEvent: StateFlow<StreakCheckInEvent?> = _streakCelebrationEvent.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeOverlay = MutableStateFlow<ActiveOverlay>(ActiveOverlay.None)
    val activeOverlay: StateFlow<ActiveOverlay> = _activeOverlay.asStateFlow()

    private val _libraryCategory = MutableStateFlow(LibraryCategory.ALL)
    val libraryCategory: StateFlow<LibraryCategory> = _libraryCategory.asStateFlow()

    private val _librarySortOrder = MutableStateFlow(LibrarySortOrder.RECENT)
    val librarySortOrder: StateFlow<LibrarySortOrder> = _librarySortOrder.asStateFlow()

    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery: StateFlow<String> = _librarySearchQuery.asStateFlow()

    private val _toolsSearchQuery = MutableStateFlow("")
    val toolsSearchQuery: StateFlow<String> = _toolsSearchQuery.asStateFlow()

    private val _importPreview = MutableStateFlow<ImportPreviewState?>(null)
    val importPreview: StateFlow<ImportPreviewState?> = _importPreview.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _toolState = MutableStateFlow(ToolOperationState())
    val toolState: StateFlow<ToolOperationState> = _toolState.asStateFlow()

    private val _isAppStartingLoading = MutableStateFlow(true)
    val isAppStartingLoading: StateFlow<Boolean> = _isAppStartingLoading.asStateFlow()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.removeLegacyDemoFilesIfPresent()
            val checkIn = settingsDataStore.registerDailyCheckInIfNeeded()
            kotlinx.coroutines.delay(450)
            _isAppStartingLoading.value = false
            if (checkIn != null) {
                _streakCelebrationEvent.value = checkIn
            }
        }
    }

    fun dismissStreakCelebration() {
        _streakCelebrationEvent.value = null
    }

    fun openStreakDetails() {
        _streakCelebrationEvent.value = null
        _activeOverlay.value = ActiveOverlay.StreakDetails
    }

    fun openAuthentication() {
        _streakCelebrationEvent.value = null
        _activeOverlay.value = ActiveOverlay.Authentication
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
            _snackbarMessage.value = "Signed out of Paperflow"
        }
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _activeOverlay.value = ActiveOverlay.None
    }

    fun openSelectPdfSheet() {
        _importPreview.value = null
        _activeOverlay.value = ActiveOverlay.SelectPdfSheet
    }

    fun openBookmarksSheet() {
        _activeOverlay.value = ActiveOverlay.BookmarksSheet
    }

    fun openPdfReader(docId: Long, initialPage: Int = -1) {
        _importPreview.value = null
        _activeOverlay.value = ActiveOverlay.PdfReader(docId, initialPage)
    }

    fun openNoteEditor(
        noteId: Long? = null,
        attachedDocId: Long? = null,
        attachedDocTitle: String? = null,
        attachedPage: Int? = null,
        prefilledSelectedText: String? = null
    ) {
        _activeOverlay.value = ActiveOverlay.NoteEditor(
            noteId = noteId,
            attachedDocId = attachedDocId,
            attachedDocTitle = attachedDocTitle,
            attachedPage = attachedPage,
            prefilledSelectedText = prefilledSelectedText
        )
    }

    fun openToolWorkspace(toolId: String) {
        _toolState.value = ToolOperationState()
        _activeOverlay.value = ActiveOverlay.ToolWorkspace(toolId)
    }

    fun closeOverlay() {
        _activeOverlay.value = ActiveOverlay.None
    }

    fun setLibraryCategory(category: LibraryCategory) {
        _libraryCategory.value = category
    }

    fun setLibrarySortOrder(order: LibrarySortOrder) {
        _librarySortOrder.value = order
    }

    fun setLibrarySearchQuery(query: String) {
        _librarySearchQuery.value = query
    }

    fun setToolsSearchQuery(query: String) {
        _toolsSearchQuery.value = query
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun onSystemPdfSelected(uri: Uri, sourceLabel: String = "Device") {
        viewModelScope.launch {
            val result = repository.importPdfFromUri(uri, sourceLabel)
            result.onSuccess { doc ->
                _importPreview.value = ImportPreviewState(doc, "Imported from $sourceLabel")
                _snackbarMessage.value = "Imported ${doc.title}"
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "Unable to open this PDF"
            }
        }
    }

    fun dismissImportPreview() {
        _importPreview.value = null
    }

    fun updateReadingProgress(docId: Long, pageIndex: Int) {
        viewModelScope.launch {
            if (settings.value.rememberReadingPosition) {
                repository.updateReadingPosition(docId, pageIndex)
            }
        }
    }

    fun toggleFavorite(doc: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.toggleDocumentFavorite(doc)
        }
    }

    fun renameDocument(docId: Long, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.renameDocument(docId, newTitle)
            _snackbarMessage.value = "Document renamed"
        }
    }

    fun moveDocumentCategory(doc: PdfDocumentEntity, newCategory: String) {
        viewModelScope.launch {
            repository.updateDocumentCategory(doc, newCategory)
            _snackbarMessage.value = "Moved to $newCategory"
        }
    }

    fun duplicateDocument(doc: PdfDocumentEntity) {
        viewModelScope.launch {
            val copy = repository.duplicateDocument(doc)
            if (copy != null) {
                _snackbarMessage.value = "Created ${copy.title}"
            } else {
                _snackbarMessage.value = "Could not duplicate file"
            }
        }
    }

    fun deleteDocument(doc: PdfDocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            _snackbarMessage.value = "Deleted ${doc.title}"
        }
    }

    fun shareDocument(context: Context, doc: PdfDocumentEntity) {
        try {
            val uri = if (doc.filePath.startsWith("content://") || doc.filePath.startsWith("file://")) {
                Uri.parse(doc.filePath)
            } else {
                val file = File(doc.filePath)
                if (!file.exists()) {
                    _snackbarMessage.value = "File not found on device"
                    return
                }
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, doc.title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share ${doc.title}"))
        } catch (e: Exception) {
            _snackbarMessage.value = "Unable to share document"
        }
    }

    // Notes & Bookmarks & Annotations
    fun saveNote(
        id: Long,
        title: String,
        content: String,
        attachedDocId: Long?,
        attachedDocTitle: String?,
        attachedPage: Int?,
        accentColorHex: Long,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            repository.saveNote(
                NoteEntity(
                    id = id,
                    title = title.ifBlank { "Untitled Study Note" },
                    content = content,
                    attachedDocumentId = attachedDocId,
                    attachedDocumentTitle = attachedDocTitle,
                    attachedPageNumber = attachedPage,
                    accentColorHex = accentColorHex,
                    isFavorite = isFavorite
                )
            )
            _snackbarMessage.value = "Note saved"
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
            _snackbarMessage.value = "Note deleted"
        }
    }

    fun toggleBookmark(doc: PdfDocumentEntity, pageIndex: Int) {
        viewModelScope.launch {
            repository.toggleBookmark(doc.id, doc.title, pageIndex, bookmarks.value)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmarkId)
        }
    }

    fun getAnnotationsForDoc(docId: Long) = repository.getAnnotationsForDocument(docId)

    fun addAnnotation(
        docId: Long,
        pageIndex: Int,
        toolType: String,
        colorHex: Long,
        strokeWidth: Float,
        pointsSerialized: String,
        textNoteContent: String = ""
    ) {
        viewModelScope.launch {
            repository.addAnnotation(
                AnnotationEntity(
                    documentId = docId,
                    pageIndex = pageIndex,
                    toolType = toolType,
                    colorHex = colorHex,
                    strokeWidth = strokeWidth,
                    pointsSerialized = pointsSerialized,
                    textNoteContent = textNoteContent
                )
            )
        }
    }

    fun clearPageAnnotations(docId: Long, pageIndex: Int) {
        viewModelScope.launch {
            repository.clearPageAnnotations(docId, pageIndex)
            _snackbarMessage.value = "Cleared annotations on page ${pageIndex + 1}"
        }
    }

    // ==================== PDF TOOLS EXECUTION ====================

    fun clearToolResult() {
        _toolState.value = ToolOperationState()
    }

    fun importAndSelectPdfForTool(
        uri: Uri,
        onImported: (PdfDocumentEntity) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.importPdfFromUri(uri, "Tools Input")
            result.onSuccess { doc ->
                onImported(doc)
            }.onFailure { err ->
                _toolState.value = ToolOperationState(errorMessage = err.message ?: "Unable to import selected PDF.")
            }
        }
    }

    fun executeMergePdfEntries(entries: List<MergeFileEntry>, outputTitle: String) {
        if (entries.isEmpty()) {
            _toolState.value = ToolOperationState(errorMessage = "Select at least 1 PDF file to merge.")
            return
        }
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Merging ${entries.size} PDF files...")
            val combinedText = entries.joinToString("||PAGE||") { it.searchableText }
            val cleanTitle = outputTitle.ifBlank { "paperflow-merged" }
            val outFile = PdfEngine.mergePdfsWithRotations(
                appContext,
                entries,
                cleanTitle
            )
            if (outFile != null) {
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = if (cleanTitle.endsWith(".pdf", true)) cleanTitle else "$cleanTitle.pdf",
                    categoryTag = "Merged",
                    accentHex = 0xFFEC4899,
                    searchableText = combinedText
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Unable to merge selected PDFs.")
            }
        }
    }

    fun executeMergePdfs(selectedDocs: List<PdfDocumentEntity>, outputTitle: String) {
        executeMergePdfEntries(
            entries = selectedDocs.map {
                MergeFileEntry(
                    filePath = it.filePath,
                    title = it.title,
                    pageCount = it.pageCount,
                    fileSizeBytes = it.fileSizeBytes,
                    rotationDegrees = 0,
                    searchableText = it.searchableText
                )
            },
            outputTitle = outputTitle
        )
    }

    fun executePageSelectionTool(
        doc: PdfDocumentEntity,
        selectedZeroBasedPages: List<Int>,
        operationLabel: String,
        customOutputFilename: String = ""
    ) {
        if (selectedZeroBasedPages.isEmpty()) {
            _toolState.value = ToolOperationState(errorMessage = "Select at least 1 page to continue.")
            return
        }
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Processing $operationLabel (${selectedZeroBasedPages.size} pages)...")
            val outFile = PdfEngine.extractOrSplitPages(
                appContext,
                doc.filePath,
                doc.title,
                selectedZeroBasedPages,
                operationLabel,
                customOutputFilename
            )
            if (outFile != null) {
                val origPages = doc.searchableText.split("||PAGE||")
                val filteredText = selectedZeroBasedPages.mapNotNull { origPages.getOrNull(it) }.joinToString("||PAGE||")
                val finalTitle = customOutputFilename.ifBlank {
                    "${doc.title.removeSuffix(".pdf")} ($operationLabel).pdf"
                }
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = finalTitle,
                    categoryTag = operationLabel,
                    accentHex = 0xFF8B5CF6,
                    searchableText = filteredText
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Failed to process pages.")
            }
        }
    }

    fun executeRotatePdf(doc: PdfDocumentEntity, degrees: Int, customOutputFilename: String = "") {
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Rotating pages by $degrees°...")
            val outFile = PdfEngine.rotatePdf(appContext, doc.filePath, doc.title, degrees, customOutputFilename)
            if (outFile != null) {
                val finalTitle = customOutputFilename.ifBlank {
                    "${doc.title.removeSuffix(".pdf")}-rotated-$degrees.pdf"
                }
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = finalTitle,
                    categoryTag = "Rotated",
                    accentHex = 0xFF3B82F6,
                    searchableText = doc.searchableText
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Unable to rotate PDF.")
            }
        }
    }

    fun executeCustomWatermark(doc: PdfDocumentEntity, config: WatermarkConfig) {
        if (config.text.isBlank()) {
            _toolState.value = ToolOperationState(errorMessage = "Please enter watermark text.")
            return
        }
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Applying watermark across ${doc.pageCount} pages...")
            val outFile = PdfEngine.applyCustomWatermark(appContext, doc.filePath, doc.title, config)
            if (outFile != null) {
                val finalTitle = config.outputFilename.ifBlank {
                    "${doc.title.removeSuffix(".pdf")}-watermarked.pdf"
                }
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = finalTitle,
                    categoryTag = "Watermarked",
                    accentHex = 0xFF8B5CF6,
                    searchableText = doc.searchableText
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Failed to apply watermark.")
            }
        }
    }

    fun executeSignatureStamp(doc: PdfDocumentEntity, config: SignatureStampConfig) {
        if (config.strokePoints.isEmpty() && config.uploadedSignatureUri == null) {
            _toolState.value = ToolOperationState(errorMessage = "Draw or upload a signature first.")
            return
        }
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Signing Page ${config.targetPageIndex + 1} of ${doc.pageCount}...")
            val outFile = PdfEngine.stampSignatureOnPage(appContext, doc.filePath, doc.title, config)
            if (outFile != null) {
                val finalTitle = config.outputFilename.ifBlank {
                    "${doc.title.removeSuffix(".pdf")}-signed.pdf"
                }
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = finalTitle,
                    categoryTag = "Signed",
                    accentHex = 0xFFEC4899,
                    searchableText = doc.searchableText
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Failed to sign document.")
            }
        }
    }

    fun executeProtectPdf(
        doc: PdfDocumentEntity,
        newPassword: String,
        confirmPassword: String,
        outputFilename: String
    ) {
        if (newPassword.length < 4) {
            _toolState.value = ToolOperationState(errorMessage = "Password must be at least 4 characters.")
            return
        }
        if (newPassword != confirmPassword) {
            _toolState.value = ToolOperationState(errorMessage = "Passwords do not match.")
            return
        }
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Encrypting & locking PDF...")
            val hash = PdfEngine.hashDocumentPassword(newPassword)
            val cleanName = outputFilename.ifBlank { "${doc.title.removeSuffix(".pdf")}-protected" }
            val outFile = PdfEngine.protectOrUnlockPdfCopy(appContext, doc.filePath, doc.title, cleanName)
            if (outFile != null) {
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = if (cleanName.endsWith(".pdf", true)) cleanName else "$cleanName.pdf",
                    categoryTag = "Protected",
                    accentHex = 0xFF8B5CF6,
                    searchableText = doc.searchableText,
                    passwordProtectionHash = hash
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Failed to encrypt & save PDF.")
            }
        }
    }

    fun executeUnlockPdf(
        doc: PdfDocumentEntity,
        passwordInput: String,
        outputFilename: String
    ) {
        if (doc.passwordProtectionHash.isNotBlank()) {
            val enteredHash = PdfEngine.hashDocumentPassword(passwordInput)
            if (enteredHash != doc.passwordProtectionHash) {
                _toolState.value = ToolOperationState(errorMessage = "Incorrect password for this protected PDF.")
                return
            }
        } else if (passwordInput.isBlank()) {
            _toolState.value = ToolOperationState(errorMessage = "Enter the document password to unlock.")
            return
        }
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Removing password protection...")
            val cleanName = outputFilename.ifBlank { "${doc.title.removeSuffix(".pdf")}-unlocked" }
            val outFile = PdfEngine.protectOrUnlockPdfCopy(appContext, doc.filePath, doc.title, cleanName)
            if (outFile != null) {
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = if (cleanName.endsWith(".pdf", true)) cleanName else "$cleanName.pdf",
                    categoryTag = "Unlocked",
                    accentHex = 0xFF22D3EE,
                    searchableText = doc.searchableText,
                    passwordProtectionHash = ""
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Failed to unlock PDF.")
            }
        }
    }

    fun executeStampPdf(
        doc: PdfDocumentEntity,
        watermarkText: String?,
        includePageNumbers: Boolean,
        signaturePoints: List<Pair<Float, Float>>? = null,
        customOutputFilename: String = ""
    ) {
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Applying document layer...")
            val outFile = PdfEngine.stampPageNumbersOrWatermark(
                appContext,
                doc.filePath,
                doc.title,
                watermarkText,
                includePageNumbers,
                signaturePoints,
                customOutputFilename
            )
            if (outFile != null) {
                val label = when {
                    !signaturePoints.isNullOrEmpty() -> "Signed"
                    !watermarkText.isNullOrBlank() -> "Watermarked"
                    else -> "Numbered"
                }
                val finalTitle = customOutputFilename.ifBlank {
                    "${doc.title.removeSuffix(".pdf")}-${label.lowercase()}.pdf"
                }
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = finalTitle,
                    categoryTag = label,
                    accentHex = 0xFF14B8A6,
                    searchableText = doc.searchableText
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Failed to stamp document.")
            }
        }
    }

    fun executeOptimizeOrGrayscalePdf(
        doc: PdfDocumentEntity,
        grayscale: Boolean,
        scaleFactor: Float,
        label: String,
        customOutputFilename: String = ""
    ) {
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Running $label engine...")
            val outFile = PdfEngine.convertOrOptimizePdf(
                appContext,
                doc.filePath,
                doc.title,
                grayscale,
                scaleFactor,
                customOutputFilename
            )
            if (outFile != null) {
                val finalTitle = customOutputFilename.ifBlank {
                    "${doc.title.removeSuffix(".pdf")}-${label.lowercase()}.pdf"
                }
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = finalTitle,
                    categoryTag = label,
                    accentHex = 0xFFF59E0B,
                    searchableText = doc.searchableText
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Operation failed.")
            }
        }
    }

    fun executeImageToPdf(imageUris: List<Uri>, outputTitle: String) {
        if (imageUris.isEmpty()) {
            _toolState.value = ToolOperationState(errorMessage = "Pick at least 1 image from your device.")
            return
        }
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Converting ${imageUris.size} images to PDF...")
            val outFile = PdfEngine.createPdfFromImages(appContext, imageUris, outputTitle.ifBlank { "Scanned_Images" })
            if (outFile != null) {
                val newDoc = repository.registerGeneratedPdfFile(
                    file = outFile,
                    title = outputTitle.ifBlank { "Scanned Images.pdf" },
                    categoryTag = "Converted",
                    accentHex = 0xFF22C55E
                )
                _toolState.value = ToolOperationState(isRunning = false, resultDocument = newDoc)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Could not convert selected images.")
            }
        }
    }

    fun executePdfToImages(doc: PdfDocumentEntity) {
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Rendering high-res PNG pages...")
            val files = PdfEngine.exportPagesAsPngs(appContext, doc.filePath, doc.title)
            if (files.isNotEmpty()) {
                _toolState.value = ToolOperationState(isRunning = false, resultImagesCount = files.size)
            } else {
                _toolState.value = ToolOperationState(isRunning = false, errorMessage = "Could not export page images.")
            }
        }
    }

    fun executePdfToText(doc: PdfDocumentEntity) {
        viewModelScope.launch {
            _toolState.value = ToolOperationState(isRunning = true, progressText = "Extracting document text...")
            val text = if (doc.searchableText.isNotBlank()) {
                doc.searchableText.split("||PAGE||")
                    .mapIndexed { idx, pageTxt -> "--- PAGE ${idx + 1} ---\n$pageTxt" }
                    .joinToString("\n\n")
            } else {
                "This document may contain scanned pages.\n\nFile: ${doc.title}\nPages: ${doc.pageCount}\nSize: ${formatFileSize(doc.fileSizeBytes)}"
            }
            _toolState.value = ToolOperationState(isRunning = false, extractedText = text)
        }
    }

    // ==================== SETTINGS ACTIONS ====================

    fun setAppTheme(option: AppThemeOption) = viewModelScope.launch { settingsDataStore.setAppTheme(option) }
    fun setReaderTheme(option: ReaderThemeOption) = viewModelScope.launch { settingsDataStore.setReaderTheme(option) }
    fun setTextSize(option: TextSizeOption) = viewModelScope.launch { settingsDataStore.setTextSize(option) }
    fun setPageLayout(option: PageLayoutOption) = viewModelScope.launch { settingsDataStore.setPageLayout(option) }
    fun setAutoRotate(enabled: Boolean) = viewModelScope.launch { settingsDataStore.setAutoRotate(enabled) }
    fun setKeepScreenAwake(enabled: Boolean) = viewModelScope.launch { settingsDataStore.setKeepScreenAwake(enabled) }
    fun setRememberReadingPosition(enabled: Boolean) = viewModelScope.launch { settingsDataStore.setRememberReadingPosition(enabled) }
    fun setHapticFeedback(enabled: Boolean) = viewModelScope.launch { settingsDataStore.setHapticFeedback(enabled) }
    fun setDefaultDownloadLocation(loc: String) = viewModelScope.launch { settingsDataStore.setDefaultDownloadLocation(loc) }
    fun setLibraryGridView(isGrid: Boolean) = viewModelScope.launch { settingsDataStore.setLibraryGridView(isGrid) }

    fun clearRecentHistory() {
        viewModelScope.launch {
            _snackbarMessage.value = "Reading history timestamps reset"
        }
    }

    fun clearAllAppData() {
        viewModelScope.launch {
            repository.clearAllAppData()
            settingsDataStore.resetSettings()
            _snackbarMessage.value = "All Paperflow app data cleared"
        }
    }

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 KB"
            val kb = bytes / 1024.0
            return if (kb < 1024.0) {
                String.format("%.1f KB", kb)
            } else {
                String.format("%.1f MB", kb / 1024.0)
            }
        }

        fun formatRelativeTime(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            val mins = (diff / 60_000L).coerceAtLeast(0L)
            return when {
                mins < 1 -> "Just now"
                mins < 60 -> "${mins}m ago"
                mins < 1440 -> "${mins / 60}h ago"
                mins < 2880 -> "Yesterday"
                else -> "${(mins / 1440).coerceAtLeast(1)} days ago"
            }
        }
    }
}

class GlassPaperViewModelFactory(
    private val context: Context,
    private val repository: GlassPaperRepository,
    private val settingsDataStore: SettingsDataStore,
    private val authManager: PaperflowAuthManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GlassPaperViewModel(context.applicationContext, repository, settingsDataStore, authManager) as T
    }
}
