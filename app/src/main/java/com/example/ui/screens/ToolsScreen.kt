package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PdfDocumentEntity
import com.example.ui.GlassPaperViewModel
import com.example.ui.ToolOperationState
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
    // EDIT (Pink / Magenta / Red accents)
    PdfToolSpec("merge", "EDIT", "Merge PDF", "Combine multiple PDFs into one document", Icons.Filled.CallMerge, IridescentPink),
    PdfToolSpec("split", "EDIT", "Split PDF", "Split a PDF by custom page ranges", Icons.Filled.CallSplit, LiquidMagenta),
    PdfToolSpec("rotate", "EDIT", "Rotate PDF", "Rotate all pages 90°, 180°, or 270°", Icons.Filled.RotateRight, Color(0xFFF43F5E)),
    PdfToolSpec("rearrange", "EDIT", "Rearrange PDF", "Reorder pages into a custom sequence", Icons.Filled.Reorder, IridescentPink),
    PdfToolSpec("delete_pages", "EDIT", "Delete Pages", "Remove unwanted pages from a PDF", Icons.Filled.DeleteSweep, LiquidMagenta),
    PdfToolSpec("extract_pages", "EDIT", "Extract Pages", "Save selected pages as a new PDF", Icons.Filled.ContentCut, Color(0xFFE11D48)),
    PdfToolSpec("page_numbers", "EDIT", "Page Numbers", "Stamp clean page numbers on every page", Icons.Filled.FormatListNumbered, IridescentPink),
    PdfToolSpec("watermark", "EDIT", "Watermark", "Add diagonal text watermark across pages", Icons.Filled.BrandingWatermark, PrismPurple),
    PdfToolSpec("signature", "EDIT", "Signature", "Draw and stamp your ink signature", Icons.Filled.Draw, LiquidMagenta),

    // OPTIMIZE (Amber / Orange accents)
    PdfToolSpec("compress", "OPTIMIZE", "Compress PDF", "Reduce file size for faster sharing", Icons.Filled.Compress, SolarAmber),
    PdfToolSpec("repair", "OPTIMIZE", "Repair PDF", "Rebuild clean PDF page streams", Icons.Filled.AutoFixHigh, Color(0xFFF97316)),
    PdfToolSpec("grayscale", "OPTIMIZE", "Grayscale", "Convert colored pages to high-contrast B&W", Icons.Filled.FilterBAndW, SolarAmber),

    // SECURITY (Violet / Electric Blue accents)
    PdfToolSpec("metadata", "SECURITY", "Metadata", "Inspect document properties, size & paths", Icons.Filled.Info, ElectricBlue),
    PdfToolSpec("protect", "SECURITY", "Protect PDF", "AES-256 standard encryption (Coming Soon)", Icons.Filled.Lock, PrismViolet, isAvailableOffline = false),
    PdfToolSpec("unlock", "SECURITY", "Unlock PDF", "Remove owner password restrictions (Coming Soon)", Icons.Filled.LockOpen, ElectricBlue, isAvailableOffline = false),

    // CONVERT (Teal / Emerald Green accents)
    PdfToolSpec("pdf_to_image", "CONVERT", "PDF to Image", "Export PDF pages as high-res PNG images", Icons.Filled.Image, CrystalTeal),
    PdfToolSpec("image_to_pdf", "CONVERT", "Image to PDF", "Convert photos into a clean PDF document", Icons.Filled.PictureAsPdf, EmeraldGreen),
    PdfToolSpec("extract_images", "CONVERT", "Extract Images", "Save rendered visual plates from PDF", Icons.Filled.Collections, CrystalTeal),
    PdfToolSpec("pdf_to_text", "CONVERT", "PDF to Text", "Extract searchable text & formulas", Icons.Filled.TextSnippet, EmeraldGreen)
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

        // Search Field: "Search for a tool..."
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    if (!tool.isAvailableOffline) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrismViolet.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "COMING SOON",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (glass.isDark) Color.White else PrismViolet
                            )
                        }
                    }
                }
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
    val tool = remember(toolId) {
        ALL_PDF_TOOLS.firstOrNull { it.id == toolId } ?: ALL_PDF_TOOLS.first()
    }

    var selectedPrimaryDoc by remember(documents) { mutableStateOf(documents.firstOrNull()) }
    val selectedMergeDocIds = remember { mutableStateListOf<Long>() }
    val selectedPagesZeroBased = remember(selectedPrimaryDoc) {
        mutableStateListOf<Int>().apply {
            selectedPrimaryDoc?.let { doc ->
                for (i in 0 until doc.pageCount) add(i)
            }
        }
    }
    var rotationDegrees by remember { mutableIntStateOf(90) }
    var watermarkInput by remember { mutableStateOf("CONFIDENTIAL • PAPERFLOW") }
    var outputNameInput by remember { mutableStateOf("Merged Document") }
    val signaturePoints = remember { mutableStateListOf<Pair<Float, Float>>() }
    val selectedImageUris = remember { mutableStateListOf<Uri>() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        selectedImageUris.clear()
        selectedImageUris.addAll(uris)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
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
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tool.title,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
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

        // Disabled / Coming-Soon State for complex encryption tools per Master Prompt Section 9
        if (!tool.isAvailableOffline) {
            item {
                LiquidGlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    tintColor = PrismViolet
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = null,
                            tint = PrismViolet,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${tool.title} • Coming Soon",
                            style = MaterialTheme.typography.titleLarge,
                            color = glass.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hardware-backed AES-256 PDF stream encryption is in active development. Your documents remain 100% local on your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glass.textSecondary
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(onClick = onBack) {
                            Text("Return to Tools")
                        }
                    }
                }
            }
            return@LazyColumn
        }

        // Progress or Result Banner
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
                        CircularProgressIndicator(color = ElectricBlue, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = toolState.progressText,
                            style = MaterialTheme.typography.titleMedium,
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
                    cornerRadius = 20.dp,
                    tintColor = LiquidMagenta
                ) {
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = LiquidMagenta,
                        modifier = Modifier.padding(16.dp)
                    )
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Saved to Library!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = "${resultDoc.title} • ${resultDoc.pageCount} pages • ${GlassPaperViewModel.formatFileSize(resultDoc.fileSizeBytes)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.textSecondary
                                )
                            }
                        }
                        Button(
                            onClick = { onOpenResultDocument(resultDoc) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Generated PDF")
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

        // Tool-specific Controls
        when (tool.id) {
            "merge" -> {
                item {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        tintColor = IridescentPink
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Select 2 or more PDFs to merge:",
                                style = MaterialTheme.typography.titleMedium,
                                color = glass.textPrimary
                            )
                            OutlinedTextField(
                                value = outputNameInput,
                                onValueChange = { outputNameInput = it },
                                label = { Text("Output Filename") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            documents.forEach { doc ->
                                val checked = selectedMergeDocIds.contains(doc.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (checked) selectedMergeDocIds.remove(doc.id)
                                            else selectedMergeDocIds.add(doc.id)
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = {
                                            if (it) selectedMergeDocIds.add(doc.id)
                                            else selectedMergeDocIds.remove(doc.id)
                                        }
                                    )
                                    Text(
                                        text = "${doc.title} (${doc.pageCount}p)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = glass.textPrimary
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val chosen = documents.filter { selectedMergeDocIds.contains(it.id) }
                                    viewModel.executeMergePdfs(chosen, outputNameInput)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Merge Selected PDFs")
                            }
                        }
                    }
                }
            }

            "image_to_pdf" -> {
                item {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        tintColor = EmeraldGreen
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Convert Device Images to PDF",
                                style = MaterialTheme.typography.titleMedium,
                                color = glass.textPrimary
                            )
                            Text(
                                text = "Selected images: ${selectedImageUris.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Pick Photos")
                                }
                                Button(
                                    onClick = {
                                        viewModel.executeImageToPdf(selectedImageUris.toList(), "Image_Collection")
                                    },
                                    enabled = selectedImageUris.isNotEmpty(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Build PDF")
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // Document selector for single-PDF tools
                item {
                    LiquidGlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp,
                        tintColor = tool.accentColor
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "1. Choose Target PDF Document",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                documents.forEach { doc ->
                                    val isSel = selectedPrimaryDoc?.id == doc.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isSel) ElectricBlue else Color.White.copy(alpha = 0.35f))
                                            .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                                            .clickable {
                                                selectedPrimaryDoc = doc
                                                selectedPagesZeroBased.clear()
                                                for (i in 0 until doc.pageCount) selectedPagesZeroBased.add(i)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = doc.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSel) Color.White else glass.textPrimary
                                        )
                                    }
                                }
                            }

                            selectedPrimaryDoc?.let { doc ->
                                when (tool.id) {
                                    "split", "extract_pages", "delete_pages", "rearrange" -> {
                                        Text(
                                            text = if (tool.id == "rearrange") "Tap pages in desired order (Included: ${selectedPagesZeroBased.map { it + 1 }.joinToString(", ")})"
                                            else "Toggle pages to include in output PDF:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = glass.textSecondary
                                        )
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            for (p in 0 until doc.pageCount) {
                                                val active = selectedPagesZeroBased.contains(p)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (active) PrismViolet else Color.White.copy(alpha = 0.3f))
                                                        .clickable {
                                                            if (active) selectedPagesZeroBased.remove(p)
                                                            else selectedPagesZeroBased.add(p)
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "Page ${p + 1}",
                                                        color = if (active) Color.White else glass.textPrimary,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.executePageSelectionTool(
                                                    doc = doc,
                                                    selectedZeroBasedPages = selectedPagesZeroBased.toList(),
                                                    operationLabel = tool.title.replace(" ", "_")
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Run ${tool.title}")
                                        }
                                    }

                                    "rotate" -> {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            listOf(90, 180, 270).forEach { deg ->
                                                val sel = rotationDegrees == deg
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (sel) ElectricBlue else Color.White.copy(alpha = 0.3f))
                                                        .clickable { rotationDegrees = deg }
                                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "$deg°",
                                                        color = if (sel) Color.White else glass.textPrimary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Button(
                                            onClick = { viewModel.executeRotatePdf(doc, rotationDegrees) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Rotate All Pages $rotationDegrees°")
                                        }
                                    }

                                    "page_numbers" -> {
                                        Button(
                                            onClick = {
                                                viewModel.executeStampPdf(doc, watermarkText = null, includePageNumbers = true)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Stamp Page Numbers (1 / ${doc.pageCount})")
                                        }
                                    }

                                    "watermark" -> {
                                        OutlinedTextField(
                                            value = watermarkInput,
                                            onValueChange = { watermarkInput = it },
                                            label = { Text("Watermark Text") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Button(
                                            onClick = {
                                                viewModel.executeStampPdf(doc, watermarkText = watermarkInput, includePageNumbers = false)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Apply Diagonal Watermark")
                                        }
                                    }

                                    "signature" -> {
                                        Text(
                                            text = "Draw your signature below to stamp onto the final page:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = glass.textSecondary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White)
                                                .border(1.5.dp, ElectricBlue, RoundedCornerShape(16.dp))
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
                                                            }
                                                        )
                                                    }
                                            ) {
                                                if (signaturePoints.size >= 2) {
                                                    val path = Path().apply {
                                                        signaturePoints.forEachIndexed { idx, pt ->
                                                            val px = pt.first * size.width
                                                            val py = pt.second * size.height
                                                            if (idx == 0) moveTo(px, py) else lineTo(px, py)
                                                        }
                                                    }
                                                    drawPath(
                                                        path = path,
                                                        color = Color(0xFF1D4ED8),
                                                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                    )
                                                }
                                            }
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Button(
                                                onClick = { signaturePoints.clear() },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Clear Pad")
                                            }
                                            Button(
                                                onClick = {
                                                    val pts = if (signaturePoints.size >= 2) signaturePoints.toList()
                                                    else listOf(0.1f to 0.7f, 0.3f to 0.3f, 0.5f to 0.6f, 0.9f to 0.4f)
                                                    viewModel.executeStampPdf(
                                                        doc = doc,
                                                        watermarkText = null,
                                                        includePageNumbers = false,
                                                        signaturePoints = pts
                                                    )
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Stamp Signature")
                                            }
                                        }
                                    }

                                    "compress" -> {
                                        Button(
                                            onClick = {
                                                viewModel.executeOptimizeOrGrayscalePdf(
                                                    doc = doc,
                                                    grayscale = false,
                                                    scaleFactor = 0.72f,
                                                    label = "Compressed"
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Compress PDF Stream")
                                        }
                                    }

                                    "repair" -> {
                                        Button(
                                            onClick = {
                                                viewModel.executeOptimizeOrGrayscalePdf(
                                                    doc = doc,
                                                    grayscale = false,
                                                    scaleFactor = 1.0f,
                                                    label = "Repaired"
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Rebuild & Repair PDF Structure")
                                        }
                                    }

                                    "grayscale" -> {
                                        Button(
                                            onClick = {
                                                viewModel.executeOptimizeOrGrayscalePdf(
                                                    doc = doc,
                                                    grayscale = true,
                                                    scaleFactor = 1.0f,
                                                    label = "Grayscale"
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Convert PDF to Grayscale")
                                        }
                                    }

                                    "metadata" -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Title: ${doc.title}", fontWeight = FontWeight.Bold, color = glass.textPrimary)
                                            Text("Category: ${doc.categoryTag}", color = glass.textSecondary)
                                            Text("Pages: ${doc.pageCount}", color = glass.textSecondary)
                                            Text("File Size: ${GlassPaperViewModel.formatFileSize(doc.fileSizeBytes)} (${doc.fileSizeBytes} bytes)", color = glass.textSecondary)
                                            Text("Last Opened: ${GlassPaperViewModel.formatRelativeTime(doc.lastOpenedTimestamp)}", color = glass.textSecondary)
                                            Text("Storage Path: ${doc.filePath}", style = MaterialTheme.typography.labelSmall, color = glass.textMuted)
                                            Text("Privacy: Local On-Device Storage Only", color = EmeraldGreen, style = MaterialTheme.typography.labelMedium)
                                        }
                                    }

                                    "pdf_to_image", "extract_images" -> {
                                        Button(
                                            onClick = { viewModel.executePdfToImages(doc) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Export All ${doc.pageCount} Pages as High-Res PNG")
                                        }
                                    }

                                    "pdf_to_text" -> {
                                        Button(
                                            onClick = { viewModel.executePdfToText(doc) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Extract Text from ${doc.title}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
