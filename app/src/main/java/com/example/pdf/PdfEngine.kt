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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

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
    val searchableText: String = ""
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

object PdfEngine {
    private val renderMutex = Mutex()

    private val thumbnailCache = object : LruCache<String, Bitmap>(48) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    private val highResPageCache = object : LruCache<String, Bitmap>(16) {
        override fun sizeOf(key: String, value: Bitmap): Int = 1
    }

    fun openParcelFileDescriptor(context: Context, pathOrUri: String): ParcelFileDescriptor? {
        return try {
            if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
                context.contentResolver.openFileDescriptor(Uri.parse(pathOrUri), "r")
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
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
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

    // ==================== REAL PDF TOOLS ====================

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

    suspend fun mergePdfsWithRotations(
        context: Context,
        entries: List<MergeFileEntry>,
        outputTitle: String
    ): File? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
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
                                    val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
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

    suspend fun extractOrSplitPages(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        selectedZeroBasedPages: List<Int>,
        suffixLabel: String = "Extracted",
        customOutputFilename: String = ""
    ): File? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                var writtenPages = 0
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        for (pageIdx in selectedZeroBasedPages) {
                            if (pageIdx in 0 until renderer.pageCount) {
                                renderer.openPage(pageIdx).use { srcPage ->
                                    val w = srcPage.width.coerceAtLeast(200)
                                    val h = srcPage.height.coerceAtLeast(200)
                                    val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                                    bmp.eraseColor(Color.WHITE)
                                    srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                    val pageInfo = PdfDocument.PageInfo.Builder(w, h, ++writtenPages).create()
                                    val destPage = outPdf.startPage(pageInfo)
                                    destPage.canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
                                    outPdf.finishPage(destPage)
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
                val base = customOutputFilename.ifBlank { sourceTitle }
                val outFile = getOutputFile(context, if (customOutputFilename.isBlank()) suffixLabel else "", base)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun rotatePdf(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        degrees: Int, // 90, 180, 270
        customOutputFilename: String = ""
    ): File? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                val normalizedDeg = ((degrees % 360) + 360) % 360
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        for (i in 0 until renderer.pageCount) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                val matrix = Matrix().apply { postRotate(normalizedDeg.toFloat()) }
                                val rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                val newW = if (normalizedDeg == 90 || normalizedDeg == 270) h else w
                                val newH = if (normalizedDeg == 90 || normalizedDeg == 270) w else h

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
                val base = customOutputFilename.ifBlank { sourceTitle }
                val outFile = getOutputFile(context, if (customOutputFilename.isBlank()) "Rotated_${normalizedDeg}deg" else "", base)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun applyCustomWatermark(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        config: WatermarkConfig
    ): File? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                val alphaInt = ((config.opacityPercent.coerceIn(5, 100) / 100f) * 255f).toInt().coerceIn(12, 255)
                val r = Color.red(config.colorArgb)
                val g = Color.green(config.colorArgb)
                val b = Color.blue(config.colorArgb)

                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        val total = renderer.pageCount
                        for (i in 0 until total) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
                                val destPage = outPdf.startPage(pageInfo)
                                val canvas = destPage.canvas
                                canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))

                                if (config.text.isNotBlank()) {
                                    canvas.save()
                                    canvas.translate(w / 2f, h / 2f)
                                    canvas.rotate(config.rotationDegrees)
                                    val scaledFontSize = (config.fontSizePx * (w / 595f)).coerceIn(14f, 180f)
                                    val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        color = Color.argb(alphaInt, r, g, b)
                                        textSize = scaledFontSize
                                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                        textAlign = Paint.Align.CENTER
                                    }
                                    val fontMetrics = wmPaint.fontMetrics
                                    val centerOffset = (fontMetrics.descent + fontMetrics.ascent) / 2f
                                    canvas.drawText(config.text, 0f, -centerOffset, wmPaint)
                                    canvas.restore()
                                }

                                outPdf.finishPage(destPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
                val baseName = config.outputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-watermarked" }
                val outFile = getOutputFile(context, "", baseName)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun stampSignatureOnPage(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        config: SignatureStampConfig
    ): File? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val uploadedBitmap: Bitmap? = config.uploadedSignatureUri?.let { uri ->
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                    }.getOrNull()
                }

                val outPdf = PdfDocument()
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        val total = renderer.pageCount
                        val targetIdx = config.targetPageIndex.coerceIn(0, (total - 1).coerceAtLeast(0))
                        for (i in 0 until total) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
                                val destPage = outPdf.startPage(pageInfo)
                                val canvas = destPage.canvas
                                canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))

                                if (i == targetIdx) {
                                    val boxW = (w * config.normalizedWidth).coerceIn(80f, w * 0.8f)
                                    val boxH = (h * config.normalizedHeight).coerceIn(40f, h * 0.4f)
                                    val centerX = (w * config.normalizedPositionX).coerceIn(boxW / 2f, w - boxW / 2f)
                                    val centerY = (h * config.normalizedPositionY).coerceIn(boxH / 2f, h - boxH / 2f)
                                    val boxLeft = centerX - boxW / 2f
                                    val boxTop = centerY - boxH / 2f

                                    if (uploadedBitmap != null) {
                                        val dstRect = RectF(boxLeft, boxTop, boxLeft + boxW, boxTop + boxH)
                                        canvas.drawBitmap(uploadedBitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
                                    } else if (config.strokePoints.isNotEmpty()) {
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
                                }

                                outPdf.finishPage(destPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
                uploadedBitmap?.recycle()
                val baseName = config.outputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-signed" }
                val outFile = getOutputFile(context, "", baseName)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
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
        renderMutex.withLock {
            try {
                val pfd = openParcelFileDescriptor(context, sourcePath) ?: return@withLock null
                val outPdf = PdfDocument()
                pfd.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        val total = renderer.pageCount
                        for (i in 0 until total) {
                            renderer.openPage(i).use { srcPage ->
                                val w = srcPage.width.coerceAtLeast(200)
                                val h = srcPage.height.coerceAtLeast(200)
                                val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                                bmp.eraseColor(Color.WHITE)
                                srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                val pageInfo = PdfDocument.PageInfo.Builder(w, h, i + 1).create()
                                val destPage = outPdf.startPage(pageInfo)
                                val canvas = destPage.canvas
                                canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))

                                if (includePageNumbers) {
                                    val pnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        color = Color.rgb(51, 65, 85)
                                        textSize = 11f
                                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                        textAlign = Paint.Align.CENTER
                                    }
                                    val pillRect = RectF(w / 2f - 42f, h - 34f, w / 2f + 42f, h - 14f)
                                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        color = Color.argb(220, 241, 245, 249)
                                    }
                                    canvas.drawRoundRect(pillRect, 10f, 10f, bgPaint)
                                    canvas.drawText("${i + 1} / $total", w / 2f, h - 20f, pnPaint)
                                }

                                outPdf.finishPage(destPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
                val baseName = customOutputFilename.ifBlank { "${sourceTitle.removeSuffix(".pdf")}-numbered" }
                val outFile = getOutputFile(context, "", baseName)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
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
                                val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
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
                                val bmp = Bitmap.createBitmap(renderW, renderH, Bitmap.Config.ARGB_8888)
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
        try {
            if (imageUris.isEmpty()) return@withContext null
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
            val outFile = getOutputFile(context, "FromImages", outputTitle)
            FileOutputStream(outFile).use { outPdf.writeTo(it) }
            outPdf.close()
            outFile
        } catch (e: Exception) {
            null
        }
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
                                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
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
