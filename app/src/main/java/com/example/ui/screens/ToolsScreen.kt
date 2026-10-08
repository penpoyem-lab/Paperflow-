package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.PaperflowAiService
import com.example.ai.TranslationLanguageOption
import com.example.data.PdfDocumentEntity
import com.example.pdf.CompressionLevel
import com.example.pdf.MergeFileEntry
import com.example.pdf.PageNumbersConfig
import com.example.pdf.PdfDetailedMetadata
import com.example.pdf.PdfEngine
import com.example.pdf.SignatureStampConfig
import com.example.pdf.WatermarkConfig
import com.example.ui.GlassPaperViewModel
import com.example.ui.ToolOperationState
import com.example.ui.components.GlassCircularIconButton
import com.example.ui.components.LiquidGlassPanel
import com.example.ui.components.PlayStoreOrganicBlobSpinner
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
import kotlin.math.roundToInt

data class PdfToolSpec(
    val id: String,
    val category: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val isAvailableOffline: Boolean = true
)

val ALL_PDF_TOOLS: List<PdfToolSpec> = listOf(
    // EDIT (Pink / Magenta / Violet / Cyan accents)
    PdfToolSpec("merge", "EDIT", "Merge PDF", "Combine & reorder multiple PDFs into one document", Icons.AutoMirrored.Filled.CallMerge, IridescentPink),
    PdfToolSpec("split", "EDIT", "Split PDF", "Select visual pages or ranges to extract into a new PDF", Icons.AutoMirrored.Filled.CallSplit, LiquidMagenta),
    PdfToolSpec("rearrange", "EDIT", "Rearrange PDF", "Hold, drag & reorder visual page thumbnails", Icons.Filled.Reorder, IridescentPink),
    PdfToolSpec("rotate", "EDIT", "Rotate PDF", "Rotate all pages 90°, 180°, or 270° with preview", Icons.AutoMirrored.Filled.RotateRight, Color(0xFFF43F5E)),
    PdfToolSpec("delete_pages", "EDIT", "Delete Pages", "Select unwanted pages to remove from a PDF", Icons.Filled.DeleteSweep, LiquidMagenta),
    PdfToolSpec("extract_pages", "EDIT", "Extract Pages", "Save selected visual pages as a standalone PDF", Icons.Filled.ContentCut, Color(0xFFE11D48)),
    PdfToolSpec("watermark", "EDIT", "Watermark", "Live preview diagonal watermark with color, opacity, size & angle", Icons.AutoMirrored.Filled.BrandingWatermark, PrismPurple),
    PdfToolSpec("signature", "EDIT", "Signature", "Draw or upload signature and place on any PDF page", Icons.Filled.Draw, LiquidMagenta),
    PdfToolSpec("page_numbers", "EDIT", "Page Numbers", "Stamp clean page numbers across all pages", Icons.Filled.FormatListNumbered, IridescentPink),

    // OPTIMIZE (Amber / Orange accents)
    PdfToolSpec("compress", "OPTIMIZE", "Compress PDF", "Reduce file size for faster offline sharing", Icons.Filled.Compress, SolarAmber),
    PdfToolSpec("repair", "OPTIMIZE", "Repair PDF", "Rebuild clean PDF page streams", Icons.Filled.AutoFixHigh, Color(0xFFF97316)),
    PdfToolSpec("grayscale", "OPTIMIZE", "Grayscale", "Convert colored pages to high-contrast B&W", Icons.Filled.FilterBAndW, SolarAmber),

    // SECURITY (Violet / Electric Blue accents — NOW 100% ACTIVE)
    PdfToolSpec("protect", "SECURITY", "Protect PDF", "Encrypt & lock PDF with a local password", Icons.Filled.Lock, PrismViolet),
    PdfToolSpec("unlock", "SECURITY", "Unlock PDF", "Remove password protection from a locked PDF", Icons.Filled.LockOpen, ElectricBlue),
    PdfToolSpec("metadata", "SECURITY", "Metadata", "Inspect document properties, size & paths", Icons.Filled.Info, LiquidCyan),

    // CONVERT (Teal / Emerald Green / Cyan accents)
    PdfToolSpec("translate_pdf", "CONVERT", "Translate PDF", "Translate any PDF from any language to any language with AI", Icons.Filled.Translate, LiquidCyan, isAvailableOffline = false),
    PdfToolSpec("pdf_to_image", "CONVERT", "PDF to Image", "Export PDF pages as high-res PNG images", Icons.Filled.Image, CrystalTeal),
    PdfToolSpec("image_to_pdf", "CONVERT", "Image to PDF", "Convert photos into a clean PDF document", Icons.Filled.PictureAsPdf, EmeraldGreen),
    PdfToolSpec("extract_images", "CONVERT", "Extract Images", "Save rendered visual plates from PDF", Icons.Filled.Collections, CrystalTeal),
    PdfToolSpec("pdf_to_text", "CONVERT", "PDF to Text", "Extract searchable text & formulas", Icons.AutoMirrored.Filled.TextSnippet, EmeraldGreen)
)

@Composable
fun ToolsScreen(
    viewModel: GlassPaperViewModel,
    searchQuery: String,
    onSelectTool: (PdfToolSpec) -> Unit
) {
    val glass = LocalGlassColors.current
    val filteredTools = remember(searchQuery) {
        if (searchQuery.isBlank()) ALL_PDF_TOOLS
        else ALL_PDF_TOOLS.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val grouped = remember(filteredTools) {
        filteredTools.groupBy { it.category }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .testTag("tools_screen_list"),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 12.dp,
            bottom = 118.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = stringResource(R.string.tools_title),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = glass.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Local-first PDF utilities • Zero cloud uploads",
                    style = MaterialTheme.typography.bodyMedium,
                    color = glass.textSecondary
                )
            }
        }

        // Search Field
        item {
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                tintColor = PrismViolet,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search tools",
                        tint = PrismViolet,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_tools_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = glass.textMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setToolsSearchQuery(it) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = glass.textPrimary),
                            cursorBrush = SolidColor(PrismViolet),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tools_search_input")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setToolsSearchQuery("") },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = glass.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Categorized Glass Cards
        grouped.forEach { (category, tools) ->
            item(key = "header_$category") {
                val catColor = when (category) {
                    "EDIT" -> IridescentPink
                    "OPTIMIZE" -> SolarAmber
                    "SECURITY" -> PrismViolet
                    else -> CrystalTeal
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = catColor
                    )
                }
            }

            items(tools, key = { it.id }) { tool ->
                ToolGlassCard(
                    tool = tool,
                    onClick = { onSelectTool(tool) }
                )
            }
        }
    }
}

