package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.backup.drive.GoogleDriveBackupClient
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.dao.DiaryDao
import com.ssverma.shared.data.local.db.dao.EpisodeWatchHistoryDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.CustomListEntity
import com.ssverma.shared.data.local.db.entity.CustomListItemEntity
import com.ssverma.shared.data.local.db.entity.DiaryEntryEntity
import com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.BacklogRepository
import com.ssverma.shared.domain.repository.CinemaGameRepository
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
    private val mockDiaryDao: DiaryDao = mockk(relaxed = true)
    private val mockShowWatchProgressDao: ShowWatchProgressDao = mockk(relaxed = true)
    private val mockEpisodeWatchHistoryDao: EpisodeWatchHistoryDao = mockk(relaxed = true)
    private val mockBacklogRepository: BacklogRepository = mockk(relaxed = true)
    private val mockCinemaGameRepository: CinemaGameRepository = mockk(relaxed = true)
    private val mockAppConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val mockFirestore: FirebaseFirestore = mockk(relaxed = true)
    private val mockCollection: CollectionReference = mockk(relaxed = true)
    private val mockDocument: DocumentReference = mockk(relaxed = true)
    private val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)

    private val currentUserFlow = MutableStateFlow<GoogleUser?>(null)
    private val preferencesFlow =
        MutableStateFlow<androidx.datastore.preferences.core.Preferences>(emptyPreferences())
    private lateinit var repository: BackupRepositoryImpl

    private var storedBackupPayload: String? = null

    @Before
    fun setUp() {
        storedBackupPayload = null
        preferencesFlow.value = emptyPreferences()
        every { mockGoogleAuthClient.currentUser } returns currentUserFlow
        coEvery { mockGoogleAuthClient.getEffectiveUserId() } returns "test_user_123"
        every { mockGoogleDriveBackupClient.getBackupFile(any()) } returns File("/tmp/mock_backup.json.gz")
        every { mockKeyValueStorageClient.createKeyValueStorage(any(), any()) } returns mockStorage
        every { mockStorage.data } returns preferencesFlow
        coEvery { mockStorage.updateData(any()) } coAnswers {
            val transform =
                firstArg<suspend (androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences>()
            val updated = transform(preferencesFlow.value)
            preferencesFlow.value = updated
            updated
        }
        every { mockAppConfigRepository.appTheme } returns flowOf(AppTheme.System)
        every { mockAppConfigRepository.watchProviderRegion } returns MutableStateFlow("US")
        every { mockBacklogRepository.activeChallengesFlow } returns flowOf(emptyList())
        every { mockBacklogRepository.blindspotsFlow } returns flowOf(emptyList())

        every { mockSnapshot.exists() } returns false
        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument
        every { mockDocument.set(any(), any<SetOptions>()) } returns Tasks.forResult(null)
        every { mockDocument.get() } returns Tasks.forResult(mockSnapshot)

        every {
            mockGoogleDriveBackupClient.saveCompressedBackup(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
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
            val diaryCount = arg<Int>(9)
            val showCount = arg<Int>(10)
            val epCount = arg<Int>(11)
            val challengeCount = arg<Int>(12)

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
                customListItemsCount = listItemCount,
                diaryEntriesCount = diaryCount,
                showProgressCount = showCount,
                episodeHistoryCount = epCount,
                challengesCount = challengeCount
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
            diaryDao = mockDiaryDao,
            showWatchProgressDao = mockShowWatchProgressDao,
            episodeWatchHistoryDao = mockEpisodeWatchHistoryDao,
            backlogRepository = mockBacklogRepository,
            cinemaGameRepository = mockCinemaGameRepository,
            appConfigRepository = mockAppConfigRepository,
            firestore = mockFirestore,
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

        val diary = listOf(
            DiaryEntryEntity(
                id = 1L,
                mediaId = 1,
                mediaType = "movie",
                title = "Interstellar",
                posterImageUrl = "/interstellar.jpg",
                userRating = 5.0f
            )
        )
        val showProgress = listOf(
            ShowWatchProgressEntity(
                showId = 2,
                showTitle = "Dark",
                showPosterPath = "/dark.jpg",
                seasonNumber = 1,
                episodeNumber = 5,
                episodeTitle = "Truths",
                totalCompleted = 5,
                totalAired = 26
            )
        )
        val epHistory = listOf(
            EpisodeWatchHistoryEntity(
                showId = 2,
                seasonNumber = 1,
                episodeNumber = 5
            )
        )

        coEvery { mockFavoriteDao.getAllFavorites() } returns favs
        coEvery { mockWatchlistDao.getAllWatchlist() } returns watch
        coEvery { mockWatchHistoryDao.getAllHistory() } returns hist
        coEvery { mockCustomListDao.getAllLists() } returns lists
        coEvery { mockCustomListDao.getAllListItems() } returns listItems
        coEvery { mockDiaryDao.getAllDiaryEntriesList() } returns diary
        coEvery { mockShowWatchProgressDao.getAllProgress() } returns showProgress
        coEvery { mockEpisodeWatchHistoryDao.getAllHistory() } returns epHistory
        coEvery { mockCinemaGameRepository.getGameStats() } returns CinemaGameStats(
            gamesWon = 3,
            gamesPlayed = 5
        )

        val result = repository.backupNow()

        assertThat(result.isSuccess).isTrue()
        val metadata = result.getOrNull()
        assertThat(metadata).isNotNull()
        assertThat(metadata?.favoritesCount).isEqualTo(1)
        assertThat(metadata?.watchlistCount).isEqualTo(1)
        assertThat(metadata?.historyCount).isEqualTo(1)
        assertThat(metadata?.customListsCount).isEqualTo(1)
        assertThat(metadata?.customListItemsCount).isEqualTo(1)
        assertThat(metadata?.diaryEntriesCount).isEqualTo(1)
        assertThat(metadata?.showProgressCount).isEqualTo(1)
        assertThat(metadata?.episodeHistoryCount).isEqualTo(1)

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
        val diary = listOf(
            DiaryEntryEntity(
                id = 10L,
                mediaId = 10,
                mediaType = "movie",
                title = "Dune",
                posterImageUrl = "/dune.jpg",
                userRating = 4.5f
            )
        )
        val stats = CinemaGameStats(gamesWon = 7, gamesPlayed = 10)

        coEvery { mockFavoriteDao.getAllFavorites() } returns favs
        coEvery { mockWatchlistDao.getAllWatchlist() } returns emptyList()
        coEvery { mockWatchHistoryDao.getAllHistory() } returns emptyList()
        coEvery { mockCustomListDao.getAllLists() } returns lists
        coEvery { mockCustomListDao.getAllListItems() } returns listItems
        coEvery { mockDiaryDao.getAllDiaryEntriesList() } returns diary
        coEvery { mockShowWatchProgressDao.getAllProgress() } returns emptyList()
        coEvery { mockEpisodeWatchHistoryDao.getAllHistory() } returns emptyList()
        coEvery { mockCinemaGameRepository.getGameStats() } returns stats

        repository.backupNow()

        val restoreResult = repository.restoreBackup()

        assertThat(restoreResult.isSuccess).isTrue()
        coVerify { mockFavoriteDao.insertAll(favs) }
        coVerify { mockCustomListDao.insertAllLists(lists) }
        coVerify { mockCustomListDao.insertAllListItems(listItems) }
        coVerify { mockDiaryDao.insertAll(diary) }
        coVerify { mockCinemaGameRepository.restoreGameStats(stats) }
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

    @Test
    fun `backupNow skips Firestore set when payload hash is unchanged`() = runTest {
        coEvery { mockFavoriteDao.getAllFavorites() } returns emptyList()
        coEvery { mockWatchlistDao.getAllWatchlist() } returns emptyList()
        coEvery { mockWatchHistoryDao.getAllHistory() } returns emptyList()
        coEvery { mockCustomListDao.getAllLists() } returns emptyList()
        coEvery { mockCustomListDao.getAllListItems() } returns emptyList()
        coEvery { mockDiaryDao.getAllDiaryEntriesList() } returns emptyList()
        coEvery { mockShowWatchProgressDao.getAllProgress() } returns emptyList()
        coEvery { mockEpisodeWatchHistoryDao.getAllHistory() } returns emptyList()
        coEvery { mockCinemaGameRepository.getGameStats() } returns CinemaGameStats()

        // First backup uploads to Firestore
        val firstResult = repository.backupNow()
        assertThat(firstResult.isSuccess).isTrue()
        coVerify(exactly = 1) { mockDocument.set(any(), any<SetOptions>()) }

        // Second backup with identical data skips Firestore write (SHA-256 cost gate)
        val secondResult = repository.backupNow()
        assertThat(secondResult.isSuccess).isTrue()
        coVerify(exactly = 1) { mockDocument.set(any(), any<SetOptions>()) }
    }
}
