package com.example.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.data.NoteEntity
import com.example.data.PdfDocumentEntity
import com.example.pdf.PdfEngine
import com.example.ui.GlassPaperViewModel
import com.example.ui.LibraryCategory
import com.example.ui.LibrarySortOrder
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

@Composable
fun LibraryScreen(
    viewModel: GlassPaperViewModel,
    documents: List<PdfDocumentEntity>,
    notes: List<NoteEntity>,
    selectedCategory: LibraryCategory,
    sortOrder: LibrarySortOrder,
    searchQuery: String,
    isGridView: Boolean,
    onImportPdf: () -> Unit,
    onScanDocument: () -> Unit,
    onCreateNote: () -> Unit,
    onOpenPdf: (PdfDocumentEntity) -> Unit,
    onOpenNote: (NoteEntity) -> Unit
) {
    val glass = LocalGlassColors.current
    val context = LocalContext.current

    var sortMenuExpanded by remember { mutableStateOf(false) }
    var docToRename by remember { mutableStateOf<PdfDocumentEntity?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var docToMove by remember { mutableStateOf<PdfDocumentEntity?>(null) }
    var docToDelete by remember { mutableStateOf<PdfDocumentEntity?>(null) }

    val filteredDocuments = remember(documents, selectedCategory, sortOrder, searchQuery) {
        val base = when (selectedCategory) {
            LibraryCategory.ALL, LibraryCategory.PDFS -> documents
            LibraryCategory.RECENT -> documents.sortedByDescending { it.lastOpenedTimestamp }.take(10)
            LibraryCategory.FAVORITES -> documents.filter { it.isFavorite }
            LibraryCategory.NOTES -> emptyList()
        }
        val searched = if (searchQuery.isBlank()) base else {
            base.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.categoryTag.contains(searchQuery, ignoreCase = true) ||
                    it.searchableText.contains(searchQuery, ignoreCase = true)
            }
        }
        when (sortOrder) {
            LibrarySortOrder.RECENT -> searched.sortedByDescending { it.lastOpenedTimestamp }
            LibrarySortOrder.NAME_ASC -> searched.sortedBy { it.title.lowercase() }
            LibrarySortOrder.SIZE_DESC -> searched.sortedByDescending { it.fileSizeBytes }
            LibrarySortOrder.PAGES_DESC -> searched.sortedByDescending { it.pageCount }
        }
    }

    val filteredNotes = remember(notes, selectedCategory, searchQuery) {
        val base = when (selectedCategory) {
            LibraryCategory.ALL, LibraryCategory.NOTES -> notes
            LibraryCategory.FAVORITES -> notes.filter { it.isFavorite }
            else -> emptyList()
        }
        if (searchQuery.isBlank()) base else {
            base.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("library_screen_list"),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 12.dp,
                bottom = 168.dp // Space for "+ Import" FAB and floating glass bottom bar
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header: "Library" & "All your documents in one place" + Grid/List & Sort
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.library_title),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = glass.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.library_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = glass.textSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassCircularIconButton(
                            icon = if (isGridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = "Toggle Grid or List View",
                            onClick = { viewModel.setLibraryGridView(!isGridView) },
                            modifier = Modifier.testTag("library_view_toggle")
                        )

                        Box {
                            GlassCircularIconButton(
                                icon = Icons.Filled.FilterList,
                                contentDescription = "Sort documents",
                                onClick = { sortMenuExpanded = true },
                                modifier = Modifier.testTag("library_sort_button")
                            )
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                LibrarySortOrder.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.label,
                                                fontWeight = if (sortOrder == option) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            sortMenuExpanded = false
                                            viewModel.setLibrarySortOrder(option)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Top Search Field: "Search documents..."
            item {
                LiquidGlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    tintColor = ElectricBlue,
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
                            contentDescription = "Search",
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search_documents_hint),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = glass.textMuted
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setLibrarySearchQuery(it) },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = glass.textPrimary),
                                cursorBrush = SolidColor(ElectricBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("library_search_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setLibrarySearchQuery("") },
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

            // 3. Main Actions Row: "Import PDF", "Scan Document", "Create Note"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LibraryActionPill(
                        title = stringResource(R.string.btn_import_pdf),
                        icon = Icons.Filled.UploadFile,
                        accentColor = ElectricBlue,
                        onClick = onImportPdf,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_action_import_pdf")
                    )
                    LibraryActionPill(
                        title = stringResource(R.string.btn_scan_document),
                        icon = Icons.Filled.DocumentScanner,
                        accentColor = CrystalTeal,
                        onClick = onScanDocument,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_action_scan")
                    )
                    LibraryActionPill(
                        title = stringResource(R.string.btn_create_note),
                        icon = Icons.Filled.EditNote,
                        accentColor = IridescentPink,
                        onClick = onCreateNote,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("library_action_create_note")
                    )
                }
            }

            // 4. Categories Pills: ALL | PDFs | NOTES | RECENT | FAVORITES
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        LibraryCategory.ALL to "ALL",
                        LibraryCategory.PDFS to "PDFs",
                        LibraryCategory.NOTES to "NOTES",
                        LibraryCategory.RECENT to "RECENT",
                        LibraryCategory.FAVORITES to "FAVORITES"
                    )
                    categories.forEach { (cat, label) ->
                        val active = selectedCategory == cat
                        val pillShape = RoundedCornerShape(50)
                        Box(
                            modifier = Modifier
                                .clip(pillShape)
                                .background(
                                    brush = if (active) {
                                        Brush.horizontalGradient(listOf(ElectricBlue, PrismViolet))
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.White.copy(alpha = if (glass.isDark) 0.14f else 0.65f),
                                                Color.White.copy(alpha = if (glass.isDark) 0.08f else 0.45f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (active) Color.White else Color.White.copy(alpha = 0.55f),
                                    shape = pillShape
                                )
                                .testTag("library_cat_${cat.name.lowercase()}")
                                .clickable { viewModel.setLibraryCategory(cat) }
                                .padding(horizontal = 18.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (active) Color.White else glass.textPrimary
                            )
                        }
                    }
                }
            }

            // 5. PDF Documents Section
            if (selectedCategory != LibraryCategory.NOTES) {
                if (filteredDocuments.isEmpty() && filteredNotes.isEmpty()) {
                    item {
                        EmptyLibraryGlassCard(onImportPdf = onImportPdf)
                    }
                } else if (isGridView) {
                    val chunkedDocs = filteredDocuments.chunked(2)
                    items(chunkedDocs) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { doc ->
                                Box(modifier = Modifier.weight(1f)) {
                                    LibraryPdfGridCard(
                                        doc = doc,
                                        onClick = { onOpenPdf(doc) },
                                        onToggleFavorite = { viewModel.toggleFavorite(doc) },
                                        onRename = {
                                            docToRename = doc
                                            renameInput = doc.title.removeSuffix(".pdf")
                                        },
                                        onMove = { docToMove = doc },
                                        onDuplicate = { viewModel.duplicateDocument(doc) },
                                        onShare = { viewModel.shareDocument(context, doc) },
                                        onDelete = { docToDelete = doc }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(filteredDocuments, key = { "doc_${it.id}" }) { doc ->
                        LibraryPdfListCard(
                            doc = doc,
                            onClick = { onOpenPdf(doc) },
                            onToggleFavorite = { viewModel.toggleFavorite(doc) },
                            onRename = {
                                docToRename = doc
                                renameInput = doc.title.removeSuffix(".pdf")
                            },
                            onMove = { docToMove = doc },
                            onDuplicate = { viewModel.duplicateDocument(doc) },
                            onShare = { viewModel.shareDocument(context, doc) },
                            onDelete = { docToDelete = doc }
                        )
                    }
                }
            }

            // 6. Notes Section (shown in ALL, NOTES, or FAVORITES)
            if (selectedCategory == LibraryCategory.ALL ||
                selectedCategory == LibraryCategory.NOTES ||
                selectedCategory == LibraryCategory.FAVORITES
            ) {
                if (selectedCategory == LibraryCategory.NOTES && filteredNotes.isEmpty()) {
                    item {
                        LiquidGlassPanel(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 26.dp,
                            tintColor = PrismPurple
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EditNote,
                                    contentDescription = null,
                                    tint = PrismPurple,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.empty_notes_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = glass.textPrimary
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(onClick = onCreateNote) {
                                    Text(stringResource(R.string.btn_create_note))
                                }
                            }
                        }
                    }
                } else if (filteredNotes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Study Notes (${filteredNotes.size})",
                            style = MaterialTheme.typography.titleLarge,
                            color = glass.textPrimary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(filteredNotes, key = { "note_${it.id}" }) { note ->
                        LibraryNoteCard(
                            note = note,
                            onClick = { onOpenNote(note) },
                            onDelete = { viewModel.deleteNote(note.id) }
                        )
                    }
                }
            }
        }

        // Floating "+ Import" Glass Action Button in Library (above the bottom capsule bar)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 104.dp)
        ) {
            val fabShape = RoundedCornerShape(50)
            Row(
                modifier = Modifier
                    .shadow(16.dp, fabShape, ambientColor = ElectricBlue, spotColor = PrismPurple)
                    .clip(fabShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ElectricBlue, PrismViolet, IridescentPink)
                        )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.9f), fabShape)
                    .testTag("library_fab_import")
                    .clickable(onClick = onImportPdf)
                    .padding(horizontal = 20.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Import PDF",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Import",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }

    // Rename Dialog
    docToRename?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToRename = null },
            title = { Text("Rename Document") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("Document name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameDocument(doc.id, renameInput)
                        docToRename = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { docToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Move / Organize Category Dialog
    docToMove?.let { doc ->
        val folders = listOf("Work", "Study", "Contracts", "Receipts", "Books", "Research", "Personal")
        AlertDialog(
            onDismissRequest = { docToMove = null },
            title = { Text("Move / Organize Document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Choose a collection folder for ${doc.title}:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    folders.forEach { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.moveDocumentCategory(doc, folder)
                                    docToMove = null
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(folder, fontWeight = FontWeight.SemiBold)
                            if (doc.categoryTag == folder) {
                                Text("Current", color = ElectricBlue, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { docToMove = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text("Delete Document?") },
            text = { Text("Are you sure you want to remove \"${doc.title}\" from your Paperflow library?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDocument(doc)
                        docToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LibraryActionPill(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    LiquidGlassPanel(
        modifier = modifier,
        cornerRadius = 20.dp,
        tintColor = accentColor,
        shadowElevation = 8.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accentColor.copy(alpha = 0.22f))
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (glass.isDark) Color.White else accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = glass.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PdfThumbnailBox(
    doc: PdfDocumentEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = Color(doc.accentHex)
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = doc.filePath) {
        value = PdfEngine.renderThumbnail(context, doc.filePath, pageIndex = 0, targetWidth = 280)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "${doc.title} preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.PictureAsPdf,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(30.dp)
            )
        }

        // Small corner PDF badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accentColor)
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${doc.pageCount}p",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}

@Composable
private fun LibraryPdfListCard(
    doc: PdfDocumentEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val glass = LocalGlassColors.current
    val accentColor = Color(doc.accentHex)
    var menuExpanded by remember { mutableStateOf(false) }

    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        tintColor = accentColor,
        shadowElevation = 10.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PdfThumbnailBox(
                doc = doc,
                modifier = Modifier
                    .width(64.dp)
                    .height(84.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = doc.categoryTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (glass.isDark) Color.White else accentColor
                        )
                    }
                    if (doc.lastReadPage > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• Page ${doc.lastReadPage + 1}/${doc.pageCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = glass.textSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = glass.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${doc.pageCount} pages  •  ${GlassPaperViewModel.formatFileSize(doc.fileSizeBytes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = glass.textSecondary
                )
                Text(
                    text = "Opened ${GlassPaperViewModel.formatRelativeTime(doc.lastOpenedTimestamp)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = glass.textMuted
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (doc.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (doc.isFavorite) IridescentPink else glass.textSecondary
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Overflow menu",
                        tint = glass.textSecondary
                    )
                }
                DocumentOverflowMenu(
                    expanded = menuExpanded,
                    onDismiss = { menuExpanded = false },
                    onOpen = onClick,
                    onRename = onRename,
                    onMove = onMove,
                    onDuplicate = onDuplicate,
                    onShare = onShare,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun LibraryPdfGridCard(
    doc: PdfDocumentEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val glass = LocalGlassColors.current
    val accentColor = Color(doc.accentHex)
    var menuExpanded by remember { mutableStateOf(false) }

    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        tintColor = accentColor,
        shadowElevation = 10.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                PdfThumbnailBox(
                    doc = doc,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                )
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        imageVector = if (doc.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (doc.isFavorite) IridescentPink else Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = glass.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${doc.pageCount} pages • ${GlassPaperViewModel.formatFileSize(doc.fileSizeBytes)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = glass.textSecondary,
                        maxLines = 1
                    )
                }
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More",
                            tint = glass.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DocumentOverflowMenu(
                        expanded = menuExpanded,
                        onDismiss = { menuExpanded = false },
                        onOpen = onClick,
                        onRename = onRename,
                        onMove = onMove,
                        onDuplicate = onDuplicate,
                        onShare = onShare,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onMove: () -> Unit,
    onDuplicate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("Open") },
            onClick = {
                onDismiss()
                onOpen()
            }
        )
        DropdownMenuItem(
            text = { Text("Rename") },
            onClick = {
                onDismiss()
                onRename()
            }
        )
        DropdownMenuItem(
            text = { Text("Move / Organize") },
            onClick = {
                onDismiss()
                onMove()
            }
        )
        DropdownMenuItem(
            text = { Text("Duplicate") },
            onClick = {
                onDismiss()
                onDuplicate()
            }
        )
        DropdownMenuItem(
            text = { Text("Share") },
            onClick = {
                onDismiss()
                onShare()
            }
        )
        DropdownMenuItem(
            text = { Text("Delete", color = LiquidMagenta) },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}

@Composable
private fun LibraryNoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val glass = LocalGlassColors.current
    val accent = Color(note.accentColorHex)

    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        tintColor = accent,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.24f))
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.EditNote,
                    contentDescription = null,
                    tint = if (glass.isDark) Color.White else accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = glass.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!note.attachedDocumentTitle.isNullOrBlank()) {
                    Text(
                        text = "Attached to ${note.attachedDocumentTitle} ${note.attachedPageNumber?.let { "(p. $it)" } ?: ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = note.content.replace("\n", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = glass.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Delete note",
                    tint = glass.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyLibraryGlassCard(onImportPdf: () -> Unit) {
    val glass = LocalGlassColors.current
    LiquidGlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        tintColor = ElectricBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.PictureAsPdf,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.empty_library_title),
                style = MaterialTheme.typography.titleLarge,
                color = glass.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.empty_library_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = glass.textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onImportPdf) {
                Text(stringResource(R.string.btn_import_pdf))
            }
        }
    }
}
