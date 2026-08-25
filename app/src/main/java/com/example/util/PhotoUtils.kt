package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.min

object PhotoUtils {

    /**
     * Converts a content Uri or file Uri into a lightweight Base64 JPEG data string.
     * This allows profile pictures to be embedded directly into cloud vault JSON files,
     * ensuring photos restore perfectly when downloaded on any device.
     */
    fun uriToBase64(context: Context, uri: Uri, maxDimension: Int = 400): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return null

            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null

            // Scale down to avatar size for efficient storage
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = min(maxDimension.toFloat() / width, maxDimension.toFloat() / height).coerceAtMost(1.0f)

            val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
            val scaledHeight = (height * scale).toInt().coerceAtLeast(1)

            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 82, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
