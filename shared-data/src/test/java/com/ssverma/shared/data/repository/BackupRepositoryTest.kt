package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.backup.drive.GoogleDriveBackupClient
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.CustomListEntity
import com.ssverma.shared.data.local.db.entity.CustomListItemEntity
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.repository.AppConfigRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File

class BackupRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockGoogleAuthClient: GoogleAuthClient = mockk(relaxed = true)
    private val mockGoogleDriveBackupClient: GoogleDriveBackupClient = mockk(relaxed = true)
    private val mockFavoriteDao: FavoriteDao = mockk(relaxed = true)
    private val mockWatchlistDao: WatchlistDao = mockk(relaxed = true)
    private val mockWatchHistoryDao: WatchHistoryDao = mockk(relaxed = true)
    private val mockCustomListDao: CustomListDao = mockk(relaxed = true)
    private val mockAppConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)

    private val currentUserFlow = MutableStateFlow<GoogleUser?>(null)
    private lateinit var repository: BackupRepositoryImpl

    private var storedBackupPayload: String? = null

    @Before
    fun setUp() {
        storedBackupPayload = null
        every { mockGoogleAuthClient.currentUser } returns currentUserFlow
        every { mockGoogleDriveBackupClient.getBackupFile(any()) } returns File("/tmp/mock_backup.json.gz")
        every { mockKeyValueStorageClient.createKeyValueStorage(any(), any()) } returns mockStorage
        every { mockStorage.data } returns flowOf(emptyPreferences())
        every { mockAppConfigRepository.appTheme } returns flowOf(AppTheme.System)
        every { mockAppConfigRepository.watchProviderRegion } returns MutableStateFlow("US")

        every {
            mockGoogleDriveBackupClient.saveCompressedBackup(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
            )
        } answers {
            val fileName = firstArg<String>()
            val payload = secondArg<String>()
            val timestamp = thirdArg<Long>()
            val deviceName = arg<String>(3)
            val favCount = arg<Int>(4)
            val watchCount = arg<Int>(5)
            val histCount = arg<Int>(6)
            val listCount = arg<Int>(7)
            val listItemCount = arg<Int>(8)

            storedBackupPayload = payload
            val metadata = BackupMetadata(
                timestamp = timestamp,
                formattedDate = "Aug 18, 2026",
                sizeBytes = 512L,
                formattedSize = "512 B",
                deviceName = deviceName.ifBlank { "Test Device" },
                favoritesCount = favCount,
                watchlistCount = watchCount,
                historyCount = histCount,
                customListsCount = listCount,
                customListItemsCount = listItemCount
            )
            Pair(File("/tmp/$fileName"), metadata)
        }

        every { mockGoogleDriveBackupClient.readCompressedBackup(any()) } answers {
            storedBackupPayload
        }

        repository = BackupRepositoryImpl(
            context = mockContext,
            googleAuthClient = mockGoogleAuthClient,
            googleDriveBackupClient = mockGoogleDriveBackupClient,
            favoriteDao = mockFavoriteDao,
            watchlistDao = mockWatchlistDao,
            watchHistoryDao = mockWatchHistoryDao,
            customListDao = mockCustomListDao,
            appConfigRepository = mockAppConfigRepository,
            keyValueStorageClient = mockKeyValueStorageClient
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
        val lists = listOf(
            CustomListEntity(
                listId = "list_1",
                title = "Marvel Cinematic",
                description = "MCU Phase 1-5"
            )
        )
        val listItems = listOf(
            CustomListItemEntity(
                listId = "list_1",
                mediaId = 100,
                mediaType = "movie",
                title = "Iron Man",
                posterImageUrl = "/ironman.jpg"
            )
        )

        coEvery { mockFavoriteDao.getAllFavorites() } returns favs
        coEvery { mockWatchlistDao.getAllWatchlist() } returns watch
        coEvery { mockWatchHistoryDao.getAllHistory() } returns hist
        coEvery { mockCustomListDao.getAllLists() } returns lists
        coEvery { mockCustomListDao.getAllListItems() } returns listItems

        val result = repository.backupNow()

        assertThat(result.isSuccess).isTrue()
        val metadata = result.getOrNull()
        assertThat(metadata).isNotNull()
        assertThat(metadata?.favoritesCount).isEqualTo(1)
        assertThat(metadata?.watchlistCount).isEqualTo(1)
        assertThat(metadata?.historyCount).isEqualTo(1)
        assertThat(metadata?.customListsCount).isEqualTo(1)
        assertThat(metadata?.customListItemsCount).isEqualTo(1)

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
        val lists = listOf(
            CustomListEntity(
                listId = "custom_1",
                title = "Sci-Fi Favorites"
            )
        )
        val listItems = listOf(
            CustomListItemEntity(
                listId = "custom_1",
                mediaId = 10,
                mediaType = "movie",
                title = "Dune",
                posterImageUrl = "/dune.jpg"
            )
        )

        coEvery { mockFavoriteDao.getAllFavorites() } returns favs
        coEvery { mockWatchlistDao.getAllWatchlist() } returns emptyList()
        coEvery { mockWatchHistoryDao.getAllHistory() } returns emptyList()
        coEvery { mockCustomListDao.getAllLists() } returns lists
        coEvery { mockCustomListDao.getAllListItems() } returns listItems

        repository.backupNow()

        val restoreResult = repository.restoreBackup()

        assertThat(restoreResult.isSuccess).isTrue()
        coVerify { mockFavoriteDao.insertAll(favs) }
        coVerify { mockCustomListDao.insertAllLists(lists) }
        coVerify { mockCustomListDao.insertAllListItems(listItems) }
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
