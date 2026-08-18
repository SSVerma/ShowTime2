package com.ssverma.core.backup.drive

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.storage.file.FileStorageClient
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.io.File

class GoogleDriveBackupClientTest {

    private val mockFileStorageClient: FileStorageClient = mockk(relaxed = true)
    private lateinit var backupClient: GoogleDriveBackupClient
    private var inMemoryStorage: String? = null

    @Before
    fun setUp() {
        inMemoryStorage = null
        val mockFile = mockk<File>(relaxed = true) {
            every { length() } returns 1024L
        }
        every { mockFileStorageClient.writeCompressedString(any(), any()) } answers {
            inMemoryStorage = secondArg()
            mockFile
        }
        every { mockFileStorageClient.readCompressedString(any<String>()) } answers {
            inMemoryStorage
        }
        every { mockFileStorageClient.getFile(any()) } returns mockFile
        every { mockFileStorageClient.deleteFile(any()) } answers {
            inMemoryStorage = null
            true
        }

        backupClient = GoogleDriveBackupClient(fileStorageClient = mockFileStorageClient)
    }

    @Test
    fun `saveCompressedBackup saves payload and returns computed metadata`() {
        val testPayload = """{"version":1,"timestamp":1723982400000}"""
        val (file, metadata) = backupClient.saveCompressedBackup(
            fileName = "backup.json.gz",
            jsonPayload = testPayload,
            timestamp = 1723982400000L,
            deviceName = "Google Pixel 8",
            favoritesCount = 5,
            watchlistCount = 3,
            historyCount = 2
        )

        assertThat(file).isNotNull()
        assertThat(metadata.deviceName).isEqualTo("Google Pixel 8")
        assertThat(metadata.favoritesCount).isEqualTo(5)
        assertThat(metadata.watchlistCount).isEqualTo(3)
        assertThat(metadata.historyCount).isEqualTo(2)
        assertThat(metadata.formattedDate).isNotEmpty()
        assertThat(metadata.formattedSize).isNotEmpty()
    }

    @Test
    fun `readCompressedBackup retrieves saved payload`() {
        val testPayload = """{"key":"value"}"""
        backupClient.saveCompressedBackup(
            fileName = "backup.json.gz",
            jsonPayload = testPayload,
            timestamp = 1000L,
            deviceName = "Device"
        )

        val retrieved = backupClient.readCompressedBackup("backup.json.gz")
        assertThat(retrieved).isEqualTo(testPayload)
    }

    @Test
    fun `deleteBackup clears stored backup`() {
        backupClient.saveCompressedBackup(
            fileName = "backup.json.gz",
            jsonPayload = "data",
            timestamp = 1000L,
            deviceName = "Device"
        )

        val deleted = backupClient.deleteBackup("backup.json.gz")
        assertThat(deleted).isTrue()

        val retrieved = backupClient.readCompressedBackup("backup.json.gz")
        assertThat(retrieved).isNull()
    }
}
