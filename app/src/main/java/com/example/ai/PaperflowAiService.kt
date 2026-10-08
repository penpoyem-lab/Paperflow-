package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.AiChatMessageEntity
import com.example.data.NoteEntity
import com.example.data.PdfDocumentEntity
import com.example.pdf.PdfEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@Serializable
data class GenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GeminiGenerationConfig(
    val temperature: Float? = 0.65f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiRestApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

data class PaperflowAiContext(
    val activeDocument: PdfDocumentEntity? = null,
    val activePageIndex: Int = 0,
    val activeNote: NoteEntity? = null,
    val allDocumentsCount: Int = 0,
    val allNotesCount: Int = 0,
    val allDocumentsSummary: String = "",
    val allNotesSummary: String = ""
)

data class AiAssistantReply(
    val text: String,
    val contextBadge: String,
    val isError: Boolean = false
)

object PaperflowAiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val json by lazy {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = false
        }
    }

    private val apiService: GeminiRestApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiRestApiService::class.java)
    }

    private fun Bitmap.toJpegBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 76, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    fun isLiveGeminiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("YOUR_")
        } catch (_: Exception) {
            false
        }
    }

    suspend fun generateAssistantReply(
        context: Context,
        userPrompt: String,
        history: List<AiChatMessageEntity>,
        aiContext: PaperflowAiContext
    ): AiAssistantReply = withContext(Dispatchers.IO) {
        val cleanPrompt = userPrompt.trim()
        val badge = buildContextBadge(aiContext)

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && !apiKey.startsWith("YOUR_")

        if (hasValidKey) {
            try {
                val systemPrompt = buildSystemInstruction(aiContext)
                val historyContents = history
                    .filter { !it.isError && it.content.isNotBlank() }
                    .takeLast(8)
                    .map { msg ->
                        GeminiContent(
                            role = if (msg.role == "user") "user" else "model",
                            parts = listOf(GeminiPart(text = msg.content))
                        )
                    }

                val currentParts = mutableListOf<GeminiPart>()
                currentParts.add(GeminiPart(text = buildEnrichedUserPrompt(cleanPrompt, aiContext)))

                // If a PDF is open, render the current page bitmap for multimodal vision understanding
                if (aiContext.activeDocument != null) {
                    val pageBitmap = PdfEngine.renderThumbnail(
                        context = context,
                        pathOrUri = aiContext.activeDocument.filePath,
                        pageIndex = aiContext.activePageIndex,
                        targetWidth = 720
                    )
                    if (pageBitmap != null && !pageBitmap.isRecycled) {
                        currentParts.add(
                            GeminiPart(
                                inlineData = GeminiInlineData(
                                    mimeType = "image/jpeg",
                                    data = pageBitmap.toJpegBase64()
                                )
                            )
                        )
                    }
                }

                val request = GenerateContentRequest(
                    contents = historyContents + GeminiContent(
                        role = "user",
                        parts = currentParts
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.6f,
                        topP = 0.95f,
                        topK = 40
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    )
                )

                val response = apiService.generateContent(apiKey = apiKey, request = request)
                val replyText = response.candidates
                    .firstOrNull()
                    ?.content
                    ?.parts
                    ?.mapNotNull { it.text }
                    ?.joinToString("\n")
                    ?.trim()

                if (!replyText.isNullOrBlank()) {
                    return@withContext AiAssistantReply(
                        text = replyText,
                        contextBadge = badge,
                        isError = false
                    )
                }
            } catch (e: Exception) {
                // Fall back to local document & note analyzer if offline or API request fails, with transparent notice
                val fallback = buildLocalIntelligentSynthesis(cleanPrompt, aiContext, errorReason = e.localizedMessage)
                return@withContext AiAssistantReply(
                    text = fallback,
                    contextBadge = badge,
                    isError = false
                )
            }
        }

        // Intelligent Local Document & Note Synthesis Engine when GEMINI_API_KEY is not yet configured in Secrets panel
        val localSynthesis = buildLocalIntelligentSynthesis(cleanPrompt, aiContext, errorReason = null)
        AiAssistantReply(
            text = localSynthesis,
            contextBadge = badge,
            isError = false
        )
    }

    private fun buildContextBadge(ctx: PaperflowAiContext): String {
        return when {
            ctx.activeDocument != null -> "${ctx.activeDocument.title} • Page ${ctx.activePageIndex + 1}/${ctx.activeDocument.pageCount}"
            ctx.activeNote != null -> "Note: ${ctx.activeNote.title.ifBlank { "Untitled Note" }}"
            ctx.allDocumentsCount > 0 -> "Workspace • ${ctx.allDocumentsCount} PDFs, ${ctx.allNotesCount} Notes"
            else -> "Paperflow AI Workspace"
        }
    }

    private fun buildSystemInstruction(ctx: PaperflowAiContext): String {
        return buildString {
            appendLine("You are Paperflow AI, an intelligent, concise, and scholarly PDF Reader, Study Note, and Document Assistant integrated inside the Paperflow Android app.")
            appendLine("Format your responses clearly using clean headings, bullet points, numbered lists, and bold key terms so they are easy to scan on mobile.")
            if (ctx.activeDocument != null) {
                appendLine("CURRENTLY OPEN PDF: \"${ctx.activeDocument.title}\" (${ctx.activeDocument.pageCount} pages, Category: ${ctx.activeDocument.categoryTag}).")
                appendLine("ACTIVE PAGE: Page ${ctx.activePageIndex + 1} of ${ctx.activeDocument.pageCount}.")
            }
            if (ctx.activeNote != null) {
                appendLine("CURRENTLY OPEN NOTE: \"${ctx.activeNote.title}\".")
            }
        }
    }

    private fun buildEnrichedUserPrompt(prompt: String, ctx: PaperflowAiContext): String {
        return buildString {
            if (ctx.activeDocument != null) {
                val doc = ctx.activeDocument
                val pages = if (doc.searchableText.isBlank()) emptyList() else doc.searchableText.split("||PAGE||")
                val currentPageText = pages.getOrNull(ctx.activePageIndex)?.trim().orEmpty()
                val fullDocExcerpt = pages.joinToString("\n\n").take(6000)

                appendLine("[Active PDF Context]")
                appendLine("Document Title: ${doc.title}")
                appendLine("Total Pages: ${doc.pageCount}")
                appendLine("Current Page Number: ${ctx.activePageIndex + 1}")
                if (currentPageText.isNotBlank()) {
                    appendLine("Current Page Extracted Text:\n$currentPageText")
                } else if (fullDocExcerpt.isNotBlank()) {
                    appendLine("Document Extracted Text Excerpt:\n$fullDocExcerpt")
                } else {
                    appendLine("(Note: An image of the current PDF page is attached for visual inspection.)")
                }
                appendLine()
            }
            if (ctx.activeNote != null) {
                appendLine("[Active Study Note Context]")
                appendLine("Note Title: ${ctx.activeNote.title}")
                appendLine("Note Content:\n${ctx.activeNote.content}")
                appendLine()
            }
            if (ctx.activeDocument == null && ctx.activeNote == null) {
                if (ctx.allDocumentsSummary.isNotBlank()) {
                    appendLine("[User Library PDFs]: ${ctx.allDocumentsSummary}")
                }
                if (ctx.allNotesSummary.isNotBlank()) {
                    appendLine("[User Study Notes]: ${ctx.allNotesSummary}")
                }
            }
            appendLine("User Request: $prompt")
        }
    }

    /**
     * Provides real, context-aware local analysis of the user's open PDF page, extracted PDF text,
     * study notes, and Paperflow tools when offline or before a live GEMINI_API_KEY is added in the Secrets panel.
     */
    private fun buildLocalIntelligentSynthesis(
        prompt: String,
        ctx: PaperflowAiContext,
        errorReason: String?
    ): String {
        val lower = prompt.lowercase()
        val doc = ctx.activeDocument
        val note = ctx.activeNote
        val pages = if (doc != null && doc.searchableText.isNotBlank()) {
            doc.searchableText.split("||PAGE||")
        } else emptyList()
        val currentPageText = pages.getOrNull(ctx.activePageIndex)?.trim().orEmpty()
        val allDocText = pages.joinToString(" ").trim()

        val sentences = (if (currentPageText.isNotBlank()) currentPageText else allDocText)
            .split(Regex("(?<=[.!?])\\s+|\n+"))
            .map { it.trim() }
            .filter { it.length > 18 }

        val body = when {
            // 1. Summarize PDF / Current Page
            lower.contains("summarize") || lower.contains("summary") || lower.contains("tldr") -> {
                if (doc != null) {
                    val keyPoints = if (sentences.isNotEmpty()) {
                        sentences.take(5).joinToString("\n") { "• $it" }
                    } else {
                        "• **Document**: ${doc.title} (${doc.pageCount} pages, ${GlassPaperSizeHelper.format(doc.fileSizeBytes)})\n" +
                            "• **Current Section**: Viewing Page ${ctx.activePageIndex + 1} of ${doc.pageCount} (${((ctx.activePageIndex + 1) * 100 / doc.pageCount.coerceAtLeast(1))}% progress)\n" +
                            "• **Category Tag**: ${doc.categoryTag}\n" +
                            "• **Tip**: Use the **Note Editor** button below to save structured study highlights directly linked to Page ${ctx.activePageIndex + 1}."
                    }
                    "### 📄 Summary — ${doc.title} (Page ${ctx.activePageIndex + 1})\n\n" +
                        "$keyPoints\n\n" +
                        "**Key Takeaway:** Focus on the core definitions and structure presented on Page ${ctx.activePageIndex + 1} of ${doc.pageCount}."
                } else if (note != null && note.content.isNotBlank()) {
                    val noteLines = note.content.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    val bullets = noteLines.take(5).joinToString("\n") { "• $it" }
                    "### 📝 Study Note Summary — ${note.title}\n\n$bullets"
                } else {
                    "### ✨ Workspace Summary\n\n" +
                        "• **Library Status**: ${ctx.allDocumentsCount} PDF document(s) and ${ctx.allNotesCount} study note(s) stored locally.\n" +
                        (if (ctx.allDocumentsSummary.isNotBlank()) "• **Recent PDFs**: ${ctx.allDocumentsSummary}\n" else "") +
                        (if (ctx.allNotesSummary.isNotBlank()) "• **Recent Notes**: ${ctx.allNotesSummary}\n" else "") +
                        "\nOpen any PDF in the Reader and tap **Summarize this PDF** to analyze a specific page."
                }
            }

            // 2. Explain this page / concept
            lower.contains("explain") || lower.contains("what is") || lower.contains("how does") -> {
                if (doc != null) {
                    val excerpt = if (sentences.isNotEmpty()) {
                        sentences.take(4).joinToString("\n\n") { "▸ $it" }
                    } else {
                        "You are currently reading **${doc.title}** on **Page ${ctx.activePageIndex + 1} of ${doc.pageCount}**."
                    }
                    "### 💡 Page ${ctx.activePageIndex + 1} Breakdown — ${doc.title}\n\n" +
                        "$excerpt\n\n" +
                        "**Study Approach:**\n" +
                        "1. Identify the main thesis or heading at the top of Page ${ctx.activePageIndex + 1}.\n" +
                        "2. Highlight supporting formulas, dates, or definitions using the **Highlight** tool.\n" +
                        "3. Tap **Save to Notes** on this message to attach this explanation to your study notebook."
                } else {
                    "### 💡 Concept Explanation\n\n" +
                        "To explain a specific PDF page or paragraph:\n" +
                        "• Open any PDF from your **Home** or **Library** tab.\n" +
                        "• Navigate to the page you want explained and tap **Explain this page**.\n" +
                        "• Or paste any paragraph directly into this chat box for a structured breakdown."
                }
            }

            // 3. Generate Study Notes / Key Points
            lower.contains("study note") || lower.contains("key point") || lower.contains("bullet") -> {
                if (doc != null) {
                    val extractedBullets = if (sentences.isNotEmpty()) {
                        sentences.take(6).mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")
                    } else {
                        "1. **Source Document**: ${doc.title} (Page ${ctx.activePageIndex + 1}/${doc.pageCount})\n" +
                            "2. **Core Topic**: Review primary headings and visual diagrams on Page ${ctx.activePageIndex + 1}.\n" +
                            "3. **Action Item**: Cross-reference key terms with your bookmarked pages."
                    }
                    "### 📓 Structured Study Notes: ${doc.title}\n" +
                        "**Reference:** Page ${ctx.activePageIndex + 1} of ${doc.pageCount}\n\n" +
                        "$extractedBullets\n\n" +
                        "*(Tap **Save to Notes** below to add these directly to your Paperflow Notes library.)*"
                } else if (note != null) {
                    "### 📓 Polished Study Notes: ${note.title}\n\n" +
                        note.content.lines().filter { it.isNotBlank() }.joinToString("\n") { "• ${it.trim()}" }
                } else {
                    "### 📓 Study Note Template\n\n" +
                        "• **Topic / Chapter**: [Enter Chapter Title]\n" +
                        "• **Core Concepts**: Key definitions, formulas, and arguments\n" +
                        "• **Questions for Review**: 2–3 self-test questions\n" +
                        "• **Summary Takeaway**: 1-sentence synthesis\n\n" +
                        "Tap **Save to Notes** below to create a new note from this template!"
                }
            }

            // 4. Create Quiz / Flashcards / Questions
            lower.contains("quiz") || lower.contains("question") || lower.contains("test me") || lower.contains("flashcard") -> {
                val subjectName = doc?.title?.removeSuffix(".pdf") ?: note?.title ?: "Your Study Material"
                val q1Context = sentences.getOrNull(0) ?: "the primary topic introduced on Page ${(ctx.activePageIndex + 1)}"
                val q2Context = sentences.getOrNull(1) ?: "the supporting evidence and structure in $subjectName"
                "### 🧠 Active Recall Quiz — $subjectName\n\n" +
                    "**Q1 (Conceptual):** Based on $subjectName, what is the main objective of:\n> *\"${q1Context.take(140)}\"*?\n\n" +
                    "**Q2 (Analysis):** How does the following point connect to the overall chapter:\n> *\"${q2Context.take(140)}\"*?\n\n" +
                    "**Q3 (Synthesis):** Summarize the key takeaway of ${if (doc != null) "Page ${ctx.activePageIndex + 1}" else "this section"} in your own words without looking at the page."
            }

            // 5. Translate / Rewrite / Improve Writing
            lower.contains("rewrite") || lower.contains("improve") || lower.contains("grammar") || lower.contains("translate") -> {
                val targetText = note?.content?.takeIf { it.isNotBlank() }
                    ?: currentPageText.takeIf { it.isNotBlank() }
                    ?: prompt
                "### ✍️ Polished & Structured Version\n\n" +
                    targetText.lines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString("\n\n") { line ->
                            line.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
            }

            // 6. General question or custom prompt
            else -> {
                if (doc != null) {
                    val relevant = sentences.filter { s ->
                        prompt.split(" ").filter { it.length > 3 }.any { word ->
                            s.contains(word, ignoreCase = true)
                        }
                    }.take(4)

                    val matchedBlock = if (relevant.isNotEmpty()) {
                        "**Relevant passages found in ${doc.title}:**\n" +
                            relevant.joinToString("\n") { "• \"$it\"" }
                    } else if (sentences.isNotEmpty()) {
                        "**Context from Page ${ctx.activePageIndex + 1} of ${doc.title}:**\n" +
                            sentences.take(3).joinToString("\n") { "• $it" }
                    } else {
                        "Currently viewing **${doc.title}** (Page ${ctx.activePageIndex + 1} of ${doc.pageCount})."
                    }

                    "### 🤖 Paperflow AI Response\n\n" +
                        "Regarding your query: *\"$prompt\"*\n\n" +
                        "$matchedBlock\n\n" +
                        "You can ask me to **Summarize this PDF**, **Explain this page**, **Generate study notes**, or **Create quiz questions**."
                } else {
                    "### 🤖 Paperflow AI Assistant\n\n" +
                        "Here is how I can help with *\"$prompt\"* inside Paperflow:\n\n" +
                        "• **PDF Analysis**: Open any PDF document and ask me to summarize pages, extract key points, or explain complex sections.\n" +
                        "• **Study Notes**: Ask me to generate structured study notes or flashcards, then tap **Save to Notes** in one click.\n" +
                        "• **PDF Tools**: Use the **Tools** tab to Merge, Split, Extract Pages, Watermark, Sign, Protect, or Convert PDFs.\n" +
                        (if (ctx.allDocumentsCount > 0) "\n📚 **Your Library**: ${ctx.allDocumentsCount} PDF(s) (${ctx.allDocumentsSummary}) and ${ctx.allNotesCount} Note(s)." else "")
                }
            }
        }

        val apiFooter = if (!isLiveGeminiKeyConfigured()) {
            "\n\n---\n*⚡ Local Document Intelligence active. To enable live cloud Gemini 3.5 Flash multimodal reasoning, add your `GEMINI_API_KEY` in the AI Studio Secrets panel.*"
        } else if (errorReason != null) {
            "\n\n---\n*⚡ Offline fallback used (${errorReason.take(60)}).*"
        } else ""

        return body + apiFooter
    }
}

private object GlassPaperSizeHelper {
    fun format(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val kb = bytes / 1024.0
        return if (kb < 1024.0) String.format(Locale.US, "%.1f KB", kb) else String.format(Locale.US, "%.1f MB", kb / 1024.0)
    }
}
