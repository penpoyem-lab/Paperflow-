package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.example.pdf.PdfEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class GlassPaperRepository(
    private val context: Context,
    private val dao: GlassPaperDao
) {
    val allDocuments: Flow<List<PdfDocumentEntity>> = dao.getAllDocuments()
    val allNotes: Flow<List<NoteEntity>> = dao.getAllNotes()
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()

    fun getBookmarksForDocument(docId: Long): Flow<List<BookmarkEntity>> =
        dao.getBookmarksForDocument(docId)

    fun getAnnotationsForDocument(docId: Long): Flow<List<AnnotationEntity>> =
        dao.getAnnotationsForDocument(docId)

    suspend fun getDocumentById(id: Long): PdfDocumentEntity? = dao.getDocumentById(id)

    suspend fun removeLegacyDemoFilesIfPresent() = withContext(Dispatchers.IO) {
        val sampleDir = File(context.filesDir, "sample_pdfs")
        if (sampleDir.exists()) {
            sampleDir.deleteRecursively()
        }
        context.deleteDatabase("glasspaper.db")
        dao.deleteLegacySampleDocuments()
    }

    suspend fun importPdfFromUri(uri: Uri, categoryOverride: String = "Imported"): Result<PdfDocumentEntity> =
        withContext(Dispatchers.IO) {
            try {
                // Attempt to persist readable URI permission so the document reference stays accessible across restarts
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // Some providers do not grant persistable permissions; fallback handled below
                }

                var displayName = "Document.pdf"
                var fileSize = 0L
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx >= 0) {
                            displayName = cursor.getString(nameIdx) ?: displayName
                        }
                        if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                            fileSize = cursor.getLong(sizeIdx)
                        }
                    }
                }

                // Verify directly via URI or fallback local file cache if provider requires stream copy
                val directMetadata = PdfEngine.inspectPdf(context, uri.toString())
                val resolvedPath: String
                val resolvedPageCount: Int
                val resolvedSize: Long

                if (directMetadata.isValid) {
                    resolvedPath = uri.toString()
                    resolvedPageCount = directMetadata.pageCount
                    resolvedSize = if (fileSize > 0L) fileSize else directMetadata.fileSizeBytes
                } else {
                    val copiedFile = PdfEngine.copyUriToInternalStorage(context, uri, displayName)
                        ?: return@withContext Result.failure(IllegalStateException("Unable to open this PDF"))
                    val copyMeta = PdfEngine.inspectPdf(context, copiedFile.absolutePath)
                    if (!copyMeta.isValid) {
                        copiedFile.delete()
                        return@withContext Result.failure(
                            IllegalArgumentException(copyMeta.errorMessage ?: "Unable to open this PDF")
                        )
                    }
                    resolvedPath = copiedFile.absolutePath
                    resolvedPageCount = copyMeta.pageCount
                    resolvedSize = if (fileSize > 0L) fileSize else copiedFile.length()
                }

                val palette = listOf(0xFF3B82F6, 0xFF8B5CF6, 0xFFEC4899, 0xFF14B8A6, 0xFFF59E0B, 0xFF22C55E)
                val chosenAccent = palette[(displayName.hashCode() and 0x7FFFFFFF) % palette.size]

                val entity = PdfDocumentEntity(
                    title = if (displayName.endsWith(".pdf", ignoreCase = true)) displayName else "$displayName.pdf",
                    filePath = resolvedPath,
                    fileSizeBytes = resolvedSize,
                    pageCount = resolvedPageCount,
                    lastOpenedTimestamp = System.currentTimeMillis(),
                    createdAtTimestamp = System.currentTimeMillis(),
                    isFavorite = false,
                    lastReadPage = 0,
                    categoryTag = categoryOverride,
                    accentHex = chosenAccent,
                    searchableText = "",
                    isScannedOnly = true
                )
                val newId = dao.insertDocument(entity)
                Result.success(entity.copy(id = newId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun registerGeneratedPdfFile(
        file: File,
        title: String,
        categoryTag: String = "Tools Output",
        accentHex: Long = 0xFF22D3EE,
        searchableText: String = ""
    ): PdfDocumentEntity? = withContext(Dispatchers.IO) {
        val meta = PdfEngine.inspectPdf(context, file.absolutePath)
        if (!meta.isValid) return@withContext null
        val cleanTitle = if (title.endsWith(".pdf", ignoreCase = true)) title else "$title.pdf"
        val entity = PdfDocumentEntity(
            title = cleanTitle,
            filePath = file.absolutePath,
            fileSizeBytes = file.length(),
            pageCount = meta.pageCount,
            lastOpenedTimestamp = System.currentTimeMillis(),
            createdAtTimestamp = System.currentTimeMillis(),
            isFavorite = false,
            lastReadPage = 0,
            categoryTag = categoryTag,
            accentHex = accentHex,
            searchableText = searchableText,
            isScannedOnly = searchableText.isBlank()
        )
        val id = dao.insertDocument(entity)
        entity.copy(id = id)
    }

    suspend fun duplicateDocument(doc: PdfDocumentEntity): PdfDocumentEntity? = withContext(Dispatchers.IO) {
        try {
            val copyTitle = doc.title.removeSuffix(".pdf") + " (Copy).pdf"
            val dstFile = if (doc.filePath.startsWith("content://") || doc.filePath.startsWith("file://")) {
                PdfEngine.copyUriToInternalStorage(context, Uri.parse(doc.filePath), copyTitle)
            } else {
                val src = File(doc.filePath)
                if (!src.exists()) return@withContext null
                val dir = File(context.filesDir, "imported_pdfs")
                if (!dir.exists()) dir.mkdirs()
                val dst = File(dir, "${System.currentTimeMillis()}_${copyTitle.replace(" ", "_")}")
                src.copyTo(dst, overwrite = true)
                dst
            } ?: return@withContext null

            val newDoc = doc.copy(
                id = 0,
                title = copyTitle,
                filePath = dstFile.absolutePath,
                lastOpenedTimestamp = System.currentTimeMillis(),
                createdAtTimestamp = System.currentTimeMillis()
            )
            val id = dao.insertDocument(newDoc)
            newDoc.copy(id = id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateReadingPosition(docId: Long, pageIndex: Int) {
        dao.updateReadingPosition(docId, pageIndex, System.currentTimeMillis())
    }

    suspend fun toggleDocumentFavorite(doc: PdfDocumentEntity) {
        dao.setDocumentFavorite(doc.id, !doc.isFavorite)
    }

    suspend fun renameDocument(docId: Long, newTitle: String) {
        val formatted = if (newTitle.endsWith(".pdf", ignoreCase = true)) newTitle.trim() else "${newTitle.trim()}.pdf"
        dao.renameDocument(docId, formatted)
    }

    suspend fun updateDocumentCategory(doc: PdfDocumentEntity, newCategory: String) {
        dao.updateDocument(doc.copy(categoryTag = newCategory))
    }

    suspend fun deleteDocument(doc: PdfDocumentEntity) = withContext(Dispatchers.IO) {
        if (!doc.filePath.startsWith("content://")) {
            runCatching { File(doc.filePath).delete() }
        }
        dao.deleteDocumentById(doc.id)
    }

    // Notes
    suspend fun saveNote(note: NoteEntity): Long {
        return if (note.id == 0L) {
            dao.insertNote(note.copy(updatedAtTimestamp = System.currentTimeMillis()))
        } else {
            dao.updateNote(note.copy(updatedAtTimestamp = System.currentTimeMillis()))
            note.id
        }
    }

    suspend fun deleteNote(noteId: Long) {
        dao.deleteNoteById(noteId)
    }

    // Bookmarks
    suspend fun toggleBookmark(docId: Long, docTitle: String, pageIndex: Int, existingBookmarks: List<BookmarkEntity>) {
        val existing = existingBookmarks.firstOrNull { it.documentId == docId && it.pageIndex == pageIndex }
        if (existing != null) {
            dao.deleteBookmarkById(existing.id)
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    documentId = docId,
                    documentTitle = docTitle,
                    pageIndex = pageIndex,
                    label = "Page ${pageIndex + 1} Bookmark"
                )
            )
        }
    }

    suspend fun deleteBookmark(bookmarkId: Long) {
        dao.deleteBookmarkById(bookmarkId)
    }

    // Annotations
    suspend fun addAnnotation(annotation: AnnotationEntity): Long {
        return dao.insertAnnotation(annotation)
    }

    suspend fun deleteAnnotation(annotationId: Long) {
        dao.deleteAnnotationById(annotationId)
    }

    suspend fun clearPageAnnotations(docId: Long, pageIndex: Int) {
        dao.clearAnnotationsOnPage(docId, pageIndex)
    }

    // Clear / Reset
    suspend fun clearAllAppData() = withContext(Dispatchers.IO) {
        dao.clearAllAnnotations()
        dao.clearAllBookmarks()
        dao.clearAllNotes()
        dao.clearAllDocuments()
        removeLegacyDemoFilesIfPresent()
    }
}