@Composable
private fun ToolGlassCard(
    tool: PdfToolSpec,
    onClick: () -> Unit
) {
    val glass = LocalGlassColors.current
    LiquidGlassPanel(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tool_card_${tool.id}"),
        cornerRadius = 22.dp,
        tintColor = tool.accentColor,
        shadowElevation = 9.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(tool.accentColor, tool.accentColor.copy(alpha = 0.65f))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = glass.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = glass.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = glass.textSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ============================================================================
// PAPERFLOW ARRANGE-REARRANGE & STUDIO WORKSPACE OVERLAY
// Inspired by the structured arrangement & live-preview flow in the reference
// photos, styled in Paperflow's signature VisionOS Liquid Glass aesthetic.
// ============================================================================

private data class MergeQueueItem(
    val key: Long,
    val doc: PdfDocumentEntity,
    val rotationDegrees: Int = 0
)

@Composable
fun ToolWorkspaceOverlay(
    viewModel: GlassPaperViewModel,
    toolId: String,
    documents: List<PdfDocumentEntity>,
    toolState: ToolOperationState,
    onBack: () -> Unit,
    onOpenResultDocument: (PdfDocumentEntity) -> Unit
) {
    BackHandler(onBack = onBack)

    val glass = LocalGlassColors.current
    val context = LocalContext.current
    val tool = remember(toolId) {
        ALL_PDF_TOOLS.firstOrNull { it.id == toolId } ?: ALL_PDF_TOOLS.first()
    }

    // Active target document for single-PDF tools (always starts null so the user explicitly chooses their PDF file)
    var selectedPrimaryDoc by remember(toolId) { mutableStateOf<PdfDocumentEntity?>(null) }
    var showLibraryPickerModal by remember(toolId) { mutableStateOf(false) }

    // Merge PDF Queue State (always starts empty so no default PDFs are pre-populated)
    val mergeQueue = remember(toolId) {
        mutableStateListOf<MergeQueueItem>()
    }
    var mergeOutputFilename by remember(toolId) { mutableStateOf("paperflow-merged") }

    // Split / Extract / Delete Page Selection State
    val selectedPagesZeroBased = remember(selectedPrimaryDoc?.id, toolId) {
        mutableStateListOf<Int>().apply {
            selectedPrimaryDoc?.let { doc ->
                if (toolId != "delete_pages") {
                    for (i in 0 until doc.pageCount) add(i)
                }
            }
        }
    }
    var rangeSelectionInput by remember(selectedPrimaryDoc?.id) { mutableStateOf("") }
    var showRangeInputBar by remember { mutableStateOf(false) }

    // Rearrange PDF Page Order State (0-based original page indices in their current arranged order + per-slot rotations)
    val rearrangedPageOrder = remember(selectedPrimaryDoc?.id) {
        mutableStateListOf<Int>().apply {
            selectedPrimaryDoc?.let { doc ->
                for (i in 0 until doc.pageCount) add(i)
            }
        }
    }
    val rearrangedSlotRotations = remember(selectedPrimaryDoc?.id) {
        mutableStateMapOf<Int, Int>()
    }
    var selectedSwapSlot by remember(selectedPrimaryDoc?.id) { mutableStateOf<Int?>(null) }

    // Rotate PDF State
    var rotationDegrees by remember { mutableIntStateOf(90) }
    val perPageRotations = remember(selectedPrimaryDoc?.id) {
        mutableStateMapOf<Int, Int>()
    }

    // Compress PDF Level State (Paperflow 3-Tier Compression)
    var selectedCompressionLevel by remember { mutableStateOf(CompressionLevel.RECOMMENDED) }
    var compressOutputName by remember(selectedPrimaryDoc?.id) {
        mutableStateOf(
            selectedPrimaryDoc?.title?.removeSuffix(".pdf")?.let { "$it-compressed" }
                ?: "paperflow-compressed"
        )
    }

    // Page Numbers Config State (Paperflow 6-Position Grid + Start Page)
    var pageNumberPosition by remember { mutableStateOf("bottom-center") }
    var pageNumberStartPageInput by remember { mutableStateOf("1") }
    var pageNumberFormatStyle by remember { mutableStateOf("PAGE_X_OF_Y") }
    var pageNumberOutputName by remember(selectedPrimaryDoc?.id) {
        mutableStateOf(
            selectedPrimaryDoc?.title?.removeSuffix(".pdf")?.let { "$it-numbered" }
                ?: "paperflow-numbered"
        )
    }

    // Metadata Inspector & Editor State (Paperflow XMP / DocumentInformation Editor)
    var metaTitle by remember(selectedPrimaryDoc?.id) {
        mutableStateOf(selectedPrimaryDoc?.title?.removeSuffix(".pdf") ?: "")
    }
    var metaAuthor by remember(selectedPrimaryDoc?.id) { mutableStateOf("") }
    var metaSubject by remember(selectedPrimaryDoc?.id) { mutableStateOf("") }
    var metaKeywords by remember(selectedPrimaryDoc?.id) { mutableStateOf("") }
    var metaCreator by remember(selectedPrimaryDoc?.id) { mutableStateOf("Paperflow Studio") }
    var metaProducer by remember(selectedPrimaryDoc?.id) { mutableStateOf("Paperflow PDFBox Engine") }
    var metaOutputName by remember(selectedPrimaryDoc?.id) {
        mutableStateOf(
            selectedPrimaryDoc?.title?.removeSuffix(".pdf")?.let { "$it-metadata" }
                ?: "paperflow-metadata"
        )
    }

    LaunchedEffect(selectedPrimaryDoc?.id, toolId) {
        if (toolId == "metadata" && selectedPrimaryDoc != null) {
            val loaded = PdfEngine.inspectDetailedMetadata(context, selectedPrimaryDoc!!.filePath)
            if (loaded.title.isNotBlank()) metaTitle = loaded.title
            metaAuthor = loaded.author
            metaSubject = loaded.subject
            metaKeywords = loaded.keywords
            if (loaded.creator.isNotBlank()) metaCreator = loaded.creator
            if (loaded.producer.isNotBlank()) metaProducer = loaded.producer
        }
    }

    // Watermark Live Preview State
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL • PAPERFLOW") }
    val watermarkPalette = remember {
        listOf(
            IridescentPink,
            ElectricBlue,
            EmeraldGreen,
            SolarAmber,
            Color(0xFF0F172A),
            LiquidCyan,
            PrismViolet
        )
    }
    var selectedWatermarkColor by remember { mutableStateOf(IridescentPink) }
    var watermarkOpacityPercent by remember { mutableFloatStateOf(30f) }
    var watermarkFontSizePx by remember { mutableFloatStateOf(50f) }
    var watermarkRotationDeg by remember { mutableFloatStateOf(-45f) }
    var watermarkOutputName by remember(selectedPrimaryDoc?.id) {
        mutableStateOf(
            selectedPrimaryDoc?.title?.removeSuffix(".pdf")?.let { "$it-watermarked" }
                ?: "paperflow-watermarked"
        )
    }

    // Signature Studio State
    var signatureMode by remember { mutableStateOf("DRAW") } // "DRAW" or "UPLOAD"
    var showSignatureDrawModal by remember { mutableStateOf(false) }
    val signaturePoints = remember { mutableStateListOf<Pair<Float, Float>>() }
    var uploadedSignatureUri by remember { mutableStateOf<Uri?>(null) }
    var signatureTargetPageIdx by remember(selectedPrimaryDoc?.id) { mutableIntStateOf(0) }
    var sigNormalizedX by remember { mutableFloatStateOf(0.68f) }
    var sigNormalizedY by remember { mutableFloatStateOf(0.80f) }
    var signatureOutputName by remember(selectedPrimaryDoc?.id) {
        mutableStateOf(
            selectedPrimaryDoc?.title?.removeSuffix(".pdf")?.let { "$it-signed" }
                ?: "paperflow-signed"
        )
    }

    // Protect & Unlock PDF State
    var protectNewPassword by remember { mutableStateOf("") }
    var protectConfirmPassword by remember { mutableStateOf("") }
    var protectPasswordVisible by remember { mutableStateOf(false) }
    var protectOutputName by remember(selectedPrimaryDoc?.id) {
        mutableStateOf(
            selectedPrimaryDoc?.title?.removeSuffix(".pdf")?.let { "$it-protected" }
                ?: "paperflow-protected"
        )
    }
    var unlockOutputName by remember(selectedPrimaryDoc?.id) {
        mutableStateOf(
            selectedPrimaryDoc?.title?.removeSuffix(".pdf")?.let { "$it-unlocked" }
                ?: "paperflow-unlocked"
        )
    }

    // Image to PDF State
    val selectedImageUris = remember { mutableStateListOf<Uri>() }
    var imageToPdfOutputName by remember { mutableStateOf("paperflow-scanned") }

    // Universal PDF Translation State (Every Language to Every Language)
    var translateSourceLanguage by remember {
        mutableStateOf(
            PaperflowAiService.SUPPORTED_TRANSLATION_LANGUAGES.first() // Auto-Detect Language
        )
    }
    var translateTargetLanguage by remember {
        mutableStateOf(
            PaperflowAiService.SUPPORTED_TRANSLATION_LANGUAGES.firstOrNull { it.code == "es" }
                ?: PaperflowAiService.SUPPORTED_TRANSLATION_LANGUAGES[1]
        )
    }
    var translatePreserveFormatting by remember { mutableStateOf(true) }
    var translateBilingualMode by remember { mutableStateOf(false) }
    var translateCustomTargetLanguage by remember { mutableStateOf("") }
    var translateCustomSourceLanguage by remember { mutableStateOf("") }
    var showLanguagePickerFor by remember { mutableStateOf<String?>(null) } // "SOURCE" or "TARGET"
    var languageSearchQuery by remember { mutableStateOf("") }
    var translateOutputName by remember(selectedPrimaryDoc?.id, translateTargetLanguage.code, translateCustomTargetLanguage) {
        val targetTag = translateCustomTargetLanguage.trim().ifBlank { translateTargetLanguage.code }
        mutableStateOf(
            selectedPrimaryDoc?.title?.removeSuffix(".pdf")?.let { "$it-$targetTag" }
                ?: "paperflow-translated-$targetTag"
        )
    }

    // System SAF Launchers
    val singlePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importAndSelectPdfForTool(uri) { importedDoc ->
                if (tool.id == "merge") {
                    mergeQueue.add(
                        MergeQueueItem(
                            key = System.nanoTime(),
                            doc = importedDoc,
                            rotationDegrees = 0
                        )
                    )
                } else {
                    selectedPrimaryDoc = importedDoc
                }
            }
        }
    }

    val multiPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { uri ->
            viewModel.importAndSelectPdfForTool(uri) { importedDoc ->
                mergeQueue.add(
                    MergeQueueItem(
                        key = System.nanoTime(),
                        doc = importedDoc,
                        rotationDegrees = 0
                    )
                )
            }
        }
    }

    val signatureImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadedSignatureUri = uri
            signatureMode = "UPLOAD"
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(15)
    ) { uris ->
        selectedImageUris.clear()
        selectedImageUris.addAll(uris)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .testTag("tool_workspace_scroll"),
            contentPadding = PaddingValues(top = 12.dp, bottom = 124.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Navigation Header (Back Arrow + Tool Title)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassCircularIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = tool.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary
                        )
                    }
                }
            }

            // 2. Active Progress / Error / Success Banners
            if (toolState.isRunning) {
                item {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        tintColor = ElectricBlue
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = LiquidCyan,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = toolState.progressText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = glass.textPrimary
                            )
                        }
                    }
                }
            }

            toolState.errorMessage?.let { err ->
                item {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp,
                        tintColor = LiquidMagenta
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = LiquidMagenta,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = LiquidMagenta,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            toolState.resultDocument?.let { resultDoc ->
                item {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 26.dp,
                        tintColor = EmeraldGreen
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Saved to Paperflow Library!",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = glass.textPrimary
                                    )
                                    Text(
                                        text = "${resultDoc.title} • ${resultDoc.pageCount} PAGES • ${GlassPaperViewModel.formatFileSize(resultDoc.fileSizeBytes)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = glass.textSecondary
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { onOpenResultDocument(resultDoc) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                                ) {
                                    Text("Open PDF", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.shareDocument(context, resultDoc) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrismPurple)
                                ) {
                                    Text("Share PDF", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (toolState.resultImagesCount > 0) {
                item {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        tintColor = CrystalTeal
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Exported ${toolState.resultImagesCount} High-Resolution PNG Pages",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Saved locally in your device's Paperflow image directory.",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    }
                }
            }

            toolState.extractedText?.let { extracted ->
                item {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        tintColor = EmeraldGreen
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Extracted Document Text",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                            Text(
                                text = extracted,
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                    }
                }
            }

            // ====================================================================
            // TOOL 1: MERGE PDF (Photos 1 & 2 Arrange-Rearrange Style)
            // ====================================================================
            if (tool.id == "merge") {
                if (mergeQueue.isEmpty()) {
                    item {
                        SelectPdfDashedGlassDropzone(
                            title = "Select PDF Files",
                            subtitle = "Tap to browse device or choose from Paperflow Library",
                            accentColor = IridescentPink,
                            onBrowseDevice = {
                                multiPdfLauncher.launch(arrayOf("application/pdf"))
                            },
                            onChooseFromLibrary = if (documents.isNotEmpty()) {
                                { showLibraryPickerModal = true }
                            } else null
                        )
                    }
                } else {
                    // Header bar: "X FILES • Y PAGES" + "CLEAR ALL"
                    item {
                        val totalPages = mergeQueue.sumOf { it.doc.pageCount }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${mergeQueue.size} FILES • $totalPages PAGES",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = glass.textSecondary
                            )
                            Text(
                                text = "CLEAR ALL",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    letterSpacing = 1.1.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = LiquidMagenta,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { mergeQueue.clear() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Reorderable File Cards with Drag Handle, Thumbnail, Size/Pages, Rotate 90°, Move Up/Down & Remove
                    itemsIndexed(mergeQueue, key = { _, item -> item.key }) { idx, item ->
                        MergeFileGlassRowCard(
                            item = item,
                            canMoveUp = idx > 0,
                            canMoveDown = idx < mergeQueue.lastIndex,
                            onMoveUp = {
                                if (idx > 0) {
                                    val removed = mergeQueue.removeAt(idx)
                                    mergeQueue.add(idx - 1, removed)
                                }
                            },
                            onMoveDown = {
                                if (idx < mergeQueue.lastIndex) {
                                    val removed = mergeQueue.removeAt(idx)
                                    mergeQueue.add(idx + 1, removed)
                                }
                            },
                            onRotate90 = {
                                val nextRot = (item.rotationDegrees + 90) % 360
                                mergeQueue[idx] = item.copy(rotationDegrees = nextRot)
                            },
                            onRemove = {
                                mergeQueue.removeAt(idx)
                            }
                        )
                    }

                    // Dashed "+ ADD MORE FILES" Button
                    item {
                        DashedAddMoreGlassButton(
                            label = "ADD MORE FILES",
                            accentColor = IridescentPink,
                            onClick = {
                                if (documents.isNotEmpty()) {
                                    showLibraryPickerModal = true
                                } else {
                                    multiPdfLauncher.launch(arrayOf("application/pdf"))
                                }
                            }
                        )
                    }

                    // OUTPUT FILENAME Glass Card
                    item {
                        LiquidGlassPanel(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 26.dp,
                            tintColor = IridescentPink
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "OUTPUT FILENAME",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = glass.textSecondary
                                )
                                GlassOutlinedFilenameInput(
                                    value = mergeOutputFilename,
                                    onValueChange = { mergeOutputFilename = it },
                                    placeholder = "paperflow-merged",
                                    accentColor = IridescentPink,
                                    testTag = "merge_output_filename_input"
                                )
                            }
                        }
                    }
                }
            }

            // ====================================================================
            // TOOL 2: IMAGE TO PDF
            // ====================================================================
            else if (tool.id == "image_to_pdf") {
                if (selectedImageUris.isEmpty()) {
                    item {
                        SelectPdfDashedGlassDropzone(
                            title = "Select Images",
                            subtitle = "Tap to pick photos from your device to build a PDF",
                            accentColor = EmeraldGreen,
                            onBrowseDevice = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onChooseFromLibrary = null
                        )
                    }
                } else {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedImageUris.size} IMAGES SELECTED",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = glass.textSecondary
                            )
                            Text(
                                text = "CLEAR ALL",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = LiquidMagenta,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedImageUris.clear() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    item {
                        DashedAddMoreGlassButton(
                            label = "PICK DIFFERENT PHOTOS",
                            accentColor = EmeraldGreen,
                            onClick = {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                    item {
                        LiquidGlassPanel(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 26.dp,
                            tintColor = EmeraldGreen
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "OUTPUT FILENAME",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = glass.textSecondary
                                )
                                GlassOutlinedFilenameInput(
                                    value = imageToPdfOutputName,
                                    onValueChange = { imageToPdfOutputName = it },
                                    placeholder = "paperflow-scanned",
                                    accentColor = EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }

            // ====================================================================
            // SINGLE-DOCUMENT TOOLS: If no PDF selected yet, show Dropzone
            // ====================================================================
            else if (selectedPrimaryDoc == null) {
                item {
                    SelectPdfDashedGlassDropzone(
                        title = "Select PDF File",
                        subtitle = "Tap to browse device or choose from your Paperflow Library",
                        accentColor = tool.accentColor,
                        onBrowseDevice = {
                            singlePdfLauncher.launch(arrayOf("application/pdf"))
                        },
                        onChooseFromLibrary = if (documents.isNotEmpty()) {
                            { showLibraryPickerModal = true }
                        } else null
                    )
                }
            }

            // ====================================================================
            // SINGLE-DOCUMENT TOOLS WITH SELECTED PDF
            // ====================================================================
            else {
                val activeDoc = selectedPrimaryDoc!!

                // Top Selected Document Summary Card (Except Watermark which puts LIVE PREVIEW at the very top per Photo 5)
                if (tool.id != "watermark") {
                    item {
                        SelectedDocumentHeaderGlassCard(
                            doc = activeDoc,
                            accentColor = tool.accentColor,
                            onSwitchOrClear = {
                                if (documents.isNotEmpty()) {
                                    showLibraryPickerModal = true
                                } else {
                                    singlePdfLauncher.launch(arrayOf("application/pdf"))
                                }
                            },
                            onCloseFile = { selectedPrimaryDoc = null }
                        )
                    }
                }

                when (tool.id) {
                    // ============================================================
                    // SPLIT / EXTRACT / DELETE PAGES (Photo 3 Style)
                    // ============================================================
                    "split", "extract_pages", "delete_pages" -> {
                        // Heavy Document Advisory Banner (Shown when document has many pages or large size, or as range helper)
                        if (activeDoc.pageCount >= 12 || activeDoc.fileSizeBytes > 5 * 1024 * 1024L || showRangeInputBar) {
                            item {
                                HeavyDocumentRangeBanner(
                                    rangeInput = rangeSelectionInput,
                                    showRangeField = showRangeInputBar,
                                    onToggleRangeField = { showRangeInputBar = !showRangeInputBar },
                                    onRangeInputChange = { input ->
                                        rangeSelectionInput = input
                                        val parsed = parsePageRangeString(input, activeDoc.pageCount)
                                        if (parsed.isNotEmpty()) {
                                            selectedPagesZeroBased.clear()
                                            selectedPagesZeroBased.addAll(parsed)
                                        }
                                    }
                                )
                            }
                        }

                        // PAGE SELECTION 2-Column Visual Thumbnail Grid Card
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 28.dp,
                                tintColor = tool.accentColor
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
                                            text = "PAGE SELECTION",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                            Text(
                                                text = "RANGE",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = LiquidCyan,
                                                modifier = Modifier.clickable { showRangeInputBar = !showRangeInputBar }
                                            )
                                            Text(
                                                text = "SELECT ALL",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = IridescentPink,
                                                modifier = Modifier.clickable {
                                                    selectedPagesZeroBased.clear()
                                                    for (i in 0 until activeDoc.pageCount) selectedPagesZeroBased.add(i)
                                                }
                                            )
                                            Text(
                                                text = "CLEAR",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = glass.textSecondary,
                                                modifier = Modifier.clickable {
                                                    selectedPagesZeroBased.clear()
                                                }
                                            )
                                        }
                                    }

                                    // 2-Column Page Thumbnail Grid (capped at 60 visual previews for ultra-fast scrolling; all pages selectable via Range/Select All)
                                    val visualPreviewCount = activeDoc.pageCount.coerceAtMost(60)
                                    val rows = (0 until visualPreviewCount).chunked(2)
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        rows.forEach { rowPages ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                rowPages.forEach { pageIdx ->
                                                    val isSelected = selectedPagesZeroBased.contains(pageIdx)
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        SelectablePageThumbnailCard(
                                                            doc = activeDoc,
                                                            pageIndex = pageIdx,
                                                            isSelected = isSelected,
                                                            accentColor = if (tool.id == "delete_pages") LiquidMagenta else IridescentPink,
                                                            onClick = {
                                                                if (isSelected) selectedPagesZeroBased.remove(pageIdx)
                                                                else selectedPagesZeroBased.add(pageIdx)
                                                            }
                                                        )
                                                    }
                                                }
                                                if (rowPages.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ============================================================
                    // REARRANGE PDF (Photo 4 Hold & Drag / Swap Style)
                    // ============================================================
                    "rearrange" -> {
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 28.dp,
                                tintColor = IridescentPink
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
                                            text = "HOLD & DRAG OR TAP TO SWAP PAGES",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                letterSpacing = 1.1.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable {
                                                    rearrangedPageOrder.clear()
                                                    rearrangedSlotRotations.clear()
                                                    for (i in 0 until activeDoc.pageCount) rearrangedPageOrder.add(i)
                                                    selectedSwapSlot = null
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = "Reset order",
                                                tint = glass.textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "RESET",
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = glass.textSecondary
                                            )
                                        }
                                    }

                                    if (selectedSwapSlot != null) {
                                        Text(
                                            text = "Slot #${selectedSwapSlot!! + 1} selected — tap any other page card to swap positions, or use arrows.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = LiquidCyan
                                        )
                                    }

                                    val visualCount = rearrangedPageOrder.size.coerceAtMost(60)
                                    val slotRows = (0 until visualCount).chunked(2)
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        slotRows.forEach { rowSlots ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                rowSlots.forEach { slotIdx ->
                                                    val originalPageIdx = rearrangedPageOrder[slotIdx]
                                                    val slotRotDeg = rearrangedSlotRotations[slotIdx] ?: 0
                                                    val isHighlightedForSwap = selectedSwapSlot == slotIdx
                                                    Box(modifier = Modifier.weight(1f)) {
                                                        RearrangePageThumbnailCard(
                                                            doc = activeDoc,
                                                            originalPageIndex = originalPageIdx,
                                                            currentSlotIndex = slotIdx,
                                                            totalSlots = rearrangedPageOrder.size,
                                                            rotationDegrees = slotRotDeg,
                                                            isSwapSelected = isHighlightedForSwap,
                                                            onTapCard = {
                                                                val currentSel = selectedSwapSlot
                                                                if (currentSel == null) {
                                                                    selectedSwapSlot = slotIdx
                                                                } else if (currentSel == slotIdx) {
                                                                    selectedSwapSlot = null
                                                                } else {
                                                                    val tmp = rearrangedPageOrder[currentSel]
                                                                    rearrangedPageOrder[currentSel] = rearrangedPageOrder[slotIdx]
                                                                    rearrangedPageOrder[slotIdx] = tmp
                                                                    val tmpRot = rearrangedSlotRotations[currentSel] ?: 0
                                                                    rearrangedSlotRotations[currentSel] = rearrangedSlotRotations[slotIdx] ?: 0
                                                                    rearrangedSlotRotations[slotIdx] = tmpRot
                                                                    selectedSwapSlot = null
                                                                }
                                                            },
                                                            onRotate90 = {
                                                                val nextRot = ((rearrangedSlotRotations[slotIdx] ?: 0) + 90) % 360
                                                                rearrangedSlotRotations[slotIdx] = nextRot
                                                            },
                                                            onMoveLeft = {
                                                                if (slotIdx > 0) {
                                                                    val tmp = rearrangedPageOrder[slotIdx - 1]
                                                                    rearrangedPageOrder[slotIdx - 1] = rearrangedPageOrder[slotIdx]
                                                                    rearrangedPageOrder[slotIdx] = tmp
                                                                    val tmpRot = rearrangedSlotRotations[slotIdx - 1] ?: 0
                                                                    rearrangedSlotRotations[slotIdx - 1] = rearrangedSlotRotations[slotIdx] ?: 0
                                                                    rearrangedSlotRotations[slotIdx] = tmpRot
                                                                    selectedSwapSlot = null
                                                                }
                                                            },
                                                            onMoveRight = {
                                                                if (slotIdx < rearrangedPageOrder.lastIndex) {
                                                                    val tmp = rearrangedPageOrder[slotIdx + 1]
                                                                    rearrangedPageOrder[slotIdx + 1] = rearrangedPageOrder[slotIdx]
                                                                    rearrangedPageOrder[slotIdx] = tmp
                                                                    val tmpRot = rearrangedSlotRotations[slotIdx + 1] ?: 0
                                                                    rearrangedSlotRotations[slotIdx + 1] = rearrangedSlotRotations[slotIdx] ?: 0
                                                                    rearrangedSlotRotations[slotIdx] = tmpRot
                                                                    selectedSwapSlot = null
                                                                }
                                                            }
                                                        )
                                                    }
                                                }
                                                if (rowSlots.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ============================================================
                    // WATERMARK STUDIO (Photo 5 Live Preview + Appearance Sliders)
                    // ============================================================
                    "watermark" -> {
                        // 1. LIVE PREVIEW Card
                        item {
                            WatermarkLivePreviewCard(
                                doc = activeDoc,
                                watermarkText = watermarkText,
                                watermarkColor = selectedWatermarkColor,
                                opacityPercent = watermarkOpacityPercent,
                                fontSizePx = watermarkFontSizePx,
                                rotationDegrees = watermarkRotationDeg
                            )
                        }

                        // 2. WATERMARK TEXT + APPEARANCE + OUTPUT FILENAME + CLOSE FILE Card
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 28.dp,
                                tintColor = PrismPurple
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    // WATERMARK TEXT
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "WATERMARK TEXT",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        GlassOutlinedFilenameInput(
                                            value = watermarkText,
                                            onValueChange = { watermarkText = it },
                                            placeholder = "Enter watermark text...",
                                            accentColor = IridescentPink,
                                            testTag = "watermark_text_input"
                                        )
                                    }

                                    // APPEARANCE HEADER
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "APPEARANCE",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Palette,
                                            contentDescription = null,
                                            tint = glass.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // COLOR CIRCLES
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "COLOR",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.1.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            watermarkPalette.forEach { colorOption ->
                                                val isSelected = selectedWatermarkColor == colorOption
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(colorOption)
                                                        .border(
                                                            width = if (isSelected) 3.dp else 1.dp,
                                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.35f),
                                                            shape = CircleShape
                                                        )
                                                        .clickable { selectedWatermarkColor = colorOption },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // OPACITY & SIZE SLIDERS SIDE-BY-SIDE
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "OPACITY (${watermarkOpacityPercent.roundToInt()}%)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = glass.textSecondary
                                            )
                                            Slider(
                                                value = watermarkOpacityPercent,
                                                onValueChange = { watermarkOpacityPercent = it },
                                                valueRange = 10f..100f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = IridescentPink,
                                                    activeTrackColor = IridescentPink
                                                )
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "SIZE (${watermarkFontSizePx.roundToInt()}PX)",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = glass.textSecondary
                                            )
                                            Slider(
                                                value = watermarkFontSizePx,
                                                onValueChange = { watermarkFontSizePx = it },
                                                valueRange = 20f..110f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = IridescentPink,
                                                    activeTrackColor = IridescentPink
                                                )
                                            )
                                        }
                                    }

                                    // ROTATION SLIDER
                                    Column {
                                        Text(
                                            text = "ROTATION (${watermarkRotationDeg.roundToInt()}°)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = glass.textSecondary
                                        )
                                        Slider(
                                            value = watermarkRotationDeg,
                                            onValueChange = { watermarkRotationDeg = it },
                                            valueRange = -90f..90f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = IridescentPink,
                                                activeTrackColor = IridescentPink
                                            )
                                        )
                                    }

                                    // OUTPUT FILENAME
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "OUTPUT FILENAME",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        GlassOutlinedFilenameInput(
                                            value = watermarkOutputName,
                                            onValueChange = { watermarkOutputName = it },
                                            placeholder = "${activeDoc.title.removeSuffix(".pdf")}-watermarked",
                                            accentColor = PrismPurple
                                        )
                                    }

                                    // CLOSE FILE
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "CLOSE FILE",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { selectedPrimaryDoc = null }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ============================================================
                    // SIGNATURE STUDIO (Photo 6 Draw / Upload + Page Selector + Interactive Page Placement)
                    // ============================================================
                    "signature" -> {
                        // Prominent "UPLOAD SIGNATURE" Pill Button
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = LiquidMagenta)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(LiquidMagenta, IridescentPink, PrismPurple)
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                                    .clickable {
                                        signatureImageLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Image,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "UPLOAD SIGNATURE",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // SIGNATURE Mode Card: Two Side-by-Side Glass Tiles (DRAW | UPLOAD)
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 28.dp,
                                tintColor = LiquidMagenta
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "SIGNATURE",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = glass.textSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // DRAW Tile
                                        val isDrawSelected = signatureMode == "DRAW"
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(104.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (isDrawSelected) LiquidMagenta.copy(alpha = 0.22f)
                                                    else Color.White.copy(alpha = if (glass.isDark) 0.05f else 0.4f)
                                                )
                                                .border(
                                                    width = if (isDrawSelected) 1.8.dp else 1.dp,
                                                    color = if (isDrawSelected) LiquidMagenta else Color.White.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .clickable {
                                                    signatureMode = "DRAW"
                                                    showSignatureDrawModal = true
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = "Draw signature",
                                                    tint = LiquidMagenta,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = if (signaturePoints.isNotEmpty()) "REDRAW INK" else "DRAW",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        letterSpacing = 1.1.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    ),
                                                    color = LiquidMagenta
                                                )
                                            }
                                        }

                                        // UPLOAD Tile
                                        val isUploadSelected = signatureMode == "UPLOAD" && uploadedSignatureUri != null
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(104.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    if (isUploadSelected) LiquidMagenta.copy(alpha = 0.22f)
                                                    else Color.White.copy(alpha = if (glass.isDark) 0.05f else 0.4f)
                                                )
                                                .border(
                                                    width = if (isUploadSelected) 1.8.dp else 1.dp,
                                                    color = if (isUploadSelected) LiquidMagenta else Color.White.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .clickable {
                                                    signatureMode = "UPLOAD"
                                                    signatureImageLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Filled.Image,
                                                    contentDescription = "Upload signature",
                                                    tint = LiquidMagenta,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = if (uploadedSignatureUri != null) "IMAGE READY" else "UPLOAD",
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        letterSpacing = 1.1.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    ),
                                                    color = LiquidMagenta
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Page Selector Bar: "< PAGE 1 OF X >"
                        item {
                            val clampedPage = signatureTargetPageIdx.coerceIn(0, (activeDoc.pageCount - 1).coerceAtLeast(0))
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 22.dp,
                                tintColor = ElectricBlue
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (signatureTargetPageIdx > 0) signatureTargetPageIdx--
                                        },
                                        enabled = clampedPage > 0
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                            contentDescription = "Previous page",
                                            tint = if (clampedPage > 0) glass.textPrimary else glass.textMuted
                                        )
                                    }
                                    Text(
                                        text = "PAGE ${clampedPage + 1} OF ${activeDoc.pageCount}",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            letterSpacing = 1.4.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = glass.textSecondary
                                    )
                                    IconButton(
                                        onClick = {
                                            if (signatureTargetPageIdx < activeDoc.pageCount - 1) signatureTargetPageIdx++
                                        },
                                        enabled = clampedPage < activeDoc.pageCount - 1
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Next page",
                                            tint = if (clampedPage < activeDoc.pageCount - 1) glass.textPrimary else glass.textMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Live Interactive Page Preview with Draggable Signature Box
                        item {
                            SignaturePagePlacementPreviewCard(
                                doc = activeDoc,
                                pageIndex = signatureTargetPageIdx.coerceIn(0, (activeDoc.pageCount - 1).coerceAtLeast(0)),
                                strokePoints = signaturePoints,
                                uploadedSignatureUri = if (signatureMode == "UPLOAD") uploadedSignatureUri else null,
                                normalizedX = sigNormalizedX,
                                normalizedY = sigNormalizedY,
                                onPositionChange = { nx, ny ->
                                    sigNormalizedX = nx
                                    sigNormalizedY = ny
                                },
                                onTapDrawPad = { showSignatureDrawModal = true }
                            )
                        }
                    }

                    // ============================================================
                    // PROTECT PDF (Photo 7 Style — PAPERFLOW Branding)
                    // ============================================================
                    "protect" -> {
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 28.dp,
                                tintColor = PrismViolet
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    // NEW PASSWORD
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "NEW PASSWORD",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        GlassPasswordInputField(
                                            value = protectNewPassword,
                                            onValueChange = { protectNewPassword = it },
                                            placeholder = "••••••••",
                                            passwordVisible = protectPasswordVisible,
                                            onToggleVisibility = { protectPasswordVisible = !protectPasswordVisible },
                                            accentColor = PrismViolet,
                                            testTag = "protect_new_password_input"
                                        )
                                    }

                                    // CONFIRM PASSWORD
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "CONFIRM PASSWORD",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        GlassPasswordInputField(
                                            value = protectConfirmPassword,
                                            onValueChange = { protectConfirmPassword = it },
                                            placeholder = "••••••••",
                                            passwordVisible = protectPasswordVisible,
                                            onToggleVisibility = { protectPasswordVisible = !protectPasswordVisible },
                                            accentColor = PrismViolet,
                                            testTag = "protect_confirm_password_input"
                                        )
                                    }

                                    // OUTPUT FILENAME
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "OUTPUT FILENAME",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        GlassOutlinedFilenameInput(
                                            value = protectOutputName,
                                            onValueChange = { protectOutputName = it },
                                            placeholder = "${activeDoc.title.removeSuffix(".pdf")}-protected",
                                            accentColor = PrismViolet,
                                            testTag = "protect_output_filename_input"
                                        )
                                    }

                                    // Amber Security Warning Box ( strictly using Paperflow branding )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(SolarAmber.copy(alpha = 0.14f))
                                            .border(1.dp, SolarAmber.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = SolarAmber,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Paperflow cannot recover forgotten passwords. Save it securely.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 18.sp
                                            ),
                                            color = SolarAmber
                                        )
                                    }

                                    // CLOSE FILE
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "CLOSE FILE",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { selectedPrimaryDoc = null }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ============================================================
                    // UNLOCK PDF
                    // ============================================================
                    "unlock" -> {
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 28.dp,
                                tintColor = ElectricBlue
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "DOCUMENT PASSWORD",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        GlassPasswordInputField(
                                            value = protectNewPassword,
                                            onValueChange = { protectNewPassword = it },
                                            placeholder = "Enter current password",
                                            passwordVisible = protectPasswordVisible,
                                            onToggleVisibility = { protectPasswordVisible = !protectPasswordVisible },
                                            accentColor = ElectricBlue,
                                            testTag = "unlock_password_input"
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "OUTPUT FILENAME",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        GlassOutlinedFilenameInput(
                                            value = unlockOutputName,
                                            onValueChange = { unlockOutputName = it },
                                            placeholder = "${activeDoc.title.removeSuffix(".pdf")}-unlocked",
                                            accentColor = ElectricBlue
                                        )
                                    }

                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "CLOSE FILE",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                letterSpacing = 1.2.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { selectedPrimaryDoc = null }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ============================================================
                    // ROTATE PDF
                    // ============================================================
                    "rotate" -> {
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 26.dp,
                                tintColor = ElectricBlue
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "ROTATION ANGLE",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = glass.textSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        listOf(90, 180, 270).forEach { deg ->
                                            val sel = rotationDegrees == deg
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        if (sel) ElectricBlue else Color.White.copy(alpha = if (glass.isDark) 0.08f else 0.4f)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (sel) LiquidCyan else Color.White.copy(alpha = 0.4f),
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable { rotationDegrees = deg }
                                                    .padding(vertical = 14.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$deg°",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = if (sel) Color.White else glass.textPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ============================================================
                    // COMPRESS PDF (Paperflow 3-Tier Compression Selector)
                    // ============================================================
                    "compress" -> {
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 26.dp,
                                tintColor = SolarAmber
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "COMPRESSION LEVEL (PAPERFLOW ENGINE)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = glass.textSecondary
                                    )
                                    CompressionLevel.entries.forEach { level ->
                                        val isSelected = selectedCompressionLevel == level
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(
                                                    if (isSelected) SolarAmber.copy(alpha = 0.22f)
                                                    else Color.White.copy(alpha = if (glass.isDark) 0.05f else 0.4f)
                                                )
                                                .border(
                                                    width = if (isSelected) 1.8.dp else 1.dp,
                                                    color = if (isSelected) SolarAmber else Color.White.copy(alpha = 0.35f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .clickable { selectedCompressionLevel = level }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = level.label.uppercase(),
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 1.sp
                                                    ),
                                                    color = if (isSelected) SolarAmber else glass.textPrimary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = level.subtitle,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = glass.textSecondary
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = SolarAmber,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "OUTPUT FILENAME",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = glass.textSecondary
                                    )
                                    GlassOutlinedFilenameInput(
                                        value = compressOutputName,
                                        onValueChange = { compressOutputName = it },
                                        placeholder = "${activeDoc.title.removeSuffix(".pdf")}-compressed",
                                        accentColor = SolarAmber
                                    )
                                }
                            }
                        }
                    }

                    // ============================================================
                    // PAGE NUMBERS (Paperflow 6-Position Grid + Start Page)
                    // ============================================================
                    "page_numbers" -> {
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 26.dp,
                                tintColor = IridescentPink
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "STAMP POSITION (6-CORNER VECTOR GRID)",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = glass.textSecondary
                                    )

                                    val positions = listOf(
                                        listOf("top-left" to "Top Left", "top-center" to "Top Center", "top-right" to "Top Right"),
                                        listOf("bottom-left" to "Bottom Left", "bottom-center" to "Bottom Center", "bottom-right" to "Bottom Right")
                                    )
                                    positions.forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            rowItems.forEach { (posKey, posLabel) ->
                                                val sel = pageNumberPosition == posKey
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(
                                                            if (sel) IridescentPink.copy(alpha = 0.24f)
                                                            else Color.White.copy(alpha = if (glass.isDark) 0.05f else 0.4f)
                                                        )
                                                        .border(
                                                            width = if (sel) 1.8.dp else 1.dp,
                                                            color = if (sel) IridescentPink else Color.White.copy(alpha = 0.35f),
                                                            shape = RoundedCornerShape(14.dp)
                                                        )
                                                        .clickable { pageNumberPosition = posKey }
                                                        .padding(vertical = 12.dp, horizontal = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = posLabel.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            letterSpacing = 0.5.sp
                                                        ),
                                                        color = if (sel) IridescentPink else glass.textPrimary,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "START FROM PAGE",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = glass.textSecondary
                                            )
                                            GlassOutlinedFilenameInput(
                                                value = pageNumberStartPageInput,
                                                onValueChange = { pageNumberStartPageInput = it.filter { ch -> ch.isDigit() } },
                                                placeholder = "1",
                                                accentColor = IridescentPink
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "NUMBER FORMAT",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = glass.textSecondary
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(IridescentPink.copy(alpha = 0.16f))
                                                    .border(1.5.dp, IridescentPink.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                                    .clickable {
                                                        pageNumberFormatStyle =
                                                            if (pageNumberFormatStyle == "PAGE_X_OF_Y") "NUMBER_ONLY" else "PAGE_X_OF_Y"
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 15.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (pageNumberFormatStyle == "PAGE_X_OF_Y") "Page 1 of N" else "1, 2, 3...",
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = IridescentPink
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "OUTPUT FILENAME",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = glass.textSecondary
                                    )
                                    GlassOutlinedFilenameInput(
                                        value = pageNumberOutputName,
                                        onValueChange = { pageNumberOutputName = it },
                                        placeholder = "${activeDoc.title.removeSuffix(".pdf")}-numbered",
                                        accentColor = IridescentPink
                                    )
                                }
                            }
                        }
                    }

                    // ============================================================
                    // METADATA INSPECTOR & XMP EDITOR (Paperflow Style)
                    // ============================================================
                    "metadata" -> {
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 26.dp,
                                tintColor = LiquidCyan
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "DOCUMENT METADATA & XMP EDITOR",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = LiquidCyan
                                    )
                                    Text(
                                        text = "${activeDoc.pageCount} pages • ${GlassPaperViewModel.formatFileSize(activeDoc.fileSizeBytes)} • ${if (activeDoc.passwordProtectionHash.isNotBlank()) "AES-256 Encrypted" else "Standard Unlocked PDF"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldGreen
                                    )

                                    Text("TITLE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = glass.textSecondary)
                                    GlassOutlinedFilenameInput(
                                        value = metaTitle,
                                        onValueChange = { metaTitle = it },
                                        placeholder = "Document Title",
                                        accentColor = LiquidCyan
                                    )

                                    Text("AUTHOR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = glass.textSecondary)
                                    GlassOutlinedFilenameInput(
                                        value = metaAuthor,
                                        onValueChange = { metaAuthor = it },
                                        placeholder = "Author Name",
                                        accentColor = LiquidCyan
                                    )

                                    Text("SUBJECT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = glass.textSecondary)
                                    GlassOutlinedFilenameInput(
                                        value = metaSubject,
                                        onValueChange = { metaSubject = it },
                                        placeholder = "Subject / Topic",
                                        accentColor = LiquidCyan
                                    )

                                    Text("KEYWORDS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = glass.textSecondary)
                                    GlassOutlinedFilenameInput(
                                        value = metaKeywords,
                                        onValueChange = { metaKeywords = it },
                                        placeholder = "comma, separated, keywords",
                                        accentColor = LiquidCyan
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("CREATOR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = glass.textSecondary)
                                            GlassOutlinedFilenameInput(
                                                value = metaCreator,
                                                onValueChange = { metaCreator = it },
                                                placeholder = "Paperflow",
                                                accentColor = LiquidCyan
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("PRODUCER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = glass.textSecondary)
                                            GlassOutlinedFilenameInput(
                                                value = metaProducer,
                                                onValueChange = { metaProducer = it },
                                                placeholder = "PDFBox Engine",
                                                accentColor = LiquidCyan
                                            )
                                        }
                                    }

                                    Text("OUTPUT FILENAME", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = glass.textSecondary)
                                    GlassOutlinedFilenameInput(
                                        value = metaOutputName,
                                        onValueChange = { metaOutputName = it },
                                        placeholder = "${activeDoc.title.removeSuffix(".pdf")}-metadata",
                                        accentColor = LiquidCyan
                                    )
                                }
                            }
                        }
                    }

                    // ============================================================
                    // UNIVERSAL AI PDF TRANSLATOR (Every Language to Every Language)
                    // ============================================================
                    "translate_pdf" -> {
                        item {
                            LiquidGlassPanel(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 26.dp,
                                tintColor = LiquidCyan
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Translate,
                                                contentDescription = null,
                                                tint = LiquidCyan,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "UNIVERSAL LANGUAGE MATRIX (100+ LANGUAGES)",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    letterSpacing = 1.1.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = LiquidCyan
                                            )
                                        }
                                    }

                                    // Source Language <---> Swap <---> Target Language Selector Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // FROM LANGUAGE CARD
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(
                                                    if (glass.isDark) Color(0xFF060A12).copy(alpha = 0.82f)
                                                    else Color.White.copy(alpha = 0.82f)
                                                )
                                                .border(
                                                    width = 1.5.dp,
                                                    color = LiquidCyan.copy(alpha = 0.65f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .testTag("translate_source_lang_button")
                                                .clickable {
                                                    languageSearchQuery = ""
                                                    showLanguagePickerFor = "SOURCE"
                                                }
                                                .padding(horizontal = 12.dp, vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "TRANSLATE FROM",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    letterSpacing = 0.9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = glass.textSecondary
                                            )
                                            Text(
                                                text = if (translateCustomSourceLanguage.isNotBlank()) {
                                                    "🌍 ${translateCustomSourceLanguage.trim()}"
                                                } else {
                                                    "${translateSourceLanguage.flag} ${translateSourceLanguage.name}"
                                                },
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = glass.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (translateCustomSourceLanguage.isNotBlank()) {
                                                    "Custom language"
                                                } else {
                                                    translateSourceLanguage.nativeName
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LiquidCyan,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // SWAP LANGUAGES ORB BUTTON
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(LiquidCyan, ElectricBlue, PrismPurple)
                                                    )
                                                )
                                                .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                                .testTag("translate_swap_languages_button")
                                                .clickable {
                                                    if (translateCustomSourceLanguage.isNotBlank() || translateCustomTargetLanguage.isNotBlank()) {
                                                        val tmpCustom = translateCustomSourceLanguage
                                                        translateCustomSourceLanguage = translateCustomTargetLanguage
                                                        translateCustomTargetLanguage = tmpCustom.ifBlank {
                                                            if (translateSourceLanguage.code == "auto") "English" else translateSourceLanguage.name
                                                        }
                                                    } else if (translateSourceLanguage.code != "auto") {
                                                        val temp = translateSourceLanguage
                                                        translateSourceLanguage = translateTargetLanguage
                                                        translateTargetLanguage = temp
                                                    } else {
                                                        // If source was Auto-Detect, swap sets source to current target and target to English
                                                        val english = PaperflowAiService.SUPPORTED_TRANSLATION_LANGUAGES
                                                            .firstOrNull { it.code == "en" }
                                                            ?: PaperflowAiService.SUPPORTED_TRANSLATION_LANGUAGES[1]
                                                        translateSourceLanguage = translateTargetLanguage
                                                        translateTargetLanguage = english
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.SwapHoriz,
                                                contentDescription = "Swap Languages",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        // TO LANGUAGE CARD
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(
                                                    if (glass.isDark) Color(0xFF060A12).copy(alpha = 0.82f)
                                                    else Color.White.copy(alpha = 0.82f)
                                                )
                                                .border(
                                                    width = 1.5.dp,
                                                    color = IridescentPink.copy(alpha = 0.75f),
                                                    shape = RoundedCornerShape(18.dp)
                                                )
                                                .testTag("translate_target_lang_button")
                                                .clickable {
                                                    languageSearchQuery = ""
                                                    showLanguagePickerFor = "TARGET"
                                                }
                                                .padding(horizontal = 12.dp, vertical = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "TRANSLATE TO",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    letterSpacing = 0.9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                ),
                                                color = glass.textSecondary
                                            )
                                            Text(
                                                text = if (translateCustomTargetLanguage.isNotBlank()) {
                                                    "🌍 ${translateCustomTargetLanguage.trim()}"
                                                } else {
                                                    "${translateTargetLanguage.flag} ${translateTargetLanguage.name}"
                                                },
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = glass.textPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (translateCustomTargetLanguage.isNotBlank()) {
                                                    "Custom language"
                                                } else {
                                                    translateTargetLanguage.nativeName
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = IridescentPink,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Quick Popular Target Language Chips
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "QUICK TARGET LANGUAGES",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.0.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        val quickCodes = listOf("en", "es", "fr", "de", "hi", "bn", "ar", "zh-CN", "ja", "ko", "ru", "pt")
                                        val quickLangs = remember {
                                            PaperflowAiService.SUPPORTED_TRANSLATION_LANGUAGES.filter { it.code in quickCodes }
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            quickLangs.chunked(4).forEach { rowLangs ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    rowLangs.forEach { langOpt ->
                                                        val isSelected = translateCustomTargetLanguage.isBlank() &&
                                                            translateTargetLanguage.code == langOpt.code
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(
                                                                    if (isSelected) LiquidCyan.copy(alpha = 0.24f)
                                                                    else Color.White.copy(alpha = if (glass.isDark) 0.05f else 0.45f)
                                                                )
                                                                .border(
                                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                                    color = if (isSelected) LiquidCyan else Color.White.copy(alpha = 0.28f),
                                                                    shape = RoundedCornerShape(12.dp)
                                                                )
                                                                .clickable {
                                                                    translateCustomTargetLanguage = ""
                                                                    translateTargetLanguage = langOpt
                                                                }
                                                                .padding(vertical = 8.dp, horizontal = 4.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "${langOpt.flag} ${langOpt.name.substringBefore(" ")}",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontWeight = FontWeight.ExtraBold
                                                                ),
                                                                color = if (isSelected) LiquidCyan else glass.textPrimary,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Custom Any-Language Input (Supports ANY world dialect, regional language, or historical script)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "CUSTOM TARGET LANGUAGE / DIALECT (OPTIONAL — ANY WORLD LANGUAGE)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 0.9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        GlassOutlinedFilenameInput(
                                            value = translateCustomTargetLanguage,
                                            onValueChange = { translateCustomTargetLanguage = it },
                                            placeholder = "e.g., Assamese, Bhojpuri, Cantonese, Kurdish, Sanskrit, Maori...",
                                            accentColor = IridescentPink,
                                            testTag = "translate_custom_language_input"
                                        )
                                    }

                                    // Translation Mode Options: Preserve Structure & Bilingual Side-by-Side
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (translatePreserveFormatting) EmeraldGreen.copy(alpha = 0.20f)
                                                    else Color.White.copy(alpha = if (glass.isDark) 0.05f else 0.4f)
                                                )
                                                .border(
                                                    width = if (translatePreserveFormatting) 1.5.dp else 1.dp,
                                                    color = if (translatePreserveFormatting) EmeraldGreen else Color.White.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clickable { translatePreserveFormatting = !translatePreserveFormatting }
                                                .padding(vertical = 12.dp, horizontal = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (translatePreserveFormatting) "✓ Preserve Layout" else "Preserve Layout",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = if (translatePreserveFormatting) EmeraldGreen else glass.textPrimary
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    if (translateBilingualMode) PrismPurple.copy(alpha = 0.24f)
                                                    else Color.White.copy(alpha = if (glass.isDark) 0.05f else 0.4f)
                                                )
                                                .border(
                                                    width = if (translateBilingualMode) 1.5.dp else 1.dp,
                                                    color = if (translateBilingualMode) PrismPurple else Color.White.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clickable { translateBilingualMode = !translateBilingualMode }
                                                .padding(vertical = 12.dp, horizontal = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (translateBilingualMode) "✓ Bilingual (Orig + Trans)" else "Translated Only",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = if (translateBilingualMode) IridescentPink else glass.textPrimary
                                            )
                                        }
                                    }

                                    // Page Selection Controls (All pages by default, or custom subset)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "PAGES TO TRANSLATE (${selectedPagesZeroBased.size} OF ${activeDoc.pageCount})",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 1.0.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = glass.textSecondary
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "ALL PAGES",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = LiquidCyan,
                                                modifier = Modifier.clickable {
                                                    selectedPagesZeroBased.clear()
                                                    for (i in 0 until activeDoc.pageCount) selectedPagesZeroBased.add(i)
                                                }
                                            )
                                            Text(
                                                text = "FIRST PAGE ONLY",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                color = IridescentPink,
                                                modifier = Modifier.clickable {
                                                    selectedPagesZeroBased.clear()
                                                    if (activeDoc.pageCount > 0) selectedPagesZeroBased.add(0)
                                                }
                                            )
                                        }
                                    }

                                    Text(
                                        text = "OUTPUT PDF FILENAME",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.0.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = glass.textSecondary
                                    )
                                    GlassOutlinedFilenameInput(
                                        value = translateOutputName,
                                        onValueChange = { translateOutputName = it },
                                        placeholder = "${activeDoc.title.removeSuffix(".pdf")}-translated",
                                        accentColor = LiquidCyan,
                                        testTag = "translate_output_filename_input"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // BOTTOM PINNED FULL-WIDTH ACTION PILL BUTTON (Matches Reference Photos 2-7)
        // ====================================================================
        val showBottomBar = when (tool.id) {
            "merge" -> mergeQueue.isNotEmpty()
            "image_to_pdf" -> selectedImageUris.isNotEmpty()
            else -> selectedPrimaryDoc != null
        }

        if (showBottomBar) {
            val activeDoc = selectedPrimaryDoc
            val buttonLabel: String
            val buttonIcon: ImageVector
            val isButtonEnabled: Boolean

            when (tool.id) {
                "merge" -> {
                    buttonLabel = "MERGE PDFS"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = mergeQueue.isNotEmpty() && !toolState.isRunning
                }
                "split", "extract_pages" -> {
                    buttonLabel = "EXTRACT ${selectedPagesZeroBased.size} PAGES"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = selectedPagesZeroBased.isNotEmpty() && !toolState.isRunning
                }
                "delete_pages" -> {
                    val remaining = ((activeDoc?.pageCount ?: 0) - selectedPagesZeroBased.size).coerceAtLeast(0)
                    buttonLabel = "DELETE ${selectedPagesZeroBased.size} PAGES (KEEP $remaining)"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = selectedPagesZeroBased.isNotEmpty() && remaining > 0 && !toolState.isRunning
                }
                "rearrange" -> {
                    buttonLabel = "SAVE NEW ORDER"
                    buttonIcon = Icons.Filled.Refresh
                    isButtonEnabled = rearrangedPageOrder.isNotEmpty() && !toolState.isRunning
                }
                "watermark" -> {
                    buttonLabel = "APPLY WATERMARK"
                    buttonIcon = Icons.Filled.TextFields
                    isButtonEnabled = watermarkText.isNotBlank() && !toolState.isRunning
                }
                "signature" -> {
                    buttonLabel = "SIGN & SAVE"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = (signaturePoints.isNotEmpty() || uploadedSignatureUri != null) && !toolState.isRunning
                }
                "protect" -> {
                    buttonLabel = "ENCRYPT & SAVE"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = protectNewPassword.length >= 4 && protectNewPassword == protectConfirmPassword && !toolState.isRunning
                }
                "unlock" -> {
                    buttonLabel = "UNLOCK & SAVE"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = protectNewPassword.isNotBlank() && !toolState.isRunning
                }
                "rotate" -> {
                    buttonLabel = "ROTATE ALL PAGES $rotationDegrees°"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
                "page_numbers" -> {
                    buttonLabel = "STAMP PAGE NUMBERS"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
                "compress" -> {
                    buttonLabel = "COMPRESS & SAVE"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
                "repair" -> {
                    buttonLabel = "REPAIR & SAVE"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
                "grayscale" -> {
                    buttonLabel = "CONVERT TO GRAYSCALE"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
                "pdf_to_image", "extract_images" -> {
                    buttonLabel = "EXPORT ${activeDoc?.pageCount ?: 0} PNG IMAGES"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
                "pdf_to_text" -> {
                    buttonLabel = "EXTRACT DOCUMENT TEXT"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
                "metadata" -> {
                    buttonLabel = "SAVE UPDATED METADATA"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
                "image_to_pdf" -> {
                    buttonLabel = "CREATE PDF (${selectedImageUris.size} IMAGES)"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = selectedImageUris.isNotEmpty() && !toolState.isRunning
                }
                "translate_pdf" -> {
                    val targetDisplay = translateCustomTargetLanguage.trim()
                        .ifBlank { translateTargetLanguage.name.uppercase() }
                    buttonLabel = "TRANSLATE TO $targetDisplay"
                    buttonIcon = Icons.Filled.Translate
                    isButtonEnabled = selectedPrimaryDoc != null && !toolState.isRunning
                }
                else -> {
                    buttonLabel = "RUN ${tool.title.uppercase()}"
                    buttonIcon = Icons.AutoMirrored.Filled.ArrowForward
                    isButtonEnabled = !toolState.isRunning
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (glass.isDark) Color(0xEE070B14) else Color(0xEEEDF2FF)
                            )
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(
                            elevation = if (isButtonEnabled) 16.dp else 0.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = IridescentPink,
                            spotColor = ElectricBlue
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = if (isButtonEnabled) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        LiquidMagenta,
                                        IridescentPink,
                                        PrismPurple,
                                        ElectricBlue
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        LiquidMagenta.copy(alpha = 0.38f),
                                        PrismPurple.copy(alpha = 0.35f)
                                    )
                                )
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = if (isButtonEnabled) 0.7f else 0.25f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .testTag("tool_primary_action_button")
                        .clickable(enabled = isButtonEnabled) {
                            when (tool.id) {
                                "merge" -> {
                                    viewModel.executeMergePdfEntries(
                                        entries = mergeQueue.map {
                                            MergeFileEntry(
                                                filePath = it.doc.filePath,
                                                title = it.doc.title,
                                                pageCount = it.doc.pageCount,
                                                fileSizeBytes = it.doc.fileSizeBytes,
                                                rotationDegrees = it.rotationDegrees,
                                                searchableText = it.doc.searchableText
                                            )
                                        },
                                        outputTitle = mergeOutputFilename
                                    )
                                }
                                "split", "extract_pages" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executePageSelectionTool(
                                            doc = doc,
                                            selectedZeroBasedPages = selectedPagesZeroBased.sorted(),
                                            operationLabel = if (tool.id == "split") "Split" else "Extracted"
                                        )
                                    }
                                }
                                "delete_pages" -> {
                                    activeDoc?.let { doc ->
                                        val keepPages = (0 until doc.pageCount).filter { !selectedPagesZeroBased.contains(it) }
                                        viewModel.executePageSelectionTool(
                                            doc = doc,
                                            selectedZeroBasedPages = keepPages,
                                            operationLabel = "Cleaned"
                                        )
                                    }
                                }
                                "rearrange" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executePageSelectionTool(
                                            doc = doc,
                                            selectedZeroBasedPages = rearrangedPageOrder.toList(),
                                            operationLabel = "Rearranged",
                                            perSlotRotations = rearrangedSlotRotations.toMap()
                                        )
                                    }
                                }
                                "watermark" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeCustomWatermark(
                                            doc = doc,
                                            config = WatermarkConfig(
                                                text = watermarkText,
                                                colorArgb = selectedWatermarkColor.toArgb(),
                                                opacityPercent = watermarkOpacityPercent.roundToInt(),
                                                fontSizePx = watermarkFontSizePx,
                                                rotationDegrees = watermarkRotationDeg,
                                                outputFilename = watermarkOutputName
                                            )
                                        )
                                    }
                                }
                                "signature" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeSignatureStamp(
                                            doc = doc,
                                            config = SignatureStampConfig(
                                                targetPageIndex = signatureTargetPageIdx.coerceIn(0, (doc.pageCount - 1).coerceAtLeast(0)),
                                                normalizedPositionX = sigNormalizedX,
                                                normalizedPositionY = sigNormalizedY,
                                                strokePoints = signaturePoints.toList(),
                                                uploadedSignatureUri = if (signatureMode == "UPLOAD") uploadedSignatureUri else null,
                                                outputFilename = signatureOutputName
                                            )
                                        )
                                    }
                                }
                                "protect" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeProtectPdf(
                                            doc = doc,
                                            newPassword = protectNewPassword,
                                            confirmPassword = protectConfirmPassword,
                                            outputFilename = protectOutputName
                                        )
                                    }
                                }
                                "unlock" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeUnlockPdf(
                                            doc = doc,
                                            passwordInput = protectNewPassword,
                                            outputFilename = unlockOutputName
                                        )
                                    }
                                }
                                "rotate" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeRotatePdf(doc, rotationDegrees, perPageRotations = perPageRotations.toMap())
                                    }
                                }
                                "page_numbers" -> {
                                    activeDoc?.let { doc ->
                                        val startPage = pageNumberStartPageInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                        viewModel.executePageNumbers(
                                            doc = doc,
                                            config = PageNumbersConfig(
                                                position = pageNumberPosition,
                                                startFromPage = startPage,
                                                formatStyle = pageNumberFormatStyle,
                                                outputFilename = pageNumberOutputName
                                            )
                                        )
                                    }
                                }
                                "compress" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeCompressPdf(
                                            doc = doc,
                                            level = selectedCompressionLevel,
                                            customOutputFilename = compressOutputName
                                        )
                                    }
                                }
                                "repair" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeRepairPdf(doc)
                                    }
                                }
                                "grayscale" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeOptimizeOrGrayscalePdf(doc, grayscale = true, scaleFactor = 1.0f, label = "Grayscale")
                                    }
                                }
                                "pdf_to_image", "extract_images" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executePdfToImages(doc, extractEmbeddedOnly = (tool.id == "extract_images"))
                                    }
                                }
                                "pdf_to_text" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executePdfToText(doc)
                                    }
                                }
                                "metadata" -> {
                                    activeDoc?.let { doc ->
                                        viewModel.executeUpdateMetadata(
                                            doc = doc,
                                            metadata = PdfDetailedMetadata(
                                                title = metaTitle,
                                                author = metaAuthor,
                                                subject = metaSubject,
                                                keywords = metaKeywords,
                                                creator = metaCreator,
                                                producer = metaProducer
                                            ),
                                            customOutputFilename = metaOutputName
                                        )
                                    }
                                }
                                "image_to_pdf" -> {
                                    viewModel.executeImageToPdf(selectedImageUris.toList(), imageToPdfOutputName)
                                }
                                "translate_pdf" -> {
                                    activeDoc?.let { doc ->
                                        val effectiveSource = translateCustomSourceLanguage.trim()
                                            .ifBlank { translateSourceLanguage.name }
                                        val effectiveTarget = translateCustomTargetLanguage.trim()
                                            .ifBlank { translateTargetLanguage.name }
                                        viewModel.executeTranslatePdf(
                                            doc = doc,
                                            sourceLanguage = effectiveSource,
                                            targetLanguage = effectiveTarget,
                                            selectedZeroBasedPages = selectedPagesZeroBased.sorted(),
                                            includeOriginalBilingual = translateBilingualMode,
                                            customOutputFilename = translateOutputName
                                        )
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (tool.id == "rearrange" || tool.id == "watermark") {
                            Icon(
                                imageVector = buttonIcon,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = if (isButtonEnabled) 1f else 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = buttonLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                letterSpacing = 1.3.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White.copy(alpha = if (isButtonEnabled) 1f else 0.6f)
                        )
                        if (tool.id != "rearrange" && tool.id != "watermark") {
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = buttonIcon,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = if (isButtonEnabled) 1f else 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ========================================================================
    // MODALS: Library File Picker & Freehand Signature Pad Dialog
    // ========================================================================
    if (showLibraryPickerModal) {
        AlertDialog(
            onDismissRequest = { showLibraryPickerModal = false },
            title = { Text("Select PDF Document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showLibraryPickerModal = false
                            if (tool.id == "merge") {
                                multiPdfLauncher.launch(arrayOf("application/pdf"))
                            } else {
                                singlePdfLauncher.launch(arrayOf("application/pdf"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Browse Device Storage", color = Color.White)
                    }

                    if (documents.isNotEmpty()) {
                        Text(
                            text = "Or pick from Paperflow Library:",
                            style = MaterialTheme.typography.labelMedium,
                            color = glass.textSecondary
                        )
                        Column(
                            modifier = Modifier.height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(documents, key = { it.id }) { docItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(ElectricBlue.copy(alpha = 0.12f))
                                            .clickable {
                                                if (tool.id == "merge") {
                                                    mergeQueue.add(
                                                        MergeQueueItem(
                                                            key = System.nanoTime(),
                                                            doc = docItem,
                                                            rotationDegrees = 0
                                                        )
                                                    )
                                                } else {
                                                    selectedPrimaryDoc = docItem
                                                }
                                                showLibraryPickerModal = false
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PictureAsPdf,
                                            contentDescription = null,
                                            tint = IridescentPink,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = docItem.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${docItem.pageCount} pages • ${GlassPaperViewModel.formatFileSize(docItem.fileSizeBytes)}",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLibraryPickerModal = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showLanguagePickerFor != null) {
        val isPickingSource = showLanguagePickerFor == "SOURCE"
        val availableLanguages = remember(isPickingSource, languageSearchQuery) {
            val baseList = if (isPickingSource) {
                PaperflowAiService.SUPPORTED_TRANSLATION_LANGUAGES
            } else {
                PaperflowAiService.SUPPORTED_TRANSLATION_LANGUAGES.filter { it.code != "auto" }
            }
            if (languageSearchQuery.isBlank()) baseList
            else baseList.filter {
                it.name.contains(languageSearchQuery, ignoreCase = true) ||
                    it.nativeName.contains(languageSearchQuery, ignoreCase = true) ||
                    it.code.contains(languageSearchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showLanguagePickerFor = null },
            title = {
                Text(
                    text = if (isPickingSource) "Select Source Language (Every Language)" else "Select Target Language (Every Language)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassOutlinedFilenameInput(
                        value = languageSearchQuery,
                        onValueChange = { languageSearchQuery = it },
                        placeholder = "Search 100+ languages or type any dialect...",
                        accentColor = if (isPickingSource) LiquidCyan else IridescentPink,
                        testTag = "language_picker_search_input"
                    )

                    if (languageSearchQuery.isNotBlank() && availableLanguages.isEmpty()) {
                        Button(
                            onClick = {
                                if (isPickingSource) {
                                    translateCustomSourceLanguage = languageSearchQuery.trim()
                                } else {
                                    translateCustomTargetLanguage = languageSearchQuery.trim()
                                }
                                showLanguagePickerFor = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrismPurple)
                        ) {
                            Text("Use Custom Language: \"${languageSearchQuery.trim()}\"", color = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(availableLanguages, key = { it.code }) { langItem ->
                                val isCurrent = if (isPickingSource) {
                                    translateCustomSourceLanguage.isBlank() && translateSourceLanguage.code == langItem.code
                                } else {
                                    translateCustomTargetLanguage.isBlank() && translateTargetLanguage.code == langItem.code
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isCurrent) LiquidCyan.copy(alpha = 0.22f)
                                            else ElectricBlue.copy(alpha = 0.09f)
                                        )
                                        .border(
                                            width = if (isCurrent) 1.5.dp else 0.8.dp,
                                            color = if (isCurrent) LiquidCyan else Color.White.copy(alpha = 0.22f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            if (isPickingSource) {
                                                translateCustomSourceLanguage = ""
                                                translateSourceLanguage = langItem
                                            } else {
                                                translateCustomTargetLanguage = ""
                                                translateTargetLanguage = langItem
                                            }
                                            showLanguagePickerFor = null
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = langItem.flag,
                                            fontSize = 20.sp
                                        )
                                        Column {
                                            Text(
                                                text = langItem.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${langItem.nativeName} (${langItem.code})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = glass.textSecondary
                                            )
                                        }
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = LiquidCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (languageSearchQuery.isNotBlank()) {
                    TextButton(
                        onClick = {
                            if (isPickingSource) {
                                translateCustomSourceLanguage = languageSearchQuery.trim()
                            } else {
                                translateCustomTargetLanguage = languageSearchQuery.trim()
                            }
                            showLanguagePickerFor = null
                        }
                    ) {
                        Text("Use \"${languageSearchQuery.trim()}\"")
                    }
                }
                TextButton(onClick = { showLanguagePickerFor = null }) {
                    Text("Close")
                }
            }
        )
    }

    if (showSignatureDrawModal) {
        AlertDialog(
            onDismissRequest = { showSignatureDrawModal = false },
            title = { Text("Draw Your Ink Signature") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Sign inside the canvas below. You can drag the signature anywhere on the page preview.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(2.dp, LiquidMagenta, RoundedCornerShape(18.dp))
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            signaturePoints.add(
                                                Pair(
                                                    (offset.x / size.width).coerceIn(0f, 1f),
                                                    (offset.y / size.height).coerceIn(0f, 1f)
                                                )
                                            )
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            signaturePoints.add(
                                                Pair(
                                                    (change.position.x / size.width).coerceIn(0f, 1f),
                                                    (change.position.y / size.height).coerceIn(0f, 1f)
                                                )
                                            )
                                        },
                                        onDragEnd = {
                                            signaturePoints.add(Pair(-1f, -1f))
                                        }
                                    )
                                }
                        ) {
                            val path = Path()
                            var down = false
                            signaturePoints.forEach { pt ->
                                if (pt.first < 0f || pt.second < 0f) {
                                    down = false
                                } else {
                                    val px = pt.first * size.width
                                    val py = pt.second * size.height
                                    if (!down) {
                                        path.moveTo(px, py)
                                        down = true
                                    } else {
                                        path.lineTo(px, py)
                                    }
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF1D4ED8),
                                style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        signatureMode = "DRAW"
                        showSignatureDrawModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidMagenta)
                ) {
                    Text("Use Signature", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { signaturePoints.clear() }) {
                    Text("Clear Pad")
                }
            }
        )
    }
}

// ============================================================================
// REUSABLE LIQUID GLASS ARRANGE-REARRANGE COMPONENTS (Matching Photos 1–7)
// ============================================================================

@Composable
private fun SelectPdfDashedGlassDropzone(
    title: String,
    subtitle: String,
    accentColor: Color,
    onBrowseDevice: () -> Unit,
    onChooseFromLibrary: (() -> Unit)?
) {
    val glass = LocalGlassColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(310.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (glass.isDark) 0.12f else 0.16f),
                        ElectricBlue.copy(alpha = if (glass.isDark) 0.08f else 0.10f)
                    )
                )
            )
            .drawBehind {
                val stroke = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                )
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.65f),
                    cornerRadius = CornerRadius(32.dp.toPx(), 32.dp.toPx()),
                    style = stroke
                )
            }
            .testTag("select_pdf_dropzone")
            .clickable { onBrowseDevice() }
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(16.dp, CircleShape, ambientColor = accentColor)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.35f),
                                accentColor.copy(alpha = 0.14f)
                            )
                        )
                    )
                    .border(1.5.dp, accentColor.copy(alpha = 0.75f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FileUpload,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = glass.textSecondary,
                textAlign = TextAlign.Center
            )
            if (onChooseFromLibrary != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ElectricBlue.copy(alpha = 0.22f))
                        .border(1.dp, LiquidCyan.copy(alpha = 0.7f), RoundedCornerShape(50))
                        .clickable { onChooseFromLibrary() }
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Choose from Paperflow Library",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (glass.isDark) Color.White else ElectricBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun MergeFileGlassRowCard(
    item: MergeQueueItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRotate90: () -> Unit,
    onRemove: () -> Unit
) {
    val glass = LocalGlassColors.current
    val context = LocalContext.current
    val thumbnail by produceState<Bitmap?>(initialValue = null, item.doc.filePath) {
        value = PdfEngine.renderThumbnail(context, item.doc.filePath, pageIndex = 0, targetWidth = 220)
    }

    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        tintColor = IridescentPink
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 6-Dot Drag / Reorder Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = canMoveUp,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = "Move Up",
                        tint = if (canMoveUp) IridescentPink else glass.textMuted.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = "Reorder handle",
                    tint = IridescentPink,
                    modifier = Modifier.size(22.dp)
                )
                IconButton(
                    onClick = onMoveDown,
                    enabled = canMoveDown,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = "Move Down",
                        tint = if (canMoveDown) IridescentPink else glass.textMuted.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Page 1 Cover Thumbnail with live rotation indicator
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 74.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail!!.asImageBitmap(),
                        contentDescription = item.doc.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(item.rotationDegrees.toFloat())
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        tint = IridescentPink,
                        modifier = Modifier.size(28.dp)
                    )
                }
                if (item.rotationDegrees != 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(topStart = 6.dp))
                            .background(ElectricBlue)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${item.rotationDegrees}°",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & "64.40 MB • 250 pages"
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.doc.title.removeSuffix(".pdf"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = glass.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${GlassPaperViewModel.formatFileSize(item.doc.fileSizeBytes)}  •  ${item.doc.pageCount} pages",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = glass.textSecondary
                )
            }

            // Rotate Button (Circular arrow like Photo 2)
            IconButton(onClick = onRotate90) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Rotate file 90°",
                    tint = glass.textSecondary
                )
            }

            // Remove 'X' Button
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove file",
                    tint = glass.textSecondary
                )
            }
        }
    }
}

