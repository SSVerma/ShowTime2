package com.ssverma.feature.library.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ShareImageHelper {
    private const val TAG = "ShareImageHelper"
    private const val FOLDER_NAME = "shared_receipts"

    fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        chooserTitle: String
    ): Boolean {
        return try {
            val uri = saveBitmapToCache(context, bitmap) ?: return false
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share bitmap", e)
            false
        }
    }

    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String = "ShowTime_Cinema_Receipt"
    ): Boolean {
        val fileName = "${title}_${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/ShowTime"
                    )
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                if (uri != null) {
                    outputStream = context.contentResolver.openOutputStream(uri)
                }
            } else {
                val picturesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val showTimeDir = File(picturesDir, "ShowTime").apply { if (!exists()) mkdirs() }
                val imageFile = File(showTimeDir, fileName)
                outputStream = FileOutputStream(imageFile)
            }

            outputStream?.let { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save bitmap to gallery", e)
            false
        } finally {
            try {
                outputStream?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cacheFolder = File(context.cacheDir, FOLDER_NAME).apply {
                if (!exists()) mkdirs()
            }
            // Cleanup old receipts (> 1 hour old)
            val now = System.currentTimeMillis()
            cacheFolder.listFiles()?.forEach { file ->
                if (now - file.lastModified() > 3600_000) {
                    file.delete()
                }
            }

            val file = File(cacheFolder, "receipt_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save bitmap to cache", e)
            null
        }
    }
}
