package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GlassPaperDao {
    // Documents
    @Query("SELECT * FROM pdf_documents ORDER BY lastOpenedTimestamp DESC")
    fun getAllDocuments(): Flow<List<PdfDocumentEntity>>

    @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): PdfDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: PdfDocumentEntity): Long

    @Update
    suspend fun updateDocument(doc: PdfDocumentEntity)

    @Query("DELETE FROM pdf_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("DELETE FROM pdf_documents WHERE filePath LIKE '%sample_pdfs%'")
    suspend fun deleteLegacySampleDocuments()

    @Query("UPDATE pdf_documents SET lastOpenedTimestamp = :timestamp, lastReadPage = :page WHERE id = :id")
    suspend fun updateReadingPosition(id: Long, page: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pdf_documents SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setDocumentFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE pdf_documents SET title = :newTitle WHERE id = :id")
    suspend fun renameDocument(id: Long, newTitle: String)

    // Notes
    @Query("SELECT * FROM notes ORDER BY updatedAtTimestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY createdAtTimestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE documentId = :docId ORDER BY pageIndex ASC")
    fun getBookmarksForDocument(docId: Long): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE documentId = :docId AND pageIndex = :pageIndex")
    suspend fun deleteBookmarkForPage(docId: Long, pageIndex: Int)

    // Annotations
    @Query("SELECT * FROM annotations WHERE documentId = :docId ORDER BY createdAtTimestamp ASC")
    fun getAnnotationsForDocument(docId: Long): Flow<List<AnnotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity): Long

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: Long)

    @Query("DELETE FROM annotations WHERE documentId = :docId AND pageIndex = :pageIndex")
    suspend fun clearAnnotationsOnPage(docId: Long, pageIndex: Int)

    // AI Chat Messages
    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp ASC")
    fun getAllAiChatMessages(): Flow<List<AiChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiChatMessage(message: AiChatMessageEntity): Long

    @Query("DELETE FROM ai_chat_messages WHERE id = :id")
    suspend fun deleteAiChatMessageById(id: Long)

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clearAllAiChatMessages()

    // Danger Zone / History
    @Query("DELETE FROM pdf_documents")
    suspend fun clearAllDocuments()

    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()

    @Query("DELETE FROM bookmarks")
    suspend fun clearAllBookmarks()

    @Query("DELETE FROM annotations")
    suspend fun clearAllAnnotations()
}

@Database(
    entities = [
        PdfDocumentEntity::class,
        NoteEntity::class,
        BookmarkEntity::class,
        AnnotationEntity::class,
        AiChatMessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GlassPaperDatabase : RoomDatabase() {
    abstract fun dao(): GlassPaperDao
}