@Composable
private fun DashedAddMoreGlassButton(
    label: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val glass = LocalGlassColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = if (glass.isDark) 0.04f else 0.35f))
            .drawBehind {
                val stroke = Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f)
                )
                drawRoundRect(
                    color = accentColor.copy(alpha = 0.55f),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                    style = stroke
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = glass.textPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = glass.textPrimary
            )
        }
    }
}

@Composable
private fun SelectedDocumentHeaderGlassCard(
    doc: PdfDocumentEntity,
    accentColor: Color,
    onSwitchOrClear: () -> Unit,
    onCloseFile: () -> Unit
) {
    val glass = LocalGlassColors.current
    val context = LocalContext.current
    val thumb by produceState<Bitmap?>(initialValue = null, doc.filePath) {
        value = PdfEngine.renderThumbnail(context, doc.filePath, pageIndex = 0, targetWidth = 220)
    }

    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        tintColor = accentColor,
        onClick = onSwitchOrClear
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 74.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (thumb != null) {
                    Image(
                        bitmap = thumb!!.asImageBitmap(),
                        contentDescription = doc.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.title.removeSuffix(".pdf"),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = glass.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${doc.pageCount} PAGES • ${GlassPaperViewModel.formatFileSize(doc.fileSizeBytes)}",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = glass.textSecondary
                )
            }

            IconButton(onClick = onCloseFile) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close file",
                    tint = glass.textSecondary
                )
            }
        }
    }
}

