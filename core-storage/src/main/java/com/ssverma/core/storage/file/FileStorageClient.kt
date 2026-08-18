package com.ssverma.core.storage.file

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface FileStorageClient {
    fun writeCompressedString(fileName: String, content: String): File
    fun readCompressedString(file: File): String
    fun readCompressedString(fileName: String): String?
    fun getFile(fileName: String): File
    fun deleteFile(fileName: String): Boolean
}

@Singleton
class FileStorageClientImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileStorageClient {

    private val backupDir: File
        get() = File(context.filesDir, "backups").apply {
            if (!exists()) mkdirs()
        }

    override fun writeCompressedString(fileName: String, content: String): File {
        val targetFile = File(backupDir, fileName)
        FileOutputStream(targetFile).use { fos ->
            GZIPOutputStream(fos).use { gzos ->
                gzos.write(content.toByteArray(StandardCharsets.UTF_8))
                gzos.flush()
            }
        }
        return targetFile
    }

    override fun readCompressedString(file: File): String {
        if (!file.exists()) return ""
        FileInputStream(file).use { fis ->
            GZIPInputStream(fis).use { gzis ->
                val buffer = ByteArray(4096)
                val outputStream = ByteArrayOutputStream()
                var bytesRead: Int
                while (gzis.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                return outputStream.toString(StandardCharsets.UTF_8.name())
            }
        }
    }

    override fun readCompressedString(fileName: String): String? {
        val file = File(backupDir, fileName)
        if (!file.exists()) return null
        return readCompressedString(file)
    }

    override fun getFile(fileName: String): File {
        return File(backupDir, fileName)
    }

    override fun deleteFile(fileName: String): Boolean {
        val file = File(backupDir, fileName)
        return if (file.exists()) file.delete() else false
    }
}
