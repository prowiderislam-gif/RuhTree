package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageExporter {

    fun saveBitmapToDevice(
        context: Context,
        bitmap: Bitmap,
        filenamePrefix: String = "FamilyTree"
    ): Uri? {
        val filename = "${filenamePrefix}_${System.currentTimeMillis()}.png"
        var savedUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FamilyTree")
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val stream: OutputStream? = context.contentResolver.openOutputStream(uri)
                    if (stream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.flush()
                        stream.close()
                        savedUri = uri
                    }
                }
            } else {
                val imagesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FamilyTree")
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val imageFile = File(imagesDir, filename)
                val fos = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
                savedUri = Uri.fromFile(imageFile)
            }

            // Also ensure a local internal copy is saved for guaranteed access
            val internalDir = File(context.filesDir, "exports")
            if (!internalDir.exists()) internalDir.mkdirs()
            val internalFile = File(internalDir, filename)
            val internalFos = FileOutputStream(internalFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, internalFos)
            internalFos.flush()
            internalFos.close()

            Toast.makeText(context, "Saved successfully to Photos / Internal Memory!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        return savedUri
    }

    fun shareImage(context: Context, bitmap: Bitmap, shareText: String = "Shared from Family Tree") {
        try {
            val cacheDir = File(context.cacheDir, "shared_images")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, "family_tree_${System.currentTimeMillis()}.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = try {
                FileProvider.getUriForFile(context, authority, file)
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