@Composable
private fun HeavyDocumentRangeBanner(
    rangeInput: String,
    showRangeField: Boolean,
    onToggleRangeField: () -> Unit,
    onRangeInputChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SolarAmber.copy(alpha = 0.14f))
            .border(1.dp, SolarAmber.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .clickable { onToggleRangeField() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SolarAmber.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = SolarAmber,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "HEAVY DOCUMENT / RANGE SELECTION",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 1.1.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = SolarAmber
                )
                Text(
                    text = "Visual selection for large files may be slower. Tap here to use range selection (e.g. 1-5, 8, 11-15) for speed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SolarAmber.copy(alpha = 0.9f)
                )
            }
        }

        AnimatedVisibility(visible = showRangeField) {
            GlassOutlinedFilenameInput(
                value = rangeInput,
                onValueChange = onRangeInputChange,
                placeholder = "e.g. 1-5, 8, 10-12",
                accentColor = SolarAmber
            )
        }
    }
}

@Composable
private fun SelectablePageThumbnailCard(
    doc: PdfDocumentEntity,
    pageIndex: Int,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val thumb by produceState<Bitmap?>(initialValue = null, doc.filePath, pageIndex) {
        value = PdfEngine.renderThumbnail(context, doc.filePath, pageIndex = pageIndex, targetWidth = 300)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) accentColor else Color.White.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
    ) {
        if (thumb != null) {
            Image(
                bitmap = thumb!!.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                PlayStoreOrganicBlobSpinner(indicatorSize = 34.dp)
            }
        }

        // Center Glowing Checkmark Badge when selected (Matches Photo 3)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(44.dp)
                    .shadow(10.dp, CircleShape, ambientColor = accentColor)
                    .clip(CircleShape)
                    .background(accentColor)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom-left "PAGE X" Pill Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC1E293B))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "PAGE ${pageIndex + 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun RearrangePageThumbnailCard(
    doc: PdfDocumentEntity,
    originalPageIndex: Int,
    currentSlotIndex: Int,
    totalSlots: Int,
    rotationDegrees: Int = 0,
    isSwapSelected: Boolean,
    onTapCard: () -> Unit,
    onRotate90: () -> Unit = {},
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit
) {
    val context = LocalContext.current
    val thumb by produceState<Bitmap?>(initialValue = null, doc.filePath, originalPageIndex) {
        value = PdfEngine.renderThumbnail(context, doc.filePath, pageIndex = originalPageIndex, targetWidth = 300)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.74f)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
            .border(
                width = if (isSwapSelected) 3.dp else 1.5.dp,
                color = if (isSwapSelected) LiquidCyan else Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onTapCard() }
            .padding(6.dp)
    ) {
        if (thumb != null) {
            Image(
                bitmap = thumb!!.asImageBitmap(),
                contentDescription = "Page ${originalPageIndex + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .rotate(rotationDegrees.toFloat())
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                PlayStoreOrganicBlobSpinner(indicatorSize = 34.dp)
            }
        }

        // Top-right quick reorder arrows + 90° Rotate button (from Paperflow Rearrange)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC0F172A)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.RotateRight,
                contentDescription = "Rotate Page 90°",
                tint = LiquidCyan,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onRotate90() }
                    .padding(3.dp)
            )
            if (currentSlotIndex > 0) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Move Earlier",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onMoveLeft() }
                        .padding(2.dp)
                )
            }
            if (currentSlotIndex < totalSlots - 1) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Move Later",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onMoveRight() }
                        .padding(2.dp)
                )
            }
        }

        // Bottom-left "PAGE X" Pill Badge (Matches Photo 4 + Rotation indicator)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSwapSelected) LiquidCyan else Color(0xCC334155))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (rotationDegrees != 0) "PAGE ${originalPageIndex + 1} • ${rotationDegrees}°" else "PAGE ${originalPageIndex + 1}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.6.sp
                ),
                color = if (isSwapSelected) Color(0xFF0F172A) else Color.White
            )
        }
    }
}

