package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BookmarkEntity
import com.example.data.NoteEntity
import com.example.data.PdfDocumentEntity
import com.example.pdf.PdfEngine
import com.example.ui.GlassPaperViewModel
import com.example.ui.ImportPreviewState
import com.example.ui.components.GlassCircularIconButton
import com.example.ui.components.LiquidGlassPanel
import com.example.ui.theme.AnnotBlue
import com.example.ui.theme.AnnotGreen
import com.example.ui.theme.AnnotPink
import com.example.ui.theme.AnnotPurple
import com.example.ui.theme.AnnotYellow
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

@Composable
fun NoteEditorOverlay(
    viewModel: GlassPaperViewModel,
    existingNote: NoteEntity?,
    attachedDocId: Long?,
    attachedDocTitle: String?,
    attachedPage: Int?,
    prefilledSnippet: String?,
    documents: List<PdfDocumentEntity>,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val glass = LocalGlassColors.current
    var title by remember(existingNote) {
        mutableStateOf(existingNote?.title ?: if (!attachedDocTitle.isNullOrBlank()) "Notes on ${attachedDocTitle.removeSuffix(".pdf")}" else "")
    }
    var content by remember(existingNote, prefilledSnippet) {
        mutableStateOf(
            existingNote?.content ?: if (!prefilledSnippet.isNullOrBlank()) {
                "\"$prefilledSnippet\"\n\n• Key Takeaway: "
            } else ""
        )
    }
    var selectedDocId by remember(existingNote, attachedDocId) {
        mutableStateOf(existingNote?.attachedDocumentId ?: attachedDocId)
    }
    var selectedDocTitle by remember(existingNote, attachedDocTitle) {
        mutableStateOf(existingNote?.attachedDocumentTitle ?: attachedDocTitle)
    }
    var selectedPage by remember(existingNote, attachedPage) {
        mutableStateOf(existingNote?.attachedPageNumber ?: attachedPage)
    }
    var accentHex by remember(existingNote) {
        mutableStateOf(existingNote?.accentColorHex ?: 0xFFA855F7L)
    }
    var isFavorite by remember(existingNote) {
        mutableStateOf(existingNote?.isFavorite ?: false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("note_editor_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassCircularIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    onClick = onBack
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (existingNote == null) "Create Study Note" else "Edit Note",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = glass.textPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassCircularIconButton(
                    icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite Note",
                    tint = if (isFavorite) IridescentPink else glass.textPrimary,
                    onClick = { isFavorite = !isFavorite }
                )

                Button(
                    onClick = {
                        viewModel.saveNote(
                            id = existingNote?.id ?: 0L,
                            title = title,
                            content = content,
                            attachedDocId = selectedDocId,
                            attachedDocTitle = selectedDocTitle,
                            attachedPage = selectedPage,
                            accentColorHex = accentHex,
                            isFavorite = isFavorite
                        )
                        onBack()
                    },
                    modifier = Modifier.testTag("save_note_button")
                ) {
                    Text(stringResource(R.string.app_name).let { "Save" })
                }
            }
        }

        // Formatting Toolbar (Bold, Italic, Underline, Bullet list, Numbered list, Highlight, Checklist)
        LiquidGlassPanel(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            tintColor = Color(accentHex)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormatToolChip("Bold", Icons.Filled.FormatBold) {
                        content += if (content.isEmpty()) "**Bold text** " else " **Bold** "
                    }
                    FormatToolChip("Italic", Icons.Filled.FormatItalic) {
                        content += if (content.isEmpty()) "_Italic text_ " else " _Italic_ "
                    }
                    FormatToolChip("Underline", Icons.Filled.FormatUnderlined) {
                        content += if (content.isEmpty()) "__Underlined__ " else " __Underlined__ "
                    }
                    FormatToolChip("Bullets", Icons.AutoMirrored.Filled.FormatListBulleted) {
                        content += "\n• "
                    }
                    FormatToolChip("Numbered", Icons.Filled.FormatListNumbered) {
                        content += "\n1. "
                    }
                    FormatToolChip("Highlight", Icons.Filled.Highlight) {
                        content += " ==Highlighted== "
                    }
                    FormatToolChip("Checklist", Icons.Filled.CheckBox) {
                        content += "\n[ ] Task item"
                    }
                }

                // Color Accent & PDF Attachment Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val noteColors = listOf(0xFF3B82F6L, 0xFFA855F7L, 0xFFEC4899L, 0xFF14B8A6L, 0xFFF59E0BL)
                    noteColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(if (accentHex == hex) 26.dp else 20.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (accentHex == hex) 2.dp else 1.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { accentHex = hex }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    documents.forEach { doc ->
                        val attached = selectedDocId == doc.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (attached) ElectricBlue else Color.White.copy(alpha = 0.28f))
                                .clickable {
                                    if (attached) {
                                        selectedDocId = null
                                        selectedDocTitle = null
                                    } else {
                                        selectedDocId = doc.id
                                        selectedDocTitle = doc.title
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "📎 ${doc.title.removeSuffix(".pdf")}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (attached) Color.White else glass.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Main Glass Note Title & Body Editor
        LiquidGlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cornerRadius = 26.dp,
            tintColor = Color(accentHex)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (title.isEmpty()) {
                        Text(
                            text = "Note Title...",
                            style = MaterialTheme.typography.headlineMedium,
                            color = glass.textMuted
                        )
                    }
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            color = glass.textPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(ElectricBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input")
                    )
                }

                if (!selectedDocTitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Linked to PDF: $selectedDocTitle ${selectedPage?.let { "• Page $it" } ?: ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricBlue
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (content.isEmpty()) {
                        Text(
                            text = "Write your study notes, formulas, highlights, or checklist items here...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = glass.textMuted
                        )
                    }
                    BasicTextField(
                        value = content,
                        onValueChange = { content = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = glass.textPrimary,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(ElectricBlue),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("note_body_input")
                    )
                }
            }
        }
    }
}

@Composable
private fun FormatToolChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val glass = LocalGlassColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = if (glass.isDark) 0.14f else 0.72f))
            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = glass.textPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = glass.textPrimary
        )
    }
}

