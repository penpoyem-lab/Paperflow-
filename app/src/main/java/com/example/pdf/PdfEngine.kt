package com.example.pdf

import android.content.Context
import android.graphics.Bitmap
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

data class PdfFileMetadata(
    val pageCount: Int,
    val fileSizeBytes: Long,
    val isValid: Boolean,
    val errorMessage: String? = null
)

object PdfEngine {
    private val renderMutex = Mutex()

    private val thumbnailCache = object : LruCache<String, Bitmap>(24) {
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
        renderMutex.withLock {
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
        val cleanBase = baseName.removeSuffix(".pdf").replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
        return File(dir, "${cleanBase}_${prefix}_${System.currentTimeMillis() % 10000}.pdf")
    }

    suspend fun mergePdfs(
        context: Context,
        sourcePaths: List<String>,
        outputTitle: String
    ): File? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            try {
                val outPdf = PdfDocument()
                var globalPageNum = 1
                for (path in sourcePaths) {
                    val pfd = openParcelFileDescriptor(context, path) ?: continue
                    pfd.use { descriptor ->
                        PdfRenderer(descriptor).use { renderer ->
                            for (i in 0 until renderer.pageCount) {
                                renderer.openPage(i).use { srcPage ->
                                    val w = srcPage.width.coerceAtLeast(200)
                                    val h = srcPage.height.coerceAtLeast(200)
                                    val bmp = Bitmap.createBitmap(w * 2, h * 2, Bitmap.Config.ARGB_8888)
                                    bmp.eraseColor(Color.WHITE)
                                    srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                                    val pageInfo = PdfDocument.PageInfo.Builder(w, h, globalPageNum++).create()
                                    val destPage = outPdf.startPage(pageInfo)
                                    destPage.canvas.drawBitmap(bmp, null, RectF(0f, 0f, w.toFloat(), h.toFloat()), Paint(Paint.FILTER_BITMAP_FLAG))
                                    outPdf.finishPage(destPage)
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
                val outFile = getOutputFile(context, "Merged", outputTitle)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun extractOrSplitPages(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        selectedZeroBasedPages: List<Int>,
        suffixLabel: String = "Extracted"
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
                val outFile = getOutputFile(context, suffixLabel, sourceTitle)
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
        degrees: Int // 90, 180, 270
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
                val outFile = getOutputFile(context, "Rotated_${normalizedDeg}deg", sourceTitle)
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
        signaturePathPoints: List<Pair<Float, Float>>? = null
    ): File? = withContext(Dispatchers.IO) {
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

                                // Diagonal Watermark
                                if (!watermarkText.isNullOrBlank()) {
                                    canvas.save()
                                    canvas.translate(w / 2f, h / 2f)
                                    canvas.rotate(-35f)
                                    val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        color = Color.argb(48, 59, 130, 246)
                                        textSize = (w * 0.085f).coerceAtLeast(26f)
                                        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                                        textAlign = Paint.Align.CENTER
                                    }
                                    canvas.drawText(watermarkText.uppercase(), 0f, 0f, wmPaint)
                                    canvas.restore()
                                }

                                // Page Numbers
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

                                // Signature on final page
                                if (!signaturePathPoints.isNullOrEmpty() && i == total - 1) {
                                    val sigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                        color = Color.rgb(29, 78, 216)
                                        style = Paint.Style.STROKE
                                        strokeWidth = 2.8f
                                        strokeCap = Paint.Cap.ROUND
                                        strokeJoin = Paint.Join.ROUND
                                    }
                                    val boxLeft = w - 180f
                                    val boxTop = h - 110f
                                    val boxW = 136f
                                    val boxH = 64f
                                    val path = Path()
                                    signaturePathPoints.forEachIndexed { idx, pt ->
                                        val px = boxLeft + pt.first * boxW
                                        val py = boxTop + pt.second * boxH
                                        if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    canvas.drawPath(path, sigPaint)
                                }

                                outPdf.finishPage(destPage)
                                bmp.recycle()
                            }
                        }
                    }
                }
                val suffix = when {
                    !signaturePathPoints.isNullOrEmpty() -> "Signed"
                    !watermarkText.isNullOrBlank() -> "Watermarked"
                    else -> "Numbered"
                }
                val outFile = getOutputFile(context, suffix, sourceTitle)
                FileOutputStream(outFile).use { outPdf.writeTo(it) }
                outPdf.close()
                outFile
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun convertOrOptimizePdf(
        context: Context,
        sourcePath: String,
        sourceTitle: String,
        grayscale: Boolean,
        scaleFactor: Float // e.g., 1.0f for normal/repair, 0.72f for compress
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
                val outFile = getOutputFile(context, suffix, sourceTitle)
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
                    val bmp = android.graphics.BitmapFactory.decodeStream(stream)
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
                        val limit = renderer.pageCount.coerceAtMost(10)
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