@Composable
private fun WatermarkLivePreviewCard(
    doc: PdfDocumentEntity,
    watermarkText: String,
    watermarkColor: Color,
    opacityPercent: Float,
    fontSizePx: Float,
    rotationDegrees: Float
) {
    val glass = LocalGlassColors.current
    val context = LocalContext.current
    val previewBmp by produceState<Bitmap?>(initialValue = null, doc.filePath) {
        value = PdfEngine.renderPageHighRes(context, doc.filePath, pageIndex = 0, targetWidth = 900)
    }

    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        tintColor = ElectricBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Visibility,
                    contentDescription = null,
                    tint = glass.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIVE PREVIEW",
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = glass.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (previewBmp != null) {
                    Image(
                        bitmap = previewBmp!!.asImageBitmap(),
                        contentDescription = "Live Watermark Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (watermarkText.isNotBlank()) {
                    Text(
                        text = watermarkText,
                        color = watermarkColor.copy(alpha = (opacityPercent / 100f).coerceIn(0.08f, 1f)),
                        fontSize = (fontSizePx * 0.48f).coerceIn(12f, 54f).sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                rotationZ = rotationDegrees
                            }
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SignaturePagePlacementPreviewCard(
    doc: PdfDocumentEntity,
    pageIndex: Int,
    strokePoints: List<Pair<Float, Float>>,
    uploadedSignatureUri: Uri?,
    normalizedX: Float,
    normalizedY: Float,
    onPositionChange: (Float, Float) -> Unit,
    onTapDrawPad: () -> Unit
) {
    val context = LocalContext.current
    val pageBmp by produceState<Bitmap?>(initialValue = null, doc.filePath, pageIndex) {
        value = PdfEngine.renderPageHighRes(context, doc.filePath, pageIndex = pageIndex, targetWidth = 900)
    }
    val uploadedBmp by produceState<Bitmap?>(initialValue = null, uploadedSignatureUri) {
        value = uploadedSignatureUri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
        }
    }

    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        tintColor = LiquidMagenta
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.74f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
            ) {
                val containerW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val containerH = constraints.maxHeight.toFloat().coerceAtLeast(1f)

                if (pageBmp != null) {
                    Image(
                        bitmap = pageBmp!!.asImageBitmap(),
                        contentDescription = "Page ${pageIndex + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Interactive Draggable Signature Stamp Box
                val boxW = 140.dp
                val boxH = 68.dp
                val offsetX = ((normalizedX * containerW) - (containerW * 0.18f)).roundToInt()
                val offsetY = ((normalizedY * containerH) - (containerH * 0.08f)).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .size(width = boxW, height = boxH)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LiquidMagenta.copy(alpha = 0.10f))
                        .border(1.5.dp, LiquidMagenta, RoundedCornerShape(10.dp))
                        .pointerInput(containerW, containerH) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val nextX = (normalizedX + dragAmount.x / containerW).coerceIn(0.18f, 0.82f)
                                val nextY = (normalizedY + dragAmount.y / containerH).coerceIn(0.10f, 0.90f)
                                onPositionChange(nextX, nextY)
                            }
                        }
                        .clickable { onTapDrawPad() }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uploadedBmp != null) {
                        Image(
                            bitmap = uploadedBmp!!.asImageBitmap(),
                            contentDescription = "Uploaded Signature",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (strokePoints.isNotEmpty()) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            var down = false
                            strokePoints.forEach { pt ->
                                if (pt.first < 0f || pt.second < 0f) {
                                    down = false
                                } else {
                                    val px = pt.first * size.width
                                    val py = pt.second * size.height
                                    if (!down) {
                                        path.moveTo(px, py)
                                        down = true
                                    } else {
                                        path.lineTo(px, py)
                                    }
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF1D4ED8),
                                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    } else {
                        Text(
                            text = "Tap to Draw\nor Drag Here",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LiquidMagenta,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassOutlinedFilenameInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    accentColor: Color,
    testTag: String = ""
) {
    val glass = LocalGlassColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (glass.isDark) Color(0xFF060A12).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.75f))
            .border(1.5.dp, accentColor.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 15.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = glass.textMuted
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = glass.textPrimary,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(accentColor),
            modifier = Modifier
                .fillMaxWidth()
                .let { if (testTag.isNotBlank()) it.testTag(testTag) else it }
        )
    }
}

@Composable
private fun GlassPasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    accentColor: Color,
    testTag: String = ""
) {
    val glass = LocalGlassColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (glass.isDark) Color(0xFF060A12).copy(alpha = 0.78f) else Color.White.copy(alpha = 0.75f))
            .border(1.5.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = glass.textMuted
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = glass.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                cursorBrush = SolidColor(accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (testTag.isNotBlank()) it.testTag(testTag) else it }
            )
        }
        IconButton(onClick = onToggleVisibility) {
            Icon(
                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = "Toggle password visibility",
                tint = glass.textSecondary
            )
        }
    }
}

private fun parsePageRangeString(input: String, maxPages: Int): List<Int> {
    if (input.isBlank() || maxPages <= 0) return emptyList()
    val result = linkedSetOf<Int>()
    val parts = input.split(",")
    for (rawPart in parts) {
        val part = rawPart.trim()
        if (part.isEmpty()) continue
        if (part.contains("-")) {
            val bounds = part.split("-")
            if (bounds.size == 2) {
                val start = bounds[0].trim().toIntOrNull()
                val end = bounds[1].trim().toIntOrNull()
                if (start != null && end != null) {
                    val s = minOf(start, end).coerceIn(1, maxPages)
                    val e = maxOf(start, end).coerceIn(1, maxPages)
                    for (p in s..e) result.add(p - 1)
                }
            }
        } else {
            val single = part.toIntOrNull()
            if (single != null && single in 1..maxPages) {
                result.add(single - 1)
            }
        }
    }
    return result.toList()
}