// ==================== SECTION 10: PREMIUM GLASS "SELECT PDF" SCREEN ====================

@Composable
fun SelectPdfOverlay(
    documents: List<PdfDocumentEntity>,
    importPreview: ImportPreviewState?,
    onPickFromSystem: (sourceLabel: String) -> Unit,
    onOpenPdf: (PdfDocumentEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val glass = LocalGlassColors.current
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp)
            .testTag("select_pdf_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
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
                Column {
                    Text(
                        text = stringResource(R.string.select_pdf_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.privacy_notice),
                        style = MaterialTheme.typography.bodySmall,
                        color = glass.textSecondary
                    )
                }
            }
        }

        // Imported File Preview Card (shown after selecting a file from system picker)
        importPreview?.let { preview ->
            item {
                val doc = preview.document
                val thumb by produceState<Bitmap?>(initialValue = null, key1 = doc.filePath) {
                    value = PdfEngine.renderThumbnail(context, doc.filePath, 0, 320)
                }

                LiquidGlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 28.dp,
                    tintColor = EmeraldGreen,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = preview.statusMessage,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldGreen
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(74.dp)
                                    .height(98.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (thumb != null) {
                                    Image(
                                        bitmap = thumb!!.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = ElectricBlue)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = glass.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pages: ${doc.pageCount} pages",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = glass.textSecondary
                                )
                                Text(
                                    text = "Size: ${GlassPaperViewModel.formatFileSize(doc.fileSizeBytes)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = glass.textSecondary
                                )
                            }
                        }

                        Button(
                            onClick = { onOpenPdf(doc) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Document Now")
                        }
                    }
                }
            }
        }

        // 4 Source Options: Device | Recent | Downloads | Documents
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Browse Storage Sources",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = glass.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SourceOptionGlassCard(
                        title = "Device",
                        subtitle = "Internal storage",
                        icon = Icons.Filled.PhoneAndroid,
                        accent = ElectricBlue,
                        onClick = { onPickFromSystem("Device") },
                        modifier = Modifier.weight(1f)
                    )
                    SourceOptionGlassCard(
                        title = "Recent",
                        subtitle = "Recently modified",
                        icon = Icons.Filled.History,
                        accent = PrismPurple,
                        onClick = { onPickFromSystem("Recent") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SourceOptionGlassCard(
                        title = "Downloads",
                        subtitle = "Downloaded PDFs",
                        icon = Icons.Filled.Download,
                        accent = CrystalTeal,
                        onClick = { onPickFromSystem("Downloads") },
                        modifier = Modifier.weight(1f)
                    )
                    SourceOptionGlassCard(
                        title = "Documents",
                        subtitle = "SAF & Cloud files",
                        icon = Icons.Filled.FolderOpen,
                        accent = SolarAmber,
                        onClick = { onPickFromSystem("Documents") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick-Open from Ready Library Documents
        item {
            Text(
                text = "Instant Open from Paperflow Library",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = glass.textPrimary
            )
        }

        items(documents, key = { "pick_${it.id}" }) { doc ->
            LiquidGlassPanel(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                tintColor = Color(doc.accentHex),
                onClick = { onOpenPdf(doc) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(doc.accentHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = glass.textPrimary
                        )
                        Text(
                            text = "${doc.pageCount} pages • ${GlassPaperViewModel.formatFileSize(doc.fileSizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary
                        )
                    }
                    Text(
                        text = "Open",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = ElectricBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceOptionGlassCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    LiquidGlassPanel(
        modifier = modifier,
        cornerRadius = 22.dp,
        tintColor = accent,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.22f))
                    .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (glass.isDark) Color.White else accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = glass.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = glass.textSecondary
            )
        }
    }
}

// ==================== BOOKMARKS OVERLAY ====================

@Composable
fun BookmarksOverlay(
    bookmarks: List<BookmarkEntity>,
    documents: List<PdfDocumentEntity>,
    onOpenBookmark: (docId: Long, pageIndex: Int) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val glass = LocalGlassColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp)
            .testTag("bookmarks_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                Column {
                    Text(
                        text = "Saved Bookmarks",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary
                    )
                    Text(
                        text = "Jump directly to your bookmarked PDF pages",
                        style = MaterialTheme.typography.bodySmall,
                        color = glass.textSecondary
                    )
                }
            }
        }

        if (bookmarks.isEmpty()) {
            item {
                LiquidGlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    tintColor = CrystalTeal
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = CrystalTeal,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No bookmarks saved yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "Tap the bookmark icon while reading any PDF to save a page.",
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary
                        )
                    }
                }
            }
        } else {
            items(bookmarks, key = { it.id }) { bm ->
                LiquidGlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp,
                    tintColor = CrystalTeal,
                    onClick = { onOpenBookmark(bm.documentId, bm.pageIndex) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CrystalTeal.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = CrystalTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bm.label,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = glass.textPrimary
                            )
                            Text(
                                text = "${bm.documentTitle} • Page ${bm.pageIndex + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary
                            )
                        }
                        IconButton(onClick = { onDeleteBookmark(bm.id) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete bookmark",
                                tint = glass.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
