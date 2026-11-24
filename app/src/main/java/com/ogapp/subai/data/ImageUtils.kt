package com.ogapp.subai.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/** Camera capture + image downscale/encode helpers for AI diagnosis. */
object ImageUtils {

    /** Create a temp file + content Uri for the camera to write a capture into. */
    fun newCaptureUri(context: Context): Uri {
        val dir = File(context.cacheDir, "captures").apply { mkdirs() }
        val file = File(dir, "setup_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Decode [uri], downscale so the longest side <= [maxDim], JPEG-compress and
     * base64-encode (no wrapping, no data: prefix). Keeps API payloads small/fast.
     */
    fun loadScaledBase64(context: Context, uri: Uri, maxDim: Int = 1280, quality: Int = 82): String? {
        val resolver = context.contentResolver

        // First pass: bounds only, to compute a safe inSampleSize.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val longest = max(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while (longest / (sample * 2) >= maxDim) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null

        val scaled = scaleToMax(decoded, maxDim)
        if (scaled !== decoded) decoded.recycle()

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        scaled.recycle()
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    private fun scaleToMax(bmp: Bitmap, maxDim: Int): Bitmap {
        val longest = max(bmp.width, bmp.height)
        if (longest <= maxDim) return bmp
        val ratio = maxDim.toFloat() / longest
        val w = (bmp.width * ratio).roundToInt().coerceAtLeast(1)
        val h = (bmp.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bmp, w, h, true)
    }
}
