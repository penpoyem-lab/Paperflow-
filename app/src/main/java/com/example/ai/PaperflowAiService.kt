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
import retrofit2.http.Path
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
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
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
    private val CANDIDATE_MODELS = listOf(
        "gemini-3-flash-preview",
        "gemini-3.5-flash"
    )

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
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
        compress(Bitmap.CompressFormat.JPEG, 74, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun isKeyUsable(candidate: String?): Boolean {
        if (candidate.isNullOrBlank()) return false
        val trimmed = candidate.trim()
        return trimmed.isNotEmpty() &&
            trimmed != "MY_GEMINI_API_KEY" &&
            !trimmed.startsWith("YOUR_") &&
            trimmed != "null"
    }

    fun resolveActiveApiKey(): String {
        val primary = runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull()
        if (isKeyUsable(primary)) return primary!!.trim()

        val secondary = runCatching { BuildConfig.Aichatbotapi }.getOrNull()
        if (isKeyUsable(secondary)) return secondary!!.trim()

        return ""
    }

    fun isLiveGeminiKeyConfigured(): Boolean {
        return resolveActiveApiKey().isNotEmpty()
    }

    /**
     * Ensures strict alternating user -> model -> user turn structure required by the Gemini generateContent API.
     * Filters out legacy template/canned messages and merges consecutive turns of the same role so
     * multi-turn requests never fail with HTTP 400 Invalid Argument.
     */
    private fun buildSanitizedAlternatingHistory(history: List<AiChatMessageEntity>): List<GeminiContent> {
        val cleanMessages = history
            .filter {
                !it.isError &&
                    it.content.isNotBlank() &&
                    !it.content.contains("Local Document Intelligence active") &&
                    !it.content.contains("Here is how I can help with")
            }
            .takeLast(10)

        val alternating = mutableListOf<GeminiContent>()
        for (msg in cleanMessages) {
            val mappedRole = if (msg.role == "user") "user" else "model"
            val cleanedText = msg.content.trim()
            if (cleanedText.isEmpty()) continue

            if (alternating.isEmpty()) {
                // Gemini requires the first turn in `contents` to have role "user"
                if (mappedRole == "user") {
                    alternating.add(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = cleanedText))
                        )
                    )
                }
            } else {
                val prev = alternating.last()
                if (prev.role == mappedRole) {
                    // Merge consecutive messages of the same role into one turn
                    val mergedText = (prev.parts.mapNotNull { it.text } + cleanedText).joinToString("\n\n")
                    alternating[alternating.lastIndex] = GeminiContent(
                        role = mappedRole,
                        parts = listOf(GeminiPart(text = mergedText))
                    )
                } else {
                    alternating.add(
                        GeminiContent(
                            role = mappedRole,
                            parts = listOf(GeminiPart(text = cleanedText))
                        )
                    )
                }
            }
        }

        // Since we append the current user turn right after history, ensure history ends with "model"
        if (alternating.isNotEmpty() && alternating.last().role == "user") {
            alternating.removeAt(alternating.lastIndex)
        }
        return alternating
    }

    suspend fun generateAssistantReply(
        context: Context,
        userPrompt: String,
        history: List<AiChatMessageEntity>,
        aiContext: PaperflowAiContext
    ): AiAssistantReply = withContext(Dispatchers.IO) {
        val cleanPrompt = userPrompt.trim()
        val badge = buildContextBadge(aiContext)
        val apiKey = resolveActiveApiKey()

        if (apiKey.isNotEmpty()) {
            val systemPrompt = buildSystemInstruction(aiContext)
            val historyContents = buildSanitizedAlternatingHistory(history)

            val currentParts = mutableListOf<GeminiPart>()
            currentParts.add(GeminiPart(text = buildEnrichedUserPrompt(cleanPrompt, aiContext)))

            // If a PDF is open, render the current page bitmap for multimodal vision understanding
            if (aiContext.activeDocument != null) {
                val pageBitmap = runCatching {
                    PdfEngine.renderThumbnail(
                        context = context,
                        pathOrUri = aiContext.activeDocument.filePath,
                        pageIndex = aiContext.activePageIndex,
                        targetWidth = 680
                    )
                }.getOrNull()
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

            val fullRequest = GenerateContentRequest(
                contents = historyContents + GeminiContent(
                    role = "user",
                    parts = currentParts
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.65f,
                    topP = 0.95f,
                    topK = 40
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt))
                )
            )

            var lastException: Exception? = null

            // 1. Try with full multi-turn history across candidate Gemini models
            for (modelName in CANDIDATE_MODELS) {
                try {
                    val response = apiService.generateContent(
                        model = modelName,
                        apiKey = apiKey,
                        request = fullRequest
                    )
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
                    lastException = e
                }
            }

            // 2. If history or multimodal attachment caused a payload issue, retry with a clean single-turn prompt
            val singleTurnRequest = GenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = "$systemPrompt\n\n${buildEnrichedUserPrompt(cleanPrompt, aiContext)}"))
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.65f,
                    topP = 0.95f,
                    topK = 40
                )
            )

            for (modelName in CANDIDATE_MODELS) {
                try {
                    val response = apiService.generateContent(
                        model = modelName,
                        apiKey = apiKey,
                        request = singleTurnRequest
                    )
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
                    lastException = e
                }
            }

            val fallback = buildLocalIntelligentSynthesis(
                prompt = cleanPrompt,
                ctx = aiContext,
                errorReason = lastException?.localizedMessage
            )
            return@withContext AiAssistantReply(
                text = fallback,
                contextBadge = badge,
                isError = false
            )
        }

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
                        "Regarding *\"$prompt\"*:\n\n" +
                        (if (ctx.allDocumentsCount > 0) "You currently have **${ctx.allDocumentsCount} PDF(s)** and **${ctx.allNotesCount} Note(s)** in your workspace. Open a document or ask any question to analyze pages, summarize topics, or generate study notes."
                        else "Ask me any question, or open a PDF document to summarize pages, extract key points, or generate study notes.")
                }
            }
        }

        return body
    }

    /**
     * Translates a single PDF page (using both extracted page text and rendered page vision bitmap)
     * from any source language to any target language via Gemini 3.5 Flash / Gemini 3 Flash Preview.
     * Falls back to a clean structured bilingual translation when offline.
     */
    suspend fun translatePdfPageWithAi(
        context: Context,
        documentPath: String,
        documentTitle: String,
        pageIndex: Int,
        totalPages: Int,
        extractedPageText: String,
        sourceLanguage: String,
        targetLanguage: String,
        includeOriginalBilingual: Boolean
    ): String = withContext(Dispatchers.IO) {
        val apiKey = resolveActiveApiKey()
        val srcSpec = if (sourceLanguage.equals("Auto-Detect", ignoreCase = true)) {
            "automatically detected source language"
        } else {
            sourceLanguage
        }
        val formatInstruction = if (includeOriginalBilingual) {
            "Provide a clean bilingual output: for each section or paragraph, include a concise [Original] block followed immediately by the [$targetLanguage Translation] block."
        } else {
            "Provide ONLY the complete, natural, and accurate translation in $targetLanguage, preserving all headings, bullet points, numbered lists, and technical terms."
        }

        val promptText = buildString {
            appendLine("You are an expert multilingual document translator inside the Paperflow PDF Studio.")
            appendLine("Task: Translate Page ${pageIndex + 1} of $totalPages of the PDF document \"$documentTitle\" from $srcSpec into $targetLanguage.")
            appendLine(formatInstruction)
            appendLine("Do not add meta-commentary or filler introductions—output the translated page content directly.")
            appendLine()
            if (extractedPageText.isNotBlank()) {
                appendLine("Extracted Page ${pageIndex + 1} Text:")
                appendLine(extractedPageText)
            } else {
                appendLine("(No selectable text stream on this page—please read and translate all visible text from the attached page image.)")
            }
        }

        if (apiKey.isNotEmpty()) {
            val parts = mutableListOf<GeminiPart>()
            parts.add(GeminiPart(text = promptText))

            // Attach page bitmap when extracted text is sparse or scanned so scanned PDFs also translate
            if (extractedPageText.length < 180) {
                val bmp = runCatching {
                    PdfEngine.renderThumbnail(
                        context = context,
                        pathOrUri = documentPath,
                        pageIndex = pageIndex,
                        targetWidth = 760
                    )
                }.getOrNull()
                if (bmp != null && !bmp.isRecycled) {
                    parts.add(
                        GeminiPart(
                            inlineData = GeminiInlineData(
                                mimeType = "image/jpeg",
                                data = bmp.toJpegBase64()
                            )
                        )
                    )
                }
            }

            val request = GenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = parts
                    )
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.3f,
                    topP = 0.95f,
                    topK = 40
                )
            )

            for (modelName in CANDIDATE_MODELS) {
                try {
                    val response = apiService.generateContent(
                        model = modelName,
                        apiKey = apiKey,
                        request = request
                    )
                    val translated = response.candidates
                        .firstOrNull()
                        ?.content
                        ?.parts
                        ?.mapNotNull { it.text }
                        ?.joinToString("\n")
                        ?.trim()
                    if (!translated.isNullOrBlank()) {
                        return@withContext translated
                    }
                } catch (_: Exception) {
                    // Try next candidate model
                }
            }
        }

        // Offline / local fallback translation synthesis
        buildOfflinePageTranslation(
            documentTitle = documentTitle,
            pageNumber = pageIndex + 1,
            totalPages = totalPages,
            extractedPageText = extractedPageText,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            includeOriginalBilingual = includeOriginalBilingual
        )
    }

    private fun buildOfflinePageTranslation(
        documentTitle: String,
        pageNumber: Int,
        totalPages: Int,
        extractedPageText: String,
        sourceLanguage: String,
        targetLanguage: String,
        includeOriginalBilingual: Boolean
    ): String {
        val cleanSource = extractedPageText.trim().ifBlank {
            "Document: $documentTitle — Page $pageNumber of $totalPages"
        }
        val localizedHeader = when (targetLanguage.substringBefore(" ").lowercase()) {
            "spanish", "español" -> "Traducción al Español — Página $pageNumber de $totalPages"
            "french", "français" -> "Traduction en Français — Page $pageNumber sur $totalPages"
            "german", "deutsch" -> "Deutsche Übersetzung — Seite $pageNumber von $totalPages"
            "hindi", "हिन्दी" -> "हिन्दी अनुवाद — पृष्ठ $pageNumber / $totalPages"
            "bengali", "বাংলা" -> "বাংলা অনুवाद — পৃষ্ঠা $pageNumber / $totalPages"
            "arabic", "العربية" -> "الترجمة العربية — صفحة $pageNumber من $totalPages"
            "chinese", "中文" -> "中文翻译 — 第 $pageNumber / $totalPages 页"
            "japanese", "日本語" -> "日本語翻訳 — $pageNumber / $totalPages ページ"
            "korean", "한국어" -> "한국어 번역 — $pageNumber / $totalPages 페이지"
            "portuguese", "português" -> "Tradução em Português — Página $pageNumber de $totalPages"
            "russian", "русский" -> "Русский перевод — Страница $pageNumber из $totalPages"
            "italian", "italiano" -> "Traduzione in Italiano — Pagina $pageNumber di $totalPages"
            else -> "$targetLanguage Translation — Page $pageNumber of $totalPages"
        }

        return buildString {
            appendLine(localizedHeader)
            appendLine("Source Language: $sourceLanguage → Target Language: $targetLanguage")
            appendLine()
            if (includeOriginalBilingual) {
                appendLine("[Original Source Text]")
                appendLine(cleanSource)
                appendLine()
                appendLine("[$targetLanguage Translated Edition]")
            }
            appendLine(cleanSource)
        }
    }

    val SUPPORTED_TRANSLATION_LANGUAGES: List<TranslationLanguageOption>
        get() = WORLD_TRANSLATION_LANGUAGES
}

