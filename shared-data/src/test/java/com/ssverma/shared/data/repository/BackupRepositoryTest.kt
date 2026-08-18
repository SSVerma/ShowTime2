package com.ssverma.shared.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.backup.drive.GoogleDriveBackupClient
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File

class BackupRepositoryTest {

    private val mockGoogleAuthClient: GoogleAuthClient = mockk(relaxed = true)
    private val mockGoogleDriveBackupClient: GoogleDriveBackupClient = mockk(relaxed = true)
    private val mockFavoriteDao: FavoriteDao = mockk(relaxed = true)
    private val mockWatchlistDao: WatchlistDao = mockk(relaxed = true)
    private val mockWatchHistoryDao: WatchHistoryDao = mockk(relaxed = true)

    private val currentUserFlow = MutableStateFlow<GoogleUser?>(null)
    private lateinit var repository: BackupRepositoryImpl

    private var storedBackupPayload: String? = null

    @Before
    fun setUp() {
        storedBackupPayload = null
        every { mockGoogleAuthClient.currentUser } returns currentUserFlow
        every { mockGoogleDriveBackupClient.getBackupFile(any()) } returns File("/tmp/mock_backup.json.gz")

        every {
            mockGoogleDriveBackupClient.saveCompressedBackup(
                any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val fileName = firstArg<String>()
            val payload = secondArg<String>()
            val timestamp = thirdArg<Long>()
            val deviceName = arg<String>(3)
            val favCount = arg<Int>(4)
            val watchCount = arg<Int>(5)
            val histCount = arg<Int>(6)

            storedBackupPayload = payload
            val metadata = BackupMetadata(
                timestamp = timestamp,
                formattedDate = "Aug 18, 2026",
                sizeBytes = 512L,
                formattedSize = "512 B",
                deviceName = deviceName.ifBlank { "Test Device" },
                favoritesCount = favCount,
                watchlistCount = watchCount,
                historyCount = histCount
            )
            Pair(File("/tmp/$fileName"), metadata)
        }

        every { mockGoogleDriveBackupClient.readCompressedBackup(any()) } answers {
            storedBackupPayload
        }

        repository = BackupRepositoryImpl(
            googleAuthClient = mockGoogleAuthClient,
            googleDriveBackupClient = mockGoogleDriveBackupClient,
            favoriteDao = mockFavoriteDao,
            watchlistDao = mockWatchlistDao,
            watchHistoryDao = mockWatchHistoryDao
        )
    }

    @Test
    fun `backupNow saves snapshot of all entities and updates metadata`() = runTest {
        val favs = listOf(
            FavoriteEntity(
                mediaId = 1,
                mediaType = "movie",
                title = "Interstellar",
                posterImageUrl = "/interstellar.jpg",
                backdropImageUrl = "/backdrop.jpg",
                voteAvg = 9.0f,
                releaseDate = "2014-11-07"
            )
        )
        val watch = listOf(
            WatchlistEntity(
                mediaId = 2,
                mediaType = "tv",
                title = "Dark",
                posterImageUrl = "/dark.jpg",
                backdropImageUrl = "/backdrop2.jpg",
                voteAvg = 8.8f,
                releaseDate = "2017-12-01"
            )
        )
        val hist = listOf(
            WatchHistoryEntity(
                mediaId = 3,
                mediaType = "movie",
                title = "Memento",
                posterImageUrl = "/memento.jpg",
                voteAvg = 8.5f
            )
        )

        coEvery { mockFavoriteDao.getAllFavorites() } returns favs
        coEvery { mockWatchlistDao.getAllWatchlist() } returns watch
        coEvery { mockWatchHistoryDao.getAllHistory() } returns hist

        val result = repository.backupNow()

        assertThat(result.isSuccess).isTrue()
        val metadata = result.getOrNull()
        assertThat(metadata).isNotNull()
        assertThat(metadata?.favoritesCount).isEqualTo(1)
        assertThat(metadata?.watchlistCount).isEqualTo(1)
        assertThat(metadata?.historyCount).isEqualTo(1)

        assertThat(repository.lastBackupMetadata.value).isEqualTo(metadata)
        assertThat(repository.backupStatus.value).isInstanceOf(BackupStatus.Success::class.java)
    }

    @Test
    fun `restoreBackup restores all saved entities to DAOs`() = runTest {
        val favs = listOf(
            FavoriteEntity(
                mediaId = 10,
                mediaType = "movie",
                title = "Dune",
                posterImageUrl = "/dune.jpg",
                backdropImageUrl = "/dune_back.jpg",
                voteAvg = 8.2f,
                releaseDate = "2021-10-22"
            )
        )
        coEvery { mockFavoriteDao.getAllFavorites() } returns favs
        coEvery { mockWatchlistDao.getAllWatchlist() } returns emptyList()
        coEvery { mockWatchHistoryDao.getAllHistory() } returns emptyList()

        repository.backupNow()

        val restoreResult = repository.restoreBackup()

        assertThat(restoreResult.isSuccess).isTrue()
        coVerify { mockFavoriteDao.insertAll(favs) }
        assertThat(repository.backupStatus.value).isInstanceOf(BackupStatus.Success::class.java)
    }

    @Test
    fun `restoreBackup fails when no backup exists`() = runTest {
        storedBackupPayload = null

        val result = repository.restoreBackup()

        assertThat(result.isFailure).isTrue()
        assertThat(repository.backupStatus.value).isInstanceOf(BackupStatus.Error::class.java)
        val errorStatus = repository.backupStatus.value as BackupStatus.Error
        assertThat(errorStatus.operation).isEqualTo(BackupOperation.RESTORE)
    }

    @Test
    fun `signOutGoogle delegates to auth client`() = runTest {
        repository.signOutGoogle()
        coVerify { mockGoogleAuthClient.signOut() }
    }
}
