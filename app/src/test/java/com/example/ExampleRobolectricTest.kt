package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.auth.AuthResult
import com.example.auth.PaperflowAuthManager
import com.example.data.GlassPaperDatabase
import com.example.data.GlassPaperRepository
import com.example.data.NoteEntity
import com.example.data.PaperflowGitHubRepoData
import com.example.data.PdfDocumentEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Paperflow", appName)
  }

  @Test
  fun `auth manager validates email and password and hashes securely`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authManager = PaperflowAuthManager(context)

    assertTrue(PaperflowAuthManager.isValidEmail("scholar@paperflow.app"))

    val regResult = authManager.registerAccount(
      fullName = "Alex Scholar",
      email = "alex_${System.currentTimeMillis()}@paperflow.app",
      password = "Paperflow#2026",
      confirmPassword = "Paperflow#2026"
    )
    assertTrue(regResult is AuthResult.VerificationRequired)
    val verifiedReq = regResult as AuthResult.VerificationRequired
    val verifyResult = authManager.verifyEmailCode(verifiedReq.email, verifiedReq.verificationCodeHint)
    assertTrue(verifyResult is AuthResult.Success)
  }

  @Test
  fun `github repository data model exposes complete paperflow tree and commits`() {
    assertEquals("penpoyem-create", PaperflowGitHubRepoData.OWNER_USERNAME)
    assertEquals("Paperflow-pdf-reader-Notes", PaperflowGitHubRepoData.REPO_NAME)
    assertTrue(PaperflowGitHubRepoData.rootFiles.isNotEmpty())
    assertTrue(PaperflowGitHubRepoData.commitHistory.isNotEmpty())
    val flattened = PaperflowGitHubRepoData.flattenAllFiles()
    assertTrue(flattened.any { it.path == "README.md" })
    assertTrue(flattened.any { it.path == "LICENSE" })
    assertTrue(flattened.any { it.path == ".github/workflows/build-apk.yml" })
  }

  @Test
  fun `database stores pdf documents bookmarks annotations and study notes`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, GlassPaperDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = db.dao()
    val repo = GlassPaperRepository(context, dao)

    val docId = dao.insertDocument(
      PdfDocumentEntity(
        title = "Quantum Computation Notes.pdf",
        filePath = "/storage/emulated/0/Documents/Quantum_Computation_Notes.pdf",
        fileSizeBytes = 248_500L,
        pageCount = 12,
        lastOpenedTimestamp = System.currentTimeMillis(),
        createdAtTimestamp = System.currentTimeMillis(),
        categoryTag = "Study",
        accentHex = 0xFF3B82F6
      )
    )
    assertTrue(docId > 0L)

    val docs = repo.allDocuments.first()
    assertEquals(1, docs.size)
    assertEquals("Quantum Computation Notes.pdf", docs.first().title)

    val newNoteId = repo.saveNote(
      NoteEntity(
        title = "Quantum Computation Summary",
        content = "Key takeaways on qubits and gates",
        attachedDocumentId = docId,
        attachedDocumentTitle = docs.first().title,
        attachedPageNumber = 2,
        accentColorHex = 0xFF3B82F6
      )
    )
    assertTrue(newNoteId > 0L)

    val notes = repo.allNotes.first()
    assertTrue(notes.any { it.title == "Quantum Computation Summary" && it.attachedPageNumber == 2 })

    repo.toggleBookmark(docId, docs.first().title, 3, emptyList())
    val bookmarks = repo.allBookmarks.first()
    assertEquals(1, bookmarks.size)
    assertEquals(3, bookmarks.first().pageIndex)

    db.close()
  }
}