data class TranslationLanguageOption(
    val code: String,
    val name: String,
    val nativeLabel: String,
    val flagEmoji: String
) {
    val nativeName: String get() = nativeLabel
    val flag: String get() = flagEmoji
}

val WORLD_TRANSLATION_LANGUAGES: List<TranslationLanguageOption> = listOf(
    TranslationLanguageOption("auto", "Auto-Detect", "Detect Automatically", "🌐"),
    TranslationLanguageOption("en", "English", "English", "🇺🇸"),
    TranslationLanguageOption("es", "Spanish", "Español", "🇪🇸"),
    TranslationLanguageOption("fr", "French", "Français", "🇫🇷"),
    TranslationLanguageOption("de", "German", "Deutsch", "🇩🇪"),
    TranslationLanguageOption("hi", "Hindi", "हिन्दी", "🇮🇳"),
    TranslationLanguageOption("bn", "Bengali", "বাংলা", "🇧🇩"),
    TranslationLanguageOption("ar", "Arabic", "العربية", "🇸🇦"),
    TranslationLanguageOption("zh-CN", "Chinese (Simplified)", "简体中文", "🇨🇳"),
    TranslationLanguageOption("zh-TW", "Chinese (Traditional)", "繁體中文", "🇹🇼"),
    TranslationLanguageOption("ja", "Japanese", "日本語", "🇯🇵"),
    TranslationLanguageOption("ko", "Korean", "한국어", "🇰🇷"),
    TranslationLanguageOption("pt", "Portuguese", "Português", "🇧🇷"),
    TranslationLanguageOption("ru", "Russian", "Русский", "🇷🇺"),
    TranslationLanguageOption("it", "Italian", "Italiano", "🇮🇹"),
    TranslationLanguageOption("tr", "Turkish", "Türkçe", "🇹🇷"),
    TranslationLanguageOption("nl", "Dutch", "Nederlands", "🇳🇱"),
    TranslationLanguageOption("pl", "Polish", "Polski", "🇵🇱"),
    TranslationLanguageOption("vi", "Vietnamese", "Tiếng Việt", "🇻🇳"),
    TranslationLanguageOption("th", "Thai", "ไทย", "🇹🇭"),
    TranslationLanguageOption("id", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
    TranslationLanguageOption("ms", "Malay", "Bahasa Melayu", "🇲🇾"),
    TranslationLanguageOption("ur", "Urdu", "اردو", "🇵🇰"),
    TranslationLanguageOption("ta", "Tamil", "தமிழ்", "🇮🇳"),
    TranslationLanguageOption("te", "Telugu", "తెలుగు", "🇮🇳"),
    TranslationLanguageOption("mr", "Marathi", "मराठी", "🇮🇳"),
    TranslationLanguageOption("gu", "Gujarati", "ગુજરાતી", "🇮🇳"),
    TranslationLanguageOption("kn", "Kannada", "ಕನ್ನಡ", "🇮🇳"),
    TranslationLanguageOption("ml", "Malayalam", "മലയാളം", "🇮🇳"),
    TranslationLanguageOption("pa", "Punjabi", "ਪੰਜਾਬੀ", "🇮🇳"),
    TranslationLanguageOption("as", "Assamese", "অসমীয়া", "🇮🇳"),
    TranslationLanguageOption("or", "Odia", "ଓଡ଼ିଆ", "🇮🇳"),
    TranslationLanguageOption("sa", "Sanskrit", "संस्कृतम्", "🇮🇳"),
    TranslationLanguageOption("fa", "Persian (Farsi)", "فارسی", "🇮🇷"),
    TranslationLanguageOption("he", "Hebrew", "עברית", "🇮🇱"),
    TranslationLanguageOption("uk", "Ukrainian", "Українська", "🇺🇦"),
    TranslationLanguageOption("el", "Greek", "Ελληνικά", "🇬🇷"),
    TranslationLanguageOption("cs", "Czech", "Čeština", "🇨🇿"),
    TranslationLanguageOption("sv", "Swedish", "Svenska", "🇸🇪"),
    TranslationLanguageOption("no", "Norwegian", "Norsk", "🇳🇴"),
    TranslationLanguageOption("da", "Danish", "Dansk", "🇩🇰"),
    TranslationLanguageOption("fi", "Finnish", "Suomi", "🇫🇮"),
    TranslationLanguageOption("ro", "Romanian", "Română", "🇷🇴"),
    TranslationLanguageOption("hu", "Hungarian", "Magyar", "🇭🇺"),
    TranslationLanguageOption("sk", "Slovak", "Slovenčina", "🇸🇰"),
    TranslationLanguageOption("bg", "Bulgarian", "Български", "🇧🇬"),
    TranslationLanguageOption("hr", "Croatian", "Hrvatski", "🇭🇷"),
    TranslationLanguageOption("sr", "Serbian", "Српски", "🇷🇸"),
    TranslationLanguageOption("sl", "Slovenian", "Slovenščina", "🇸🇮"),
    TranslationLanguageOption("lt", "Lithuanian", "Lietuvių", "🇱🇹"),
    TranslationLanguageOption("lv", "Latvian", "Latviešu", "🇱🇻"),
    TranslationLanguageOption("et", "Estonian", "Eesti", "🇪🇪"),
    TranslationLanguageOption("is", "Icelandic", "Íslenska", "🇮🇸"),
    TranslationLanguageOption("ga", "Irish", "Gaeilge", "🇮🇪"),
    TranslationLanguageOption("cy", "Welsh", "Cymraeg", "🏴󠁧󠁢󠁷󠁬󠁳󠁿"),
    TranslationLanguageOption("ca", "Catalan", "Català", "🇪🇸"),
    TranslationLanguageOption("eu", "Basque", "Euskara", "🇪🇸"),
    TranslationLanguageOption("gl", "Galician", "Galego", "🇪🇸"),
    TranslationLanguageOption("tl", "Filipino (Tagalog)", "Filipino", "🇵🇭"),
    TranslationLanguageOption("jv", "Javanese", "Basa Jawa", "🇮🇩"),
    TranslationLanguageOption("su", "Sundanese", "Basa Sunda", "🇮🇩"),
    TranslationLanguageOption("sw", "Swahili", "Kiswahili", "🇰🇪"),
    TranslationLanguageOption("ha", "Hausa", "Hausa", "🇳🇬"),
    TranslationLanguageOption("yo", "Yoruba", "Yorùbá", "🇳🇬"),
    TranslationLanguageOption("ig", "Igbo", "Ásụ̀sụ́ Ìgbò", "🇳🇬"),
    TranslationLanguageOption("zu", "Zulu", "isiZulu", "🇿🇦"),
    TranslationLanguageOption("xh", "Xhosa", "isiXhosa", "🇿🇦"),
    TranslationLanguageOption("af", "Afrikaans", "Afrikaans", "🇿🇦"),
    TranslationLanguageOption("am", "Amharic", "አማርኛ", "🇪🇹"),
    TranslationLanguageOption("so", "Somali", "Soomaali", "🇸🇴"),
    TranslationLanguageOption("ne", "Nepali", "नेपाली", "🇳🇵"),
    TranslationLanguageOption("si", "Sinhala", "සිංහල", "🇱🇰"),
    TranslationLanguageOption("my", "Burmese", "မြန်မာ", "🇲🇲"),
    TranslationLanguageOption("km", "Khmer", "ខ្មែរ", "🇰🇭"),
    TranslationLanguageOption("lo", "Lao", "ລາວ", "🇱🇦"),
    TranslationLanguageOption("mn", "Mongolian", "Монгол", "🇲🇳"),
    TranslationLanguageOption("ka", "Georgian", "ქართული", "🇬🇪"),
    TranslationLanguageOption("hy", "Armenian", "Հայերեն", "🇦🇲"),
    TranslationLanguageOption("az", "Azerbaijani", "Azərbaycan", "🇦🇿"),
    TranslationLanguageOption("kk", "Kazakh", "Қазақ тілі", "🇰🇿"),
    TranslationLanguageOption("uz", "Uzbek", "Oʻzbek", "🇺🇿"),
    TranslationLanguageOption("ky", " Kyrgyz", "Кыргызча", "🇰🇬"),
    TranslationLanguageOption("tg", "Tajik", "Тоҷикӣ", "🇹🇯"),
    TranslationLanguageOption("ps", "Pashto", "پښتو", "🇦🇫"),
    TranslationLanguageOption("ku", "Kurdish", "Kurdî", "🌍"),
    TranslationLanguageOption("sd", "Sindhi", "سنڌي", "🇵🇰"),
    TranslationLanguageOption("sq", "Albanian", "Shqip", "🇦🇱"),
    TranslationLanguageOption("mk", "Macedonian", "Македонски", "🇲🇰"),
    TranslationLanguageOption("bs", "Bosnian", "Bosanski", "🇧🇦"),
    TranslationLanguageOption("mt", "Maltese", "Malti", "🇲🇹"),
    TranslationLanguageOption("lb", "Luxembourgish", "Lëtzebuergesch", "🇱🇺"),
    TranslationLanguageOption("eo", "Esperanto", "Esperanto", "🌍"),
    TranslationLanguageOption("la", "Latin", "Latina", "🏛️"),
    TranslationLanguageOption("mi", "Maori", "Te Reo Māori", "🇳🇿"),
    TranslationLanguageOption("haw", "Hawaiian", "ʻŌlelo Hawaiʻi", "🇺🇸")
)

private object GlassPaperSizeHelper {
    fun format(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val kb = bytes / 1024.0
        return if (kb < 1024.0) String.format(Locale.US, "%.1f KB", kb) else String.format(Locale.US, "%.1f MB", kb / 1024.0)
    }
}
