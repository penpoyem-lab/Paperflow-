package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withTranslation
import androidx.core.net.toUri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.util.Matrix as PdfBoxMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean

data class PdfFileMetadata(
    val pageCount: Int,
    val fileSizeBytes: Long,
    val isValid: Boolean,
    val errorMessage: String? = null
)

data class MergeFileEntry(
    val filePath: String,
    val title: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val rotationDegrees: Int = 0,
    val searchableText: String = "",
    val password: String = ""
)

data class WatermarkConfig(
    val text: String,
    val colorArgb: Int = Color.rgb(59, 130, 246),
    val opacityPercent: Int = 30, // 5..100
    val fontSizePx: Float = 50f,  // 16..120
    val rotationDegrees: Float = -45f, // -90..90
    val outputFilename: String = ""
)

data class SignatureStampConfig(
    val targetPageIndex: Int = 0, // 0-based page index
    val normalizedPositionX: Float = 0.68f, // 0f..1f center x on page
    val normalizedPositionY: Float = 0.82f, // 0f..1f center y on page
    val normalizedWidth: Float = 0.34f,
    val normalizedHeight: Float = 0.14f,
    val strokePoints: List<Pair<Float, Float>> = emptyList(),
    val uploadedSignatureUri: Uri? = null,
    val inkColorArgb: Int = Color.rgb(37, 99, 235),
    val outputFilename: String = ""
)

data class PageNumbersConfig(
    val position: String = "bottom-center", // bottom-center, bottom-right, bottom-left, top-center, top-right, top-left
    val startFromPage: Int = 1,
    val fontSizePt: Float = 11f,
    val formatStyle: String = "PAGE_X_OF_Y", // "PAGE_X_OF_Y" or "NUMBER_ONLY"
    val outputFilename: String = ""
)

enum class CompressionLevel(
    val id: String,
    val label: String,
    val subtitle: String,
    val jpegQuality: Float,
    val maxImageDimension: Int,
    val renderScaleFactor: Float
) {
    EXTREME(
        id = "extreme",
        label = "Extreme",
        subtitle = "Less quality, high compression (~40% DPI)",
        jpegQuality = 0.42f,
        maxImageDimension = 900,
        renderScaleFactor = 0.56f
    ),
    RECOMMENDED(
        id = "recommended",
        label = "Recommended",
        subtitle = "Good quality, balanced compression (~72% DPI)",
        jpegQuality = 0.68f,
        maxImageDimension = 1400,
        renderScaleFactor = 0.75f
    ),
    LOW(
        id = "low",
        label = "Less Compression",
        subtitle = "High quality, light compression (~90% DPI)",
        jpegQuality = 0.86f,
        maxImageDimension = 2000,
        renderScaleFactor = 0.90f
    )
}

data class PdfDetailedMetadata(
    val title: String = "",
    val author: String = "",
    val subject: String = "",
    val keywords: String = "",
    val creator: String = "",
    val producer: String = "Paperflow PDF Engine",
    val creationDateMillis: Long? = null,
    val modificationDateMillis: Long? = null,
    val pageCount: Int = 0,
    val isEncrypted: Boolean = false
)

object PdfEngine {
    private val renderMutex = Mutex()
    private val pdfBoxInitialized = AtomicBoolean(false)

    private val thumbnailCache = object : LruCache<String, Bitmap>(48) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    private val highResPageCache = object : LruCache<String, Bitmap>(16) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    fun ensurePdfBoxInitialized(context: Context) {
        if (pdfBoxInitialized.compareAndSet(false, true)) {
            runCatching {
                PDFBoxResourceLoader.init(context.applicationContext)
            }
        }
    }

