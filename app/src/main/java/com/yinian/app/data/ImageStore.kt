package com.yinian.app.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Imported images are copied into app-private storage and downscaled once.
 * The UI only ever receives a short relative path, so external URI permissions
 * can never disappear under it and Coil can cache a reasonably sized file.
 */
class ImageStore(private val context: Context) {

    private val mediaDir: File
        get() = File(context.filesDir, MEDIA_DIR).apply { if (!exists()) mkdirs() }

    fun resolve(relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        if (relativePath.startsWith("/") || relativePath.startsWith("\\")) return null
        if (relativePath.contains(':')) return null
        if (relativePath.split('/', '\\').any { it == ".." }) return null
        return File(context.filesDir, relativePath)
    }

    suspend fun import(uri: Uri, maxDimension: Int = 1600): String? = withContext(Dispatchers.IO) {
        runCatching {
            val decoded = ImageDecoding.decodeBounded(context, uri, maxDimension)
                ?: return@runCatching null
            write(decoded)
        }.getOrNull()
    }

    /**
     * Applies the frame the user chose in the cropper. The math mirrors the
     * preview: a cover-fitted image, scaled around its centre and panned by the
     * gesture, then mapped back to source pixels.
     */
    suspend fun importCropped(
        uri: Uri,
        crop: CropSpec,
        targetWidth: Int = 1200,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val source = ImageDecoding.decodeBounded(context, uri, 2400)
                ?: return@runCatching null
            val iw = source.width.toFloat()
            val ih = source.height.toFloat()
            val viewW = crop.viewWidth.toFloat()
            val viewH = crop.viewHeight.toFloat()
            if (viewW <= 0f || viewH <= 0f || iw <= 0f || ih <= 0f) {
                source.recycle()
                return@runCatching null
            }

            val zoom = crop.zoom.coerceIn(1f, 4f)
            val cover = maxOf(viewW / iw, viewH / ih)
            val drawnW = iw * cover * zoom
            val drawnH = ih * cover * zoom
            val left = (viewW - drawnW) / 2f + crop.panX
            val top = (viewH - drawnH) / 2f + crop.panY
            val unit = cover * zoom

            val sx = (-left / unit).coerceIn(0f, (iw - 1f).coerceAtLeast(0f))
            val sy = (-top / unit).coerceIn(0f, (ih - 1f).coerceAtLeast(0f))
            val sw = (viewW / unit).coerceAtMost(iw - sx).coerceAtLeast(1f)
            val sh = (viewH / unit).coerceAtMost(ih - sy).coerceAtLeast(1f)

            val cropped = Bitmap.createBitmap(
                source,
                sx.toInt(),
                sy.toInt(),
                sw.toInt().coerceAtLeast(1),
                sh.toInt().coerceAtLeast(1),
            )
            if (cropped !== source) source.recycle()

            val outWidth = targetWidth.coerceAtMost(cropped.width)
            val aspect = if (crop.aspect > 0f) crop.aspect else viewW / viewH
            val outHeight = (outWidth / aspect).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(cropped, outWidth, outHeight, true)
            if (scaled !== cropped) cropped.recycle()

            write(scaled)
        }.getOrNull()
    }

    fun delete(relativePath: String?) {
        resolve(relativePath)?.delete()
    }

    private fun write(bitmap: Bitmap): String {
        val name = "${UUID.randomUUID()}.jpg"
        File(mediaDir, name).outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, stream)
        }
        bitmap.recycle()
        return "$MEDIA_DIR/$name"
    }

    private companion object {
        const val MEDIA_DIR = "media"
    }
}
