package com.ssverma.core.backup.drive

import android.os.Build
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.storage.file.FileStorageClient
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveBackupClient @Inject constructor(
    private val fileStorageClient: FileStorageClient
) {
    fun saveCompressedBackup(
        fileName: String,
        jsonPayload: String,
        timestamp: Long = System.currentTimeMillis(),
        deviceName: String = "",
        favoritesCount: Int = 0,
        watchlistCount: Int = 0,
        historyCount: Int = 0
    ): Pair<File, BackupMetadata> {
        val file = fileStorageClient.writeCompressedString(fileName, jsonPayload)
        val sizeBytes = file.length()

        val resolvedDeviceName = deviceName.ifBlank {
            val manufacturer = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() }.orEmpty()
            val model = Build.MODEL.orEmpty()
            if (manufacturer.isNotBlank() || model.isNotBlank()) "$manufacturer $model".trim() else "Android Device"
        }

        val metadata = BackupMetadata(
            timestamp = timestamp,
            formattedDate = formatDate(timestamp),
            sizeBytes = sizeBytes,
            formattedSize = formatBytes(sizeBytes),
            deviceName = resolvedDeviceName,
            favoritesCount = favoritesCount,
            watchlistCount = watchlistCount,
            historyCount = historyCount
        )
        return Pair(file, metadata)
    }

    fun readCompressedBackup(fileName: String): String? {
        return fileStorageClient.readCompressedString(fileName)
    }

    fun getBackupFile(fileName: String): File {
        return fileStorageClient.getFile(fileName)
    }

    fun deleteBackup(fileName: String): Boolean {
        return fileStorageClient.deleteFile(fileName)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(Locale.getDefault(), "%.1f MB", bytes.toDouble() / (1024 * 1024))
        }
    }
}