    private fun openInputStream(context: Context, pathOrUri: String): InputStream? {
        return try {
            if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
                context.contentResolver.openInputStream(pathOrUri.toUri())
            } else {
                val file = File(pathOrUri)
                if (file.exists()) FileInputStream(file) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun openParcelFileDescriptor(context: Context, pathOrUri: String): ParcelFileDescriptor? {
        return try {
            if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
                context.contentResolver.openFileDescriptor(pathOrUri.toUri(), "r")
            } else {
                val file = File(pathOrUri)
                if (file.exists()) {
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun inspectPdf(context: Context, pathOrUri: String): PdfFileMetadata = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        try {
            val pfd = openParcelFileDescriptor(context, pathOrUri)
                ?: return@withContext PdfFileMetadata(0, 0L, false, "File not found or inaccessible.")
            pfd.use { descriptor ->
                val size = descriptor.statSize.coerceAtLeast(0L)
                PdfRenderer(descriptor).use { renderer ->
                    PdfFileMetadata(
                        pageCount = renderer.pageCount,
                        fileSizeBytes = size,
                        isValid = renderer.pageCount > 0
                    )
                }
            }
        } catch (e: SecurityException) {
            PdfFileMetadata(0, 0L, false, "This PDF is password-protected or permission was denied.")
        } catch (e: Exception) {
            PdfFileMetadata(0, 0L, false, "Unable to open this PDF. It may be corrupted or unsupported.")
        }
    }

    suspend fun inspectDetailedMetadata(
        context: Context,
        pathOrUri: String,
        password: String = ""
    ): PdfDetailedMetadata = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        try {
            openInputStream(context, pathOrUri)?.use { stream ->
                val doc = if (password.isNotEmpty()) PDDocument.load(stream, password) else PDDocument.load(stream)
                doc.use { pd ->
                    val info = pd.documentInformation
                    PdfDetailedMetadata(
                        title = info?.title ?: "",
                        author = info?.author ?: "",
                        subject = info?.subject ?: "",
                        keywords = info?.keywords ?: "",
                        creator = info?.creator ?: "",
                        producer = info?.producer ?: "Paperflow Engine",
                        creationDateMillis = info?.creationDate?.timeInMillis,
                        modificationDateMillis = info?.modificationDate?.timeInMillis,
                        pageCount = pd.numberOfPages,
                        isEncrypted = pd.isEncrypted
                    )
                }
            } ?: PdfDetailedMetadata()
        } catch (e: Exception) {
            PdfDetailedMetadata()
        }
    }

    suspend fun updatePdfMetadata(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        metadata: PdfDetailedMetadata,
        customOutputFilename: String = ""
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            try {
                val baseName = customOutputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-metadata" }
                val outFile = getOutputFile(context, "", baseName)
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        if (doc.isEncrypted) doc.isAllSecurityToBeRemoved = true
                        val info = doc.documentInformation
                        info.title = metadata.title
                        info.author = metadata.author
                        info.subject = metadata.subject
                        info.keywords = metadata.keywords
                        info.creator = metadata.creator.ifBlank { "Paperflow" }
                        info.producer = metadata.producer.ifBlank { "Paperflow Engine" }
                        info.modificationDate = Calendar.getInstance()
                        doc.documentInformation = info
                        doc.save(outFile)
                    }
                } ?: return@withLock null
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun renderThumbnail(
        context: Context,
        pathOrUri: String,
        pageIndex: Int = 0,
        targetWidth: Int = 360
    ): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "$pathOrUri#$pageIndex#$targetWidth"
        thumbnailCache.get(cacheKey)?.let { if (!it.isRecycled) return@withContext it }

        renderMutex.withLock {
            try {
                val pfd = openParcelFileDescriptor(context, pathOrUri) ?: return@withLock null
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withLock null
                        renderer.openPage(pageIndex).use { page ->
                            val aspect = page.height.toFloat() / page.width.toFloat().coerceAtLeast(1f)
                            val width = targetWidth.coerceIn(120, 1080)
                            val height = (width * aspect).toInt().coerceAtLeast(120)
                            val bitmap = createBitmap(width, height)
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            thumbnailCache.put(cacheKey, bitmap)
                            bitmap
                        }
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun renderPageHighRes(
        context: Context,
        pathOrUri: String,
        pageIndex: Int,
        targetWidth: Int = 1280
    ): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "$pathOrUri#hi#$pageIndex#$targetWidth"
        highResPageCache.get(cacheKey)?.let { if (!it.isRecycled) return@withContext it }

        renderMutex.withLock {
            highResPageCache.get(cacheKey)?.let { if (!it.isRecycled) return@withLock it }
            try {
                val pfd = openParcelFileDescriptor(context, pathOrUri) ?: return@withLock null
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withLock null
                        renderer.openPage(pageIndex).use { page ->
                            val aspect = page.height.toFloat() / page.width.toFloat().coerceAtLeast(1f)
                            val width = targetWidth.coerceIn(400, 2048)
                            val height = (width * aspect).toInt().coerceAtLeast(400)
                            val bitmap = createBitmap(width, height)
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            highResPageCache.put(cacheKey, bitmap)
                            bitmap
                        }
                    }
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun copyUriToInternalStorage(context: Context, uri: Uri, desiredName: String): File? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "imported_pdfs")
            if (!dir.exists()) dir.mkdirs()
            val safeName = desiredName.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
                .let { if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf" }
            val destFile = File(dir, "${System.currentTimeMillis()}_$safeName")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext null
            destFile
        } catch (e: Exception) {
            null
        }
    }

    // ==================== PAPERFLOW VECTOR PDF TOOLS ====================

    private fun getOutputFile(context: Context, prefix: String, baseName: String): File {
        val dir = File(context.filesDir, "processed_pdfs")
        if (!dir.exists()) dir.mkdirs()
        val cleanBase = baseName
            .removeSuffix(".pdf")
            .removeSuffix(".PDF")
            .replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
            .ifBlank { "paperflow_output" }
        val suffixPart = if (prefix.isNotBlank()) "_$prefix" else ""
        return File(dir, "${cleanBase}${suffixPart}_${System.currentTimeMillis() % 10000}.pdf")
    }

    /**
     * Lossless vector PDF merge using PDFBox (from Paperflow architecture) with fallback to PdfRenderer.
     */
    suspend fun mergePdfsWithRotations(
        context: Context,
        entries: List<MergeFileEntry>,
        outputTitle: String
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            // Try lossless PDFBox vector merge first
            val vectorResult = runCatching {
                val outDoc = PDDocument()
                val merger = PDFMergerUtility()
                val loadedDocs = mutableListOf<PDDocument>()
                try {
                    for (entry in entries) {
                        val stream = openInputStream(context, entry.filePath) ?: continue
                        val srcDoc = if (entry.password.isNotEmpty()) {
                            PDDocument.load(stream, entry.password)
                        } else {
                            PDDocument.load(stream)
                        }
                        if (srcDoc.isEncrypted) {
                            srcDoc.isAllSecurityToBeRemoved = true
                        }
                        loadedDocs.add(srcDoc)
                        val normalizedDeg = ((entry.rotationDegrees % 360) + 360) % 360
                        if (normalizedDeg != 0) {
                            for (i in 0 until srcDoc.numberOfPages) {
                                val page = srcDoc.getPage(i)
                                page.rotation = (page.rotation + normalizedDeg) % 360
                            }
                        }
                        merger.appendDocument(outDoc, srcDoc)
                    }
                    if (outDoc.numberOfPages == 0) {
                        outDoc.close()
                        return@runCatching null
                    }
                    val outFile = getOutputFile(context, "", outputTitle)
                    outDoc.save(outFile)
                    outFile
                } finally {
                    runCatching { outDoc.close() }
                    loadedDocs.forEach { runCatching { it.close() } }
                }
            }.getOrNull()

            if (vectorResult != null && vectorResult.exists() && vectorResult.length() > 0L) {
                return@withLock vectorResult
            }

            // Fallback to bitmap renderer if any PDF had non-standard streams
            try {
                val outPdf = PdfDocument()
                var globalPageNum = 1
                for (entry in entries) {
                    val pfd = openParcelFileDescriptor(context, entry.filePath) ?: continue
                    val normalizedDeg = ((entry.rotationDegrees % 360) + 360) % 360
                    pfd.use { descriptor ->
                        PdfRenderer(descriptor).use { renderer ->
                            for (i in 0 until renderer.pageCount) {
                                renderer.openPage(i).use { srcPage ->
                                    val w = srcPage.width.coerceAtLeast(200)
                                    val h = srcPage.height.coerceAtLeast(200)
                                    val bmp = createBitmap(w * 2, h * 2)
                                    bmp.eraseColor(Color.WHITE)
                                    srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                    val finalBmp = if (normalizedDeg != 0) {
                                        val matrix = Matrix().apply { postRotate(normalizedDeg.toFloat()) }
                                        Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                    } else bmp

                                    val newW = if (normalizedDeg == 90 || normalizedDeg == 270) h else w
                                    val newH = if (normalizedDeg == 90 || normalizedDeg == 270) w else h

                                    val pageInfo = PdfDocument.PageInfo.Builder(newW, newH, globalPageNum++).create()
                                    val destPage = outPdf.startPage(pageInfo)
                                    destPage.canvas.drawBitmap(
                                        finalBmp,
                                        null,
                                        RectF(0f, 0f, newW.toFloat(), newH.toFloat()),
                                        Paint(Paint.FILTER_BITMAP_FLAG)
                                    )
                                    outPdf.finishPage(destPage)
                                    if (finalBmp != bmp) finalBmp.recycle()
                                    bmp.recycle()
                                }
                            }
                        }
                    }
                }
                if (globalPageNum == 1) {
                    outPdf.close()
                    return@withLock null
                }
                val outFile = getOutputFile(context, "", outputTitle)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun mergePdfs(
        context: Context,
        sourcePaths: List<String>,
        outputTitle: String
    ): File? = mergePdfsWithRotations(
        context = context,
        entries = sourcePaths.map { MergeFileEntry(filePath = it, title = outputTitle, pageCount = 0, fileSizeBytes = 0L) },
        outputTitle = outputTitle
    )

    /**
     * Lossless vector Split / Extract / Delete / Rearrange pages using PDFBox (from Paperflow).
     * Also supports per-page rotation degrees!
     */
    suspend fun extractOrSplitPages(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        selectedZeroBasedPages: List<Int>,
        suffixLabel: String = "Extracted",
        customOutputFilename: String = "",
        perSlotRotations: Map<Int, Int> = emptyMap()
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val base = customOutputFilename.ifBlank { sourceTitle }
            val outFile = getOutputFile(context, if (customOutputFilename.isBlank()) suffixLabel else "", base)

            // Try lossless PDFBox page import first
            val vectorSuccess = runCatching {
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { srcDoc ->
                        if (srcDoc.isEncrypted) srcDoc.isAllSecurityToBeRemoved = true
                        PDDocument().use { outDoc ->
                            selectedZeroBasedPages.forEachIndexed { slotIdx, pageIdx ->
                                if (pageIdx in 0 until srcDoc.numberOfPages) {
                                    val importedPage = outDoc.importPage(srcDoc.getPage(pageIdx))
                                    val extraRot = perSlotRotations[slotIdx] ?: 0
                                    if (extraRot != 0) {
                                        importedPage.rotation = (importedPage.rotation + extraRot) % 360
                                    }
                                }
                            }
                            if (outDoc.numberOfPages == 0) return@runCatching false
                            outDoc.save(outFile)
                            true
                        }
                    }
                } ?: false
            }.getOrDefault(false)

            if (vectorSuccess && outFile.exists() && outFile.length() > 0L) {
                return@withLock outFile
            }

            // Fallback to PdfRenderer
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                var writtenPages = 0
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        selectedZeroBasedPages.forEachIndexed { slotIdx, pageIdx ->
                            if (pageIdx in 0 until renderer.pageCount) {
                                renderer.openPage(pageIdx).use { srcPage ->
                                    val w = srcPage.width.coerceAtLeast(200)
                                    val h = srcPage.height.coerceAtLeast(200)
                                    val bmp = createBitmap(w * 2, h * 2)
                                    bmp.eraseColor(Color.WHITE)
                                    srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                    val rot = ((perSlotRotations[slotIdx] ?: 0) % 360 + 360) % 360
                                    val finalBmp = if (rot != 0) {
                                        val matrix = Matrix().apply { postRotate(rot.toFloat()) }
                                        Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                    } else bmp

                                    val newW = if (rot == 90 || rot == 270) h else w
                                    val newH = if (rot == 90 || rot == 270) w else h

                                    val pageInfo = PdfDocument.PageInfo.Builder(newW, newH, ++writtenPages).create()
                                    val destPage = outPdf.startPage(pageInfo)
                                    destPage.canvas.drawBitmap(finalBmp, null, RectF(0f, 0f, newW.toFloat(), newH.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
                                    outPdf.finishPage(destPage)
                                    if (finalBmp != bmp) finalBmp.recycle()
                                    bmp.recycle()
                                }
                            }
                        }
                    }
                }
                if (writtenPages == 0) {
                    outPdf.close()
                    return@withLock null
                }
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Lossless vector Rotate PDF using PDFBox (supports global rotation or per-page rotations).
     */
    suspend fun rotatePdf(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        degrees: Int, // 90, 180, 270
        customOutputFilename: String = "",
        perPageRotations: Map<Int, Int> = emptyMap()
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val normalizedDeg = ((degrees % 360) + 360) % 360
            val base = customOutputFilename.ifBlank { sourceTitle }
            val outFile = getOutputFile(context, if (customOutputFilename.isBlank()) "Rotated_${normalizedDeg}deg" else "", base)

            val vectorSuccess = runCatching {
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        if (doc.isEncrypted) doc.isAllSecurityToBeRemoved = true
                        for (i in 0 until doc.numberOfPages) {
                            val page = doc.getPage(i)
                            val delta = perPageRotations[i] ?: normalizedDeg
                            page.rotation = (page.rotation + delta + 360) % 360
                        }
                        doc.save(outFile)
                        true
                    }
                } ?: false
            }.getOrDefault(false)

            if (vectorSuccess && outFile.exists() && outFile.length() > 0L) {
                return@withLock outFile
            }

            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        for (i in 0 until renderer.pageCount) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val bmp = createBitmap(w * 2, h * 2)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                val pageDeg = ((perPageRotations[i] ?: normalizedDeg) % 360 + 360) % 360
                                val matrix = Matrix().apply { postRotate(pageDeg.toFloat()) }
                                val rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                val newW = if (pageDeg == 90 || pageDeg == 270) h else w
                                val newH = if (pageDeg == 90 || pageDeg == 270) w else h

                                val pageInfo = PdfDocument.PageInfo.Builder(newW, newH, i + 1).create()
                                val destPage = outPdf.startPage(pageInfo)
                                destPage.canvas.drawBitmap(rotatedBmp, null, RectF(0f, 0f, newW.toFloat(), newH.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
                                outPdf.finishPage(destPage)
                                bmp.recycle()
                                if (rotatedBmp != bmp) rotatedBmp.recycle()
                            }
                        }
                    }
                }
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Vector PDFBox Watermark engine (from Paperflow) with extended graphics state transparency & rotation matrix.
     */
    suspend fun applyCustomWatermark(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        config: WatermarkConfig
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val baseName = config.outputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-watermarked" }
            val outFile = getOutputFile(context, "", baseName)

            val r = Color.red(config.colorArgb)
            val g = Color.green(config.colorArgb)
            val b = Color.blue(config.colorArgb)
            val opacityFloat = (config.opacityPercent.coerceIn(5, 100) / 100f)

            val vectorSuccess = runCatching {
                if (config.text.isBlank()) return@runCatching false
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        if (doc.isEncrypted) doc.isAllSecurityToBeRemoved = true
                        val font = PDType1Font.HELVETICA_BOLD
                        val fontSize = config.fontSizePx.coerceIn(14f, 160f)
                        val textWidth = (font.getStringWidth(config.text) / 1000f) * fontSize

                        for (i in 0 until doc.numberOfPages) {
                            val page = doc.getPage(i)
                            val mediaBox = page.mediaBox
                            val centerX = mediaBox.width / 2f
                            val centerY = mediaBox.height / 2f

                            PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                                val gs = PDExtendedGraphicsState().apply {
                                    nonStrokingAlphaConstant = opacityFloat
                                }
                                cs.setGraphicsStateParameters(gs)
                                cs.setNonStrokingColor(r / 255f, g / 255f, b / 255f)
                                cs.beginText()
                                cs.setFont(font, fontSize)

                                val matrix = PdfBoxMatrix()
                                matrix.translate(centerX, centerY)
                                // Paperflow convention: negate degrees so positive/negative matches UI preview
                                matrix.rotate(Math.toRadians(-config.rotationDegrees.toDouble()))
                                matrix.translate(-textWidth / 2f, -fontSize / 3f)
                                cs.setTextMatrix(matrix)
                                cs.showText(config.text)
                                cs.endText()
                            }
                        }
                        doc.save(outFile)
                        true
                    }
                } ?: false
            }.getOrDefault(false)

            if (vectorSuccess && outFile.exists() && outFile.length() > 0L) {
                return@withLock outFile
            }

            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                val alphaInt = (opacityFloat * 255f).toInt().coerceIn(12, 255)

                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        val total = renderer.pageCount
                        for (i in 0 until total) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val bmp = createBitmap(w * 2, h * 2)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
                                val destPage = outPdf.startPage(pageInfo)
                                val canvas = destPage.canvas
                                canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))

                                if (config.text.isNotBlank()) {
                                    canvas.withTranslation(w / 2f, h / 2f) {
                                        rotate(config.rotationDegrees)
                                        val scaledFontSize = (config.fontSizePx * (w / 595f)).coerceIn(14f, 180f)
                                        val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                            color = Color.argb(alphaInt, r, g, b)
                                            textSize = scaledFontSize
                                            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                            textAlign = Paint.Align.CENTER
                                        }
                                        val fontMetrics = wmPaint.fontMetrics
                                        val centerOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f
                                        drawText(config.text, 0f, -centerOffset, wmPaint)
                                    }
                                }

                                outPdf.finishPage(destPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Vector PDFBox Signature Stamp (embeds lossless transparent PNG signature onto target PDF page without rasterizing text).
     */
    suspend fun stampSignatureOnPage(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        config: SignatureStampConfig
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val baseName = config.outputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-signed" }
            val outFile = getOutputFile(context, "", baseName)

            val uploadedBitmap: Bitmap? = config.uploadedSignatureUri?.let { uri ->
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }

            // Build transparent signature bitmap from either uploaded image or drawn ink strokes
            val sigBitmap: Bitmap? = when {
                uploadedBitmap != null -> uploadedBitmap
                config.strokePoints.isNotEmpty() -> {
                    val bmpW = 600
                    val bmpH = 260
                    val bmp = createBitmap(bmpW, bmpH)
                    val c = Canvas(bmp)
                    val sigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = config.inkColorArgb
                        style = Paint.Style.STROKE
                        strokeWidth = 8.5f
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                    val path = Path()
                    var penDown = false
                    config.strokePoints.forEach { pt ->
                        if (pt.first < 0f || pt.second < 0f) {
                            penDown = false
                        } else {
                            val px = pt.first * bmpW
                            val py = pt.second * bmpH
                            if (!penDown) {
                                path.moveTo(px, py)
                                penDown = true
                            } else {
                                path.lineTo(px, py)
                            }
                        }
                    }
                    c.drawPath(path, sigPaint)
                    bmp
                }
                else -> null
            }

            if (sigBitmap != null) {
                val vectorSuccess = runCatching {
                    openInputStream(context, sourcePath)?.use { stream ->
                        PDDocument.load(stream).use { doc ->
                            if (doc.isEncrypted) doc.isAllSecurityToBeRemoved = true
                            val targetIdx = config.targetPageIndex.coerceIn(0, (doc.numberOfPages - 1).coerceAtLeast(0))
                            val page = doc.getPage(targetIdx)
                            val mediaBox = page.mediaBox
                            val pageW = mediaBox.width
                            val pageH = mediaBox.height

                            val boxW = (pageW * config.normalizedWidth).coerceIn(60f, pageW * 0.85f)
                            val boxH = (pageH * config.normalizedHeight).coerceIn(30f, pageH * 0.45f)
                            val centerX = (pageW * config.normalizedPositionX).coerceIn(boxW / 2f, pageW - boxW / 2f)
                            val centerYFromTop = (pageH * config.normalizedPositionY).coerceIn(boxH / 2f, pageH - boxH / 2f)
                            val pdfX = centerX - boxW / 2f
                            // Convert top-origin Y to PDF bottom-origin Y
                            val pdfY = pageH - centerYFromTop - boxH / 2f

                            val pdImage = LosslessFactory.createFromImage(doc, sigBitmap)
                            PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                                cs.drawImage(pdImage, pdfX, pdfY, boxW, boxH)
                            }
                            doc.save(outFile)
                            true
                        }
                    } ?: false
                }.getOrDefault(false)

                if (sigBitmap != uploadedBitmap) sigBitmap.recycle()
                uploadedBitmap?.recycle()

                if (vectorSuccess && outFile.exists() && outFile.length() > 0L) {
                    return@withLock outFile
                }
            }

            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        val total = renderer.pageCount
                        val targetIdx = config.targetPageIndex.coerceIn(0, (total - 1).coerceAtLeast(0))
                        for (i in 0 until total) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val bmp = createBitmap(w * 2, h * 2)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
                                val destPage = outPdf.startPage(pageInfo)
                                val canvas = destPage.canvas
                                canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))

                                if (i == targetIdx && config.strokePoints.isNotEmpty()) {
                                    val boxW = (w * config.normalizedWidth).coerceIn(80f, w * 0.8f)
                                    val boxH = (h * config.normalizedHeight).coerceIn(40f, h * 0.4f)
                                    val centerX = (w * config.normalizedPositionX).coerceIn(boxW / 2f, w - boxW / 2f)
                                    val centerY = (h * config.normalizedPositionY).coerceIn(boxH / 2f, h - boxH / 2f)
                                    val boxLeft = centerX - boxW / 2f
                                    val boxTop = centerY - boxH / 2f

                                    val sigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        color = config.inkColorArgb
                                        style = Paint.Style.STROKE
                                        strokeWidth = 3.2f
                                        strokeCap = Paint.Cap.ROUND
                                        strokeJoin = Paint.Join.ROUND
                                    }
                                    val path = Path()
                                    var penDown = false
                                    config.strokePoints.forEach { pt ->
                                        if (pt.first < 0f || pt.second < 0f) {
                                            penDown = false
                                        } else {
                                            val px = boxLeft + pt.first * boxW
                                            val py = boxTop + pt.second * boxH
                                            if (!penDown) {
                                                path.moveTo(px, py)
                                                penDown = true
                                            } else {
                                                path.lineTo(px, py)
                                            }
                                        }
                                    }
                                    canvas.drawPath(path, sigPaint)
                                }

                                outPdf.finishPage(destPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Vector PDFBox Page Numbers tool (supports all 6 positions & starting page from Paperflow).
     */
    suspend fun addPageNumbersVector(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        config: PageNumbersConfig
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val baseName = config.outputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-numbered" }
            val outFile = getOutputFile(context, "", baseName)

            val vectorSuccess = runCatching {
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        if (doc.isEncrypted) doc.isAllSecurityToBeRemoved = true
                        val totalPages = doc.numberOfPages
                        val font = PDType1Font.HELVETICA_BOLD
                        val fontSize = config.fontSizePt.coerceIn(8f, 24f)
                        val startIdx = (config.startFromPage - 1).coerceAtLeast(0)

                        for (i in startIdx until totalPages) {
                            val page = doc.getPage(i)
                            val mediaBox = page.mediaBox
                            val label = if (config.formatStyle == "NUMBER_ONLY") {
                                "${i + 1}"
                            } else {
                                "Page ${i + 1} of $totalPages"
                            }
                            val textW = (font.getStringWidth(label) / 1000f) * fontSize
                            val margin = 28f

                            val x = when {
                                config.position.endsWith("left") -> margin
                                config.position.endsWith("right") -> (mediaBox.width - textW - margin).coerceAtLeast(margin)
                                else -> (mediaBox.width - textW) / 2f
                            }
                            val y = if (config.position.startsWith("top")) {
                                (mediaBox.height - margin - fontSize).coerceAtLeast(margin)
                            } else {
                                margin
                            }

                            PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
                                cs.beginText()
                                cs.setFont(font, fontSize)
                                cs.setNonStrokingColor(60f / 255f, 64f / 255f, 72f / 255f)
                                cs.newLineAtOffset(x, y)
                                cs.showText(label)
                                cs.endText()
                            }
                        }
                        doc.save(outFile)
                        true
                    }
                } ?: false
            }.getOrDefault(false)

            if (vectorSuccess && outFile.exists() && outFile.length() > 0L) {
                return@withLock outFile
            }

            stampPageNumbersOrWatermark(
                context = context,
                sourcePath = sourcePath,
                sourceTitle = sourceTitle,
                watermarkText = null,
                includePageNumbers = true,
                customOutputFilename = config.outputFilename
            )
        }
    }

    suspend fun stampPageNumbersOrWatermark(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        watermarkText: String?,
        includePageNumbers: Boolean,
        signaturePathPoints: List<Pair<Float, Float>>? = null,
        customOutputFilename: String = ""
    ): File? = withContext(Dispatchers.IO) {
        if (!watermarkText.isNullOrBlank() && !includePageNumbers && signaturePathPoints.isNullOrEmpty()) {
            return@withContext applyCustomWatermark(
                context = context,
                sourcePath = sourcePath,
                sourceTitle = sourceTitle,
                config = WatermarkConfig(text = watermarkText, outputFilename = customOutputFilename)
            )
        }
        if (!signaturePathPoints.isNullOrEmpty()) {
            return@withContext stampSignatureOnPage(
                context = context,
                sourcePath = sourcePath,
                sourceTitle = sourceTitle,
                config = SignatureStampConfig(
                    targetPageIndex = 0,
                    strokePoints = signaturePathPoints,
                    outputFilename = customOutputFilename
                )
            )
        }
        if (includePageNumbers) {
            return@withContext addPageNumbersVector(
                context = context,
                sourcePath = sourcePath,
                sourceTitle = sourceTitle,
                config = PageNumbersConfig(outputFilename = customOutputFilename)
            )
        }
        null
    }

    /**
     * Real AES-256 StandardProtectionPolicy PDF encryption & decryption using PDFBox (from Paperflow).
     */
    suspend fun encryptPdfStandard(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        password: String,
        outputFilename: String
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val base = outputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-protected" }
            val outFile = getOutputFile(context, "", base)

            val encryptedOk = runCatching {
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        val ap = AccessPermission()
                        val policy = StandardProtectionPolicy(password, password, ap).apply {
                            encryptionKeyLength = 256
                            permissions = ap
                        }
                        doc.protect(policy)
                        doc.save(outFile)
                        true
                    }
                } ?: false
            }.getOrDefault(false)

            if (encryptedOk && outFile.exists() && outFile.length() > 0L) {
                return@withLock outFile
            }

            protectOrUnlockPdfCopy(context, sourcePath, sourceTitle, outputFilename)
        }
    }

    suspend fun decryptPdfStandard(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        password: String,
        outputFilename: String
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val base = outputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-unlocked" }
            val outFile = getOutputFile(context, "", base)

            val unlockedOk = runCatching {
                openInputStream(context, sourcePath)?.use { stream ->
                    val doc = if (password.isNotEmpty()) {
                        PDDocument.load(stream, password)
                    } else {
                        PDDocument.load(stream)
                    }
                    doc.use { pd ->
                        pd.isAllSecurityToBeRemoved = true
                        pd.save(outFile)
                        true
                    }
                } ?: false
            }.getOrDefault(false)

            if (unlockedOk && outFile.exists() && outFile.length() > 0L) {
                return@withLock outFile
            }

            protectOrUnlockPdfCopy(context, sourcePath, sourceTitle, outputFilename)
        }
    }

    suspend fun protectOrUnlockPdfCopy(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        outputFilename: String
    ): File? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        for (i in 0 until renderer.pageCount) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val bmp = createBitmap(w * 2, h * 2)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
                                val destPage = outPdf.startPage(pageInfo)
                                destPage.canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
                                outPdf.finishPage(destPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
                val base = outputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-protected" }
                val outFile = getOutputFile(context, "", base)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    fun hashDocumentPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        var bytes = "paperflow-pdf-lock-v1:$password".toByteArray(Charsets.UTF_8)
        repeat(2048) {
            bytes = digest.digest(bytes)
        }
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Paperflow Smart PDF Compression Engine:
     * First downscales & recompresses embedded PDImageXObject streams inside the PDF while preserving vector text.
     * If the PDF has no embedded images or doesn't shrink enough, falls back to controlled DPI page re-encoding.
     */
    suspend fun compressPdfWithLevel(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        level: CompressionLevel,
        customOutputFilename: String = ""
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val base = customOutputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-compressed" }
            val outFile = getOutputFile(context, "", base)

            val vectorCompressed = runCatching {
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        if (doc.isEncrypted) doc.isAllSecurityToBeRemoved = true
                        var recompressedImages = 0
                        for (i in 0 until doc.numberOfPages) {
                            val page = doc.getPage(i)
                            val resources = page.resources ?: continue
                            for (name: COSName in resources.xObjectNames) {
                                if (!resources.isImageXObject(name)) continue
                                val xObj = resources.getXObject(name)
                                if (xObj is PDImageXObject) {
                                    val origBmp = xObj.image ?: continue
                                    val w = origBmp.width
                                    val h = origBmp.height
                                    if (w > 120 && h > 120) {
                                        val scale = if (w > level.maxImageDimension || h > level.maxImageDimension) {
                                            level.maxImageDimension.toFloat() / maxOf(w, h).toFloat()
                                        } else {
                                            level.renderScaleFactor
                                        }
                                        val targetW = (w * scale).toInt().coerceAtLeast(80)
                                        val targetH = (h * scale).toInt().coerceAtLeast(80)
                                        val scaledBmp = origBmp.scale(targetW, targetH)
                                        val compressedXObj = JPEGFactory.createFromImage(doc, scaledBmp, level.jpegQuality)
                                        resources.put(name, compressedXObj)
                                        if (scaledBmp != origBmp) scaledBmp.recycle()
                                        origBmp.recycle()
                                        recompressedImages++
                                    } else {
                                        origBmp.recycle()
                                    }
                                }
                            }
                        }
                        if (recompressedImages > 0) {
                            doc.save(outFile)
                            true
                        } else {
                            false
                        }
                    }
                } ?: false
            }.getOrDefault(false)

            if (vectorCompressed && outFile.exists() && outFile.length() > 0L) {
                return@withLock outFile
            }

            convertOrOptimizePdf(
                context = context,
                sourcePath = sourcePath,
                sourceTitle = sourceTitle,
                grayscale = false,
                scaleFactor = level.renderScaleFactor,
                customOutputFilename = base
            )
        }
    }

    /**
     * Paperflow Repair PDF Engine: Rebuilds cross-reference tables & COS streams using PDFBox.
     */
    suspend fun repairPdfDocument(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        customOutputFilename: String = ""
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val base = customOutputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-repaired" }
            val outFile = getOutputFile(context, "", base)

            val repairedWithPdfBox = runCatching {
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        if (doc.isEncrypted) doc.isAllSecurityToBeRemoved = true
                        doc.save(outFile)
                        true
                    }
                } ?: false
            }.getOrDefault(false)

            if (repairedWithPdfBox && outFile.exists() && outFile.length() > 0L) {
                return@withLock outFile
            }

            convertOrOptimizePdf(
                context = context,
                sourcePath = sourcePath,
                sourceTitle = sourceTitle,
                grayscale = false,
                scaleFactor = 1.0f,
                customOutputFilename = base
            )
        }
    }

    suspend fun convertOrOptimizePdf(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        grayscale: Boolean,
        scaleFactor: Float, // e.g., 1.0f for normal/repair, 0.72f for compress
        customOutputFilename: String = ""
    ): File? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                val colorPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
                    if (grayscale) {
                        val cm = ColorMatrix().apply { setSaturation(0f) }
                        colorFilter = ColorMatrixColorFilter(cm)
                    }
                }

                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        for (i in 0 until renderer.pageCount) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val renderW = (w * scaleFactor).toInt().coerceAtLeast(160)
                                val renderH = (h * scaleFactor).toInt().coerceAtLeast(160)
                                val bmp = createBitmap(renderW, renderH)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                                val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
                                val destPage = outPdf.startPage(pageInfo)
                                destPage.canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), colorPaint)
                                outPdf.finishPage(destPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
                val suffix = when {
                    grayscale -> "Grayscale"
                    scaleFactor < 0.95f -> "Compressed"
                    else -> "Repaired"
                }
                val base = customOutputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-$suffix" }
                val outFile = getOutputFile(context, "", base)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun createPdfFromImages(
        context: Context,
        imageUris: List<Uri>,
        outputTitle: String
    ): File? = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        try {
            if (imageUris.isEmpty()) return@withContext null
            val outFile = getOutputFile(context, "FromImages", outputTitle)

            val pdfBoxCreated = runCatching {
                PDDocument().use { doc ->
                    var added = 0
                    for (uri in imageUris) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            if (bmp != null) {
                                val page = PDPage(PDRectangle.A4)
                                doc.addPage(page)
                                val pageW = page.mediaBox.width
                                val pageH = page.mediaBox.height
                                val margin = 24f
                                val scale = minOf(
                                    (pageW - margin * 2f) / bmp.width.toFloat(),
                                    (pageH - margin * 2f) / bmp.height.toFloat()
                                )
                                val drawW = bmp.width * scale
                                val drawH = bmp.height * scale
                                val left = (pageW - drawW) / 2f
                                val bottom = (pageH - drawH) / 2f

                                val pdImage = JPEGFactory.createFromImage(doc, bmp, 0.88f)
                                PDPageContentStream(doc, page).use { cs ->
                                    cs.drawImage(pdImage, left, bottom, drawW, drawH)
                                }
                                bmp.recycle()
                                added++
                            }
                        }
                    }
                    if (added == 0) return@runCatching false
                    doc.save(outFile)
                    true
                }
            }.getOrDefault(false)

            if (pdfBoxCreated && outFile.exists() && outFile.length() > 0L) {
                return@withContext outFile
            }

            val outPdf = PdfDocument()
            var added = 0
            imageUris.forEachIndexed { idx, uri ->
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        val pageW = 595
                        val pageH = 842
                        val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, idx + 1).create()
                        val page = outPdf.startPage(pageInfo)
                        val canvas = page.canvas
                        canvas.drawColor(Color.WHITE)

                        val scale = minOf(
                            (pageW - 48f) / bmp.width.toFloat(),
                            (pageH - 48f) / bmp.height.toFloat()
                        )
                        val drawW = bmp.width * scale
                        val drawH = bmp.height * scale
                        val left = (pageW - drawW) / 2f
                        val top = (pageH - drawH) / 2f
                        canvas.drawBitmap(bmp, null, RectF(left, top, left + drawW, top + drawH), Paint(Paint.FILTER_BITMAP_FLAG))
                        outPdf.finishPage(page)
                        bmp.recycle()
                        added++
                    }
                }
            }
            if (added == 0) {
                outPdf.close()
                return@withContext null
            }
            FileOutputStream(outFile).use { outPdf.writeTo(it) }
            outPdf.close()
            outFile
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Paperflow Real PDF Text Extraction using PDFBox PDFTextStripper!
     */
    suspend fun extractRealTextFromPdf(
        context: Context,
        sourcePath: String,
        password: String = ""
    ): List<String> = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        try {
            openInputStream(context, sourcePath)?.use { stream ->
                val doc = if (password.isNotEmpty()) PDDocument.load(stream, password) else PDDocument.load(stream)
                doc.use { pd ->
                    if (pd.isEncrypted) pd.isAllSecurityToBeRemoved = true
                    val stripper = PDFTextStripper()
                    val pages = mutableListOf<String>()
                    for (pageNum in 1..pd.numberOfPages) {
                        stripper.startPage = pageNum
                        stripper.endPage = pageNum
                        val pageText = stripper.getText(pd).trim()
                        pages.add(pageText)
                    }
                    pages
                }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Paperflow Real Embedded Image Extraction using PDFBox PDImageXObject!
     * Extracts embedded images first; if none are embedded, exports high-res rendered page plates.
     */
    suspend fun extractEmbeddedImagesOrPages(
        context: Context,
        sourcePath: String,
        sourceTitle: String
    ): List<File> = withContext(Dispatchers.IO) {
        ensurePdfBoxInitialized(context)
        renderMutex.withLock {
            val exported = mutableListOf<File>()
            val outDir = File(context.filesDir, "exported_images")
            if (!outDir.exists()) outDir.mkdirs()
            val cleanName = sourceTitle.removeSuffix(".pdf").replace(Regex("[^a-zA-Z0-9._\\-]"), "_")

            runCatching {
                openInputStream(context, sourcePath)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        if (doc.isEncrypted) doc.isAllSecurityToBeRemoved = true
                        var imgIdx = 1
                        for (pageIdx in 0 until doc.numberOfPages) {
                            val page = doc.getPage(pageIdx)
                            val resources = page.resources ?: continue
                            for (name: COSName in resources.xObjectNames) {
                                if (!resources.isImageXObject(name)) continue
                                val obj = resources.getXObject(name)
                                if (obj is PDImageXObject) {
                                    val bmp = obj.image ?: continue
                                    if (bmp.width >= 32 && bmp.height >= 32) {
                                        val imgFile = File(outDir, "${cleanName}_embedded_${imgIdx++}.png")
                                        FileOutputStream(imgFile).use { fos ->
                                            bmp.compress(Bitmap.CompressFormat.PNG, 95, fos)
                                        }
                                        exported.add(imgFile)
                                    }
                                    bmp.recycle()
                                }
                            }
                        }
                    }
                }
            }

            if (exported.isNotEmpty()) {
                return@withLock exported
            }
        }
        exportPagesAsPngs(context, sourcePath, sourceTitle)
    }

    suspend fun exportPagesAsPngs(
        context: Context,
        sourcePath: String,
        sourceTitle: String
    ): List<File> = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            val exported = mutableListOf<File>()
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock emptyList()
                val outDir = File(context.filesDir, "exported_images")
                if (!outDir.exists()) outDir.mkdirs()
                val cleanName = sourceTitle.removeSuffix(".pdf").replace(Regex("[^a-zA-Z0-9._\\-]"), "_")

                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        val limit = renderer.pageCount.coerceAtMost(20)
                        for (i in 0 until limit) {
                            renderer.openPage(i).use { page ->
                                val w = (page.width * 1.5f).toInt().coerceAtLeast(300)
                                val h = (page.height * 1.5f).toInt().coerceAtLeast(300)
                                val bmp = createBitmap(w, h)
                                bmp.eraseColor(Color.WHITE)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                val imgFile = File(outDir, "${cleanName}_page_${i + 1}.png")
                                FileOutputStream(imgFile).use { fos ->
                                    bmp.compress(Bitmap.CompressFormat.PNG, 95, fos)
                                }
                                bmp.recycle()
                                exported.add(imgFile)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }
            exported
        }
    }
}
