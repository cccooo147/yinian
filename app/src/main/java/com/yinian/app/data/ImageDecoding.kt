package com.yinian.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

/**
 * Shared decode path for imports and the crop preview, so both see the same
 * dimensions and EXIF rotation. Call from a background dispatcher.
 */
internal object ImageDecoding {

    fun decodeBounded(context: Context, uri: Uri, maxDimension: Int): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (
            bounds.outWidth / (sample * 2) >= maxDimension ||
            bounds.outHeight / (sample * 2) >= maxDimension
        ) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val orientation = readOrientation(context, uri)
        val transformed = if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            val matrix = matrixForOrientation(orientation)
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                .also { if (it !== bitmap) bitmap.recycle() }
        } else {
            bitmap
        }

        val longest = maxOf(transformed.width, transformed.height)
        if (longest <= maxDimension) return transformed

        val scale = maxDimension.toFloat() / longest
        val scaled = Bitmap.createScaledBitmap(
            transformed,
            (transformed.width * scale).toInt().coerceAtLeast(1),
            (transformed.height * scale).toInt().coerceAtLeast(1),
            true,
        )
        if (scaled !== transformed) transformed.recycle()
        return scaled
    }

    private fun readOrientation(context: Context, uri: Uri): Int = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun matrixForOrientation(orientation: Int): Matrix = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                postRotate(90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                postRotate(-90f)
                postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        }
    }
}
