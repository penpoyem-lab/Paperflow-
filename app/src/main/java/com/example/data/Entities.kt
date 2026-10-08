package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pdf_documents")
data class PdfDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String, // Internal file path or content URI string
    val fileSizeBytes: Long,
    val pageCount: Int,
    val lastOpenedTimestamp: Long,
    val createdAtTimestamp: Long,
    val isFavorite: Boolean = false,
    val lastReadPage: Int = 0,
    val categoryTag: String = "Study",
    val accentHex: Long = 0xFF3B82F6,
    val searchableText: String = "", // Extracted or generated page text separated by "||PAGE||"
    val isScannedOnly: Boolean = false,
    val passwordProtectionHash: String = "" // SHA-256 salted hash when protected via Paperflow Protect PDF
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val attachedDocumentId: Long? = null,
    val attachedDocumentTitle: String? = null,
    val attachedPageNumber: Int? = null,
    val accentColorHex: Long = 0xFFA855F7,
    val updatedAtTimestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val documentTitle: String,
    val pageIndex: Int, // 0-indexed
    val label: String,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val pageIndex: Int,
    val toolType: String, // HIGHLIGHT, UNDERLINE, STRIKETHROUGH, PEN, MARKER, TEXT_NOTE
    val colorHex: Long,
    val strokeWidth: Float = 6f,
    val pointsSerialized: String = "", // "x1,y1;x2,y2;..." normalized 0f..1f coordinates
    val textNoteContent: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
