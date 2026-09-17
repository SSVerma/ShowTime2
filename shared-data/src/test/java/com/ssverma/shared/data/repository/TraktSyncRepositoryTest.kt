package com.ssverma.shared.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.data.remote.TraktEpisodePayload
import com.ssverma.shared.data.remote.TraktIds
import com.ssverma.shared.data.remote.TraktShowPayload
import com.ssverma.shared.data.remote.TraktShowProgressPayload
import com.ssverma.shared.data.remote.TraktSyncService
import com.ssverma.shared.data.remote.TraktWatchlistItemPayload
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TraktSyncRepositoryTest {

    private val mockTraktSyncService: TraktSyncService = mockk(relaxed = true)
    private val mockWatchlistDao: WatchlistDao = mockk(relaxed = true)
    private val mockWatchHistoryDao: WatchHistoryDao = mockk(relaxed = true)
    private val mockFavoriteDao: FavoriteDao = mockk(relaxed = true)
    private val mockEpisodeWatchHistoryDao: com.ssverma.shared.data.local.db.dao.EpisodeWatchHistoryDao =
        mockk(relaxed = true)
    private val mockShowWatchProgressDao: com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao =
        mockk(relaxed = true)
    private val mockDebugConfigManager: com.ssverma.shared.data.debug.DebugConfigManager =
        mockk(relaxed = true)
    private val mockTraktDataSource = com.ssverma.shared.data.local.mock.MockTraktDataSource()

    private val isMockTraktFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
    private val customClientIdFlow = kotlinx.coroutines.flow.MutableStateFlow("")

    private lateinit var repository: TraktSyncRepositoryImpl

    @Before
    fun setUp() {
        io.mockk.coEvery { mockDebugConfigManager.isMockTraktEnabled } returns isMockTraktFlow
        io.mockk.coEvery { mockDebugConfigManager.customTraktClientId } returns customClientIdFlow

        repository = TraktSyncRepositoryImpl(
            traktSyncService = mockTraktSyncService,
            watchlistDao = mockWatchlistDao,
            watchHistoryDao = mockWatchHistoryDao,
            favoriteDao = mockFavoriteDao,
            episodeWatchHistoryDao = mockEpisodeWatchHistoryDao,
            showWatchProgressDao = mockShowWatchProgressDao,
            debugConfigManager = mockDebugConfigManager,
            mockTraktDataSource = mockTraktDataSource
        )
    }

    @Test
    fun `syncLibrary imports remote items into Room DAOs and exports local items`() = runTest {
        val traktWatchlist = listOf(
            TraktWatchlistItemPayload(
                type = "show",
                listedAt = "2026-08-20T00:00:00Z",
                movie = null,
                show = TraktShowPayload(
                    title = "Breaking Bad",
                    year = 2008,
                    ids = TraktIds(trakt = 1388, tmdb = 1396)
                )
            )
        )

        coEvery {
            mockTraktSyncService.getWatchlist(any(), any())
        } returns ApiResponse.Success(body = traktWatchlist, payload = mockk(relaxed = true))

        coEvery {
            mockTraktSyncService.getHistory(any(), any(), any())
        } returns ApiResponse.Success(body = emptyList(), payload = mockk(relaxed = true))

        coEvery { mockWatchlistDao.getAllWatchlist() } returns listOf(
            WatchlistEntity(
                mediaId = 1396,
                mediaType = "Tv",
                title = "Breaking Bad",
                posterImageUrl = "",
                backdropImageUrl = "",
                voteAvg = 9.5f,
                releaseDate = ""
            ),
            WatchlistEntity(
                mediaId = 550,
                mediaType = "Movie",
                title = "Fight Club",
                posterImageUrl = "",
                backdropImageUrl = "",
                voteAvg = 8.8f,
                releaseDate = ""
            )
        )

        val result = repository.syncLibrary(accessToken = "mock_token")

        assertThat(result.isSuccess).isTrue()
        val syncResult = result.getOrNull()
        assertThat(syncResult).isNotNull()
        assertThat(syncResult?.itemsImportedToWatchlist).isEqualTo(1)

        coVerify { mockWatchlistDao.insertAll(any()) }
        coVerify { mockTraktSyncService.addToWatchlist(any(), any(), any()) }
    }

    @Test
    fun `getUpNextQueue parses uncompleted episodes correctly`() = runTest {
        val watchedShows = listOf(
            TraktShowProgressPayload(
                aired = 10,
                completed = 5,
                lastWatchedAt = "2026-08-19T00:00:00Z",
                show = TraktShowPayload(
                    title = "Severance",
                    year = 2022,
                    ids = TraktIds(trakt = 1234, tmdb = 93405)
                ),
                nextEpisode = TraktEpisodePayload(
                    season = 1,
                    number = 6,
                    title = "Hide and Seek",
                    ids = TraktIds(trakt = 5678, tmdb = 9988)
                )
            )
        )

        coEvery {
            mockTraktSyncService.getWatchedShowProgress(any(), any())
        } returns ApiResponse.Success(body = watchedShows, payload = mockk(relaxed = true))

        val result = repository.getUpNextQueue(accessToken = "mock_token")

        assertThat(result.isSuccess).isTrue()
        val upNextList = result.getOrNull()
        assertThat(upNextList).isNotNull()
        assertThat(upNextList).hasSize(1)

        val episode = upNextList!!.first()
        assertThat(episode.showTmdbId).isEqualTo(93405)
        assertThat(episode.showTitle).isEqualTo("Severance")
        assertThat(episode.seasonNumber).isEqualTo(1)
        assertThat(episode.episodeNumber).isEqualTo(6)
        assertThat(episode.episodeTitle).isEqualTo("Hide and Seek")
        assertThat(episode.progressPercentage).isEqualTo(0.5f)
    }

    @Test
    fun `markEpisodeWatched delegates to sync service`() = runTest {
        coEvery {
            mockTraktSyncService.addToHistory(any(), any(), any())
        } returns ApiResponse.Success(body = mockk(relaxed = true), payload = mockk(relaxed = true))

        val result = repository.markEpisodeWatched(
            accessToken = "mock_token",
            showTmdbId = 1396,
            season = 2,
            episode = 5
        )

        assertThat(result.isSuccess).isTrue()
        coVerify {
            mockTraktSyncService.addToHistory(
                bearerToken = "Bearer mock_token",
                clientId = any(),
                payload = any()
            )
        }
    }

    @Test
    fun `mock mode markEpisodeWatched advances episode number and total completed`() = runTest {
        isMockTraktFlow.value = true

        val initialQueue = repository.getUpNextQueue("mock_token").getOrThrow()
        val hotdInitial = initialQueue.first { it.showTmdbId == 94997 }
        assertThat(hotdInitial.seasonNumber).isEqualTo(2)
        assertThat(hotdInitial.episodeNumber).isEqualTo(5)
        assertThat(hotdInitial.episodeTitle).isEqualTo("Regent")
        assertThat(hotdInitial.totalCompleted).isEqualTo(4)

        repository.markEpisodeWatched(
            accessToken = "mock_token",
            showTmdbId = 94997,
            season = 2,
            episode = 5
        )

        val updatedQueue = repository.getUpNextQueue("mock_token").getOrThrow()
        val hotdUpdated = updatedQueue.first { it.showTmdbId == 94997 }
        assertThat(hotdUpdated.seasonNumber).isEqualTo(2)
        assertThat(hotdUpdated.episodeNumber).isEqualTo(6)
        assertThat(hotdUpdated.episodeTitle).isEqualTo("Smallfolk")
        assertThat(hotdUpdated.totalCompleted).isEqualTo(5)
    }

    @Test
    fun `markEpisodeWatched non-linear resolves next unwatched episode in active session sequence`() =
        runTest {
            isMockTraktFlow.value = false

            // Given show with S1E1 and S1E3 watched (user just watched S1E3)
            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(500, 1, 3) } returns false
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(500) } returns listOf(
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(500, 1, 3, 2000L),
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(500, 1, 1, 1000L)
            )

            val progressSlot =
                io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
            coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 500,
                season = 1,
                episode = 3,
                showTitle = "The Mentalist",
                totalAired = 151
            )

            // Then Up Next resolves to S1E4 (continuing the active viewing session forward)
            assertThat(progressSlot.isCaptured).isTrue()
            val captured = progressSlot.captured
            assertThat(captured.seasonNumber).isEqualTo(1)
            assertThat(captured.episodeNumber).isEqualTo(4)
            assertThat(captured.totalCompleted).isEqualTo(2)
            assertThat(captured.totalAired).isEqualTo(151)
        }

    @Test
    fun `markSeasonWatched advances to next season without deleting multi-season show`() = runTest {
        isMockTraktFlow.value = false

        // Given Season 5 (22 episodes) marked watched out of a 151-episode show
        val season5Episodes = (1..22).map {
            com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(500, 5, it, 1000L)
        }
        coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(500) } returns season5Episodes

        val progressSlot =
            io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
        coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

        repository.markSeasonWatched(
            accessToken = null,
            showTmdbId = 500,
            season = 5,
            episodeNumbers = (1..22).toList(),
            showTitle = "The Mentalist",
            totalAired = 151
        )

        // Then Up Next points to Season 6, Episode 1
        assertThat(progressSlot.isCaptured).isTrue()
        val captured = progressSlot.captured
        assertThat(captured.seasonNumber).isEqualTo(6)
        assertThat(captured.episodeNumber).isEqualTo(1)
        assertThat(captured.totalCompleted).isEqualTo(22)
        assertThat(captured.totalAired).isEqualTo(151)
    }

    @Test
    fun `markEpisodeWatched preserves totalAired denominator without incrementing it`() = runTest {
        isMockTraktFlow.value = false

        // Existing progress is 6/9 eps
        coEvery { mockShowWatchProgressDao.getProgress(93405) } returns
                com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                    showId = 93405,
                    showTitle = "Severance",
                    showPosterPath = null,
                    seasonNumber = 2,
                    episodeNumber = 1,
                    episodeTitle = "Hello, Innie",
                    totalCompleted = 6,
                    totalAired = 9,
                    lastWatchedAt = 1000L
                )

        // After marking 7th episode
        coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 1) } returns false
        coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns (1..7).map {
            com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(93405, 1, it, 1000L)
        }

        val progressSlot =
            io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
        coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

        repository.markEpisodeWatched(
            accessToken = null,
            showTmdbId = 93405,
            season = 2,
            episode = 1,
            showTitle = "Severance",
            totalAired = 9
        )

        // totalCompleted must be 7, and totalAired must stay 9 (not 10 or 8)
        assertThat(progressSlot.isCaptured).isTrue()
        val captured = progressSlot.captured
        assertThat(captured.totalCompleted).isEqualTo(7)
        assertThat(captured.totalAired).isEqualTo(9)
    }

    @Test
    fun `markEpisodeWatched unmarking episode decreases completed count and preserves totalAired`() =
        runTest {
            isMockTraktFlow.value = false

            // Existing progress: 7/9 eps
            coEvery { mockShowWatchProgressDao.getProgress(93405) } returns
                    com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                        showId = 93405,
                        showTitle = "Severance",
                        showPosterPath = null,
                        seasonNumber = 2,
                        episodeNumber = 2,
                        episodeTitle = "Goodbye, Mrs. Selvig",
                        totalCompleted = 7,
                        totalAired = 9,
                        lastWatchedAt = 1000L
                    )

            // Episode is currently watched -> will be deleted
            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 1) } returns true
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns (1..6).map {
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    1,
                    it,
                    1000L
                )
            }

            val progressSlot =
                io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
            coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 93405,
                season = 2,
                episode = 1,
                showTitle = "Severance",
                totalAired = 9
            )

            // Verifies deletion and updated progress: totalCompleted = 6, totalAired = 9
            io.mockk.coVerify { mockEpisodeWatchHistoryDao.deleteEpisode(93405, 2, 1) }
            assertThat(progressSlot.isCaptured).isTrue()
            val captured = progressSlot.captured
            assertThat(captured.totalCompleted).isEqualTo(6)
            assertThat(captured.totalAired).isEqualTo(9)
        }

    @Test
    fun `getUpNextQueueFlow emits progress list mapped to TraktUpNextEpisode`() = runTest {
        isMockTraktFlow.value = false

        val progressList = listOf(
            com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                showId = 93405,
                showTitle = "Severance",
                showPosterPath = "poster.jpg",
                seasonNumber = 2,
                episodeNumber = 1,
                episodeTitle = "Hello, Innie",
                totalCompleted = 6,
                totalAired = 9,
                lastWatchedAt = 1000L
            )
        )
        coEvery { mockShowWatchProgressDao.getUpNextQueueFlow() } returns kotlinx.coroutines.flow.flowOf(
            progressList
        )

        val flow = repository.getUpNextQueueFlow("token")
        flow.collect { list ->
            assertThat(list).hasSize(1)
            val item = list.first()
            assertThat(item.showTmdbId).isEqualTo(93405)
            assertThat(item.showTitle).isEqualTo("Severance")
            assertThat(item.totalCompleted).isEqualTo(6)
            assertThat(item.totalAired).isEqualTo(9)
            assertThat(item.progressPercentage).isWithin(0.01f).of(6f / 9f)
        }
    }

    @Test
    fun `markEpisodeWatched resolves next episode title from mock catalog or service`() = runTest {
        isMockTraktFlow.value = false

        // Existing progress is S2E1 ("Hello, Innie")
        coEvery { mockShowWatchProgressDao.getProgress(93405) } returns
                com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                    showId = 93405,
                    showTitle = "Severance",
                    showPosterPath = null,
                    seasonNumber = 2,
                    episodeNumber = 1,
                    episodeTitle = "Hello, Innie",
                    totalCompleted = 6,
                    totalAired = 9,
                    lastWatchedAt = 1000L
                )

        coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 1) } returns false
        coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns listOf(
            com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(93405, 2, 1, 1000L)
        )

        val progressSlot =
            io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
        coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

        repository.markEpisodeWatched(
            accessToken = null,
            showTmdbId = 93405,
            season = 2,
            episode = 1,
            showTitle = "Severance",
            totalAired = 9
        )

        assertThat(progressSlot.isCaptured).isTrue()
        val captured = progressSlot.captured
        assertThat(captured.seasonNumber).isEqualTo(2)
        assertThat(captured.episodeNumber).isEqualTo(2)
        // Resolves S2E2 title from catalog ("Goodbye, Mrs. Selvig"), not stale "Hello, Innie"
        assertThat(captured.episodeTitle).isEqualTo("Goodbye, Mrs. Selvig")
    }

    @Test
    fun `multi-season tracking continues active season forward when user is watching Season 2`() =
        runTest {
            isMockTraktFlow.value = false

            // Given show with S1 (10 eps) and S2 (9 eps) -> Total 19 eps
            // User watched S1E1, S1E2, S1E3 and S2E1, S2E2 (Total completed = 5)
            // Latest watched episode is S2E2
            val watchedList = listOf(
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    2,
                    2,
                    2000L
                ),
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    2,
                    1,
                    1500L
                ),
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    1,
                    3,
                    1000L
                ),
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    1,
                    2,
                    1000L
                ),
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    1,
                    1,
                    1000L
                )
            )

            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 2) } returns false
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns watchedList
            coEvery { mockShowWatchProgressDao.getProgress(93405) } returns
                    com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                        showId = 93405,
                        showTitle = "Severance",
                        showPosterPath = null,
                        seasonNumber = 2,
                        episodeNumber = 1,
                        episodeTitle = "Hello, Innie",
                        seasonCompleted = 1,
                        seasonTotalAired = 9,
                        totalCompleted = 4,
                        totalAired = 19,
                        lastWatchedAt = 1000L
                    )

            val progressSlot =
                io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
            coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 93405,
                season = 2,
                episode = 2,
                showTitle = "Severance",
                totalAired = 19
            )

            // Then Up Next reliably points to Season 2, Episode 3 (continuing the active viewing session in Season 2)
            assertThat(progressSlot.isCaptured).isTrue()
            val captured = progressSlot.captured
            assertThat(captured.seasonNumber).isEqualTo(2)
            assertThat(captured.episodeNumber).isEqualTo(3)
            assertThat(captured.seasonCompleted).isEqualTo(2)
            assertThat(captured.totalCompleted).isEqualTo(5)
            assertThat(captured.totalAired).isEqualTo(19)
        }

    @Test
    fun `markEpisodeWatched starting mid-series in Season 2 advances to S02E02 instead of S01E01`() =
        runTest {
            isMockTraktFlow.value = false

            // Given user starts watching directly at Season 2 Episode 1 (Season 1 completely unwatched)
            val watchedList = listOf(
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    2,
                    1,
                    1000L
                )
            )

            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 1) } returns false
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns watchedList
            coEvery { mockShowWatchProgressDao.getProgress(93405) } returns null

            val progressSlot =
                io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
            coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 93405,
                season = 2,
                episode = 1,
                showTitle = "Severance",
                totalAired = 19
            )

            // Then Up Next resolves to Season 2 Episode 2 (NOT resetting back to S01E01)
            assertThat(progressSlot.isCaptured).isTrue()
            val captured = progressSlot.captured
            assertThat(captured.seasonNumber).isEqualTo(2)
            assertThat(captured.episodeNumber).isEqualTo(2)
            assertThat(captured.seasonCompleted).isEqualTo(1)
            assertThat(captured.totalCompleted).isEqualTo(1)
            assertThat(captured.totalAired).isEqualTo(19)
        }

    @Test
    fun `markEpisodeWatched finishing latest season falls back to earliest unwatched backlog episode`() =
        runTest {
            isMockTraktFlow.value = false

            // Given: Season 1 has S1E1, S1E2, S1E3 watched (S1E4..S1E10 unwatched = 7 backlog eps)
            // Season 2 has all 9 episodes watched (S2E1..S2E9). User just finished S2E9 finale.
            val season1Watched = (1..3).map {
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    1,
                    it,
                    1000L
                )
            }
            val season2Watched = (1..9).map {
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    2,
                    it,
                    2000L + it
                )
            }
            val allWatched = season2Watched.reversed() + season1Watched

            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 9) } returns false
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns allWatched
            coEvery { mockShowWatchProgressDao.getProgress(93405) } returns
                    com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                        showId = 93405,
                        showTitle = "Severance",
                        showPosterPath = null,
                        seasonNumber = 2,
                        episodeNumber = 8,
                        episodeTitle = "Sweet Vitriol",
                        seasonCompleted = 8,
                        seasonTotalAired = 9,
                        totalCompleted = 11,
                        totalAired = 19,
                        lastWatchedAt = 1000L
                    )

            val progressSlot =
                io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
            coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 93405,
                season = 2,
                episode = 9,
                showTitle = "Severance",
                totalAired = 19
            )

            // Then Up Next falls back to earliest backlog episode: Season 1 Episode 4!
            assertThat(progressSlot.isCaptured).isTrue()
            val captured = progressSlot.captured
            assertThat(captured.seasonNumber).isEqualTo(1)
            assertThat(captured.episodeNumber).isEqualTo(4)
            assertThat(captured.seasonCompleted).isEqualTo(3)
            assertThat(captured.totalCompleted).isEqualTo(12)
            assertThat(captured.totalAired).isEqualTo(19)
        }

    @Test
    fun `multi-season tracking advances to Season 2 when all Season 1 episodes are completed`() =
        runTest {
            isMockTraktFlow.value = false

            // Given all 10 episodes of Season 1 watched + 2 episodes of Season 2 watched = 12 completed
            val watchedList = (1..10).map {
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    1,
                    it,
                    1000L
                )
            } + listOf(
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    2,
                    1,
                    1000L
                ),
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    2,
                    2,
                    1000L
                )
            )

            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 1, 10) } returns false
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns watchedList
            coEvery { mockShowWatchProgressDao.getProgress(93405) } returns
                    com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                        showId = 93405,
                        showTitle = "Severance",
                        showPosterPath = null,
                        seasonNumber = 1,
                        episodeNumber = 9,
                        episodeTitle = "The We We Are",
                        seasonCompleted = 9,
                        seasonTotalAired = 10,
                        totalCompleted = 11,
                        totalAired = 19,
                        lastWatchedAt = 1000L
                    )

            val progressSlot =
                io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
            coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 93405,
                season = 1,
                episode = 10,
                showTitle = "Severance",
                totalAired = 19
            )

            // Then Up Next advances to Season 2, Episode 3
            assertThat(progressSlot.isCaptured).isTrue()
            val captured = progressSlot.captured
            assertThat(captured.seasonNumber).isEqualTo(2)
            assertThat(captured.episodeNumber).isEqualTo(3)
            assertThat(captured.seasonCompleted).isEqualTo(2)
            assertThat(captured.totalCompleted).isEqualTo(12)
            assertThat(captured.totalAired).isEqualTo(19)
        }

    @Test
    fun `markEpisodeWatched starting mid-series in Season 3 Episode 5 advances to S03E06`() =
        runTest {
            isMockTraktFlow.value = false

            // Given user starts watching a 62-episode show directly at Season 3 Episode 5
            val watchedList = listOf(
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    1396,
                    3,
                    5,
                    1000L
                )
            )

            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(1396, 3, 5) } returns false
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(1396) } returns watchedList
            coEvery { mockShowWatchProgressDao.getProgress(1396) } returns null

            val progressSlot =
                io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
            coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 1396,
                season = 3,
                episode = 5,
                showTitle = "Breaking Bad",
                totalAired = 62
            )

            // Then Up Next advances to Season 3, Episode 6 (NOT resetting to S01E01)
            assertThat(progressSlot.isCaptured).isTrue()
            val captured = progressSlot.captured
            assertThat(captured.seasonNumber).isEqualTo(3)
            assertThat(captured.episodeNumber).isEqualTo(6)
            assertThat(captured.seasonCompleted).isEqualTo(1)
            assertThat(captured.totalCompleted).isEqualTo(1)
            assertThat(captured.totalAired).isEqualTo(62)
        }

    @Test
    fun `markEpisodeWatched completing entire series marks show 100 percent caught up`() = runTest {
        isMockTraktFlow.value = false

        // Given all 10 eps of S1 + all 9 eps of S2 watched = 19 total eps
        val allWatched = (1..10).map {
            com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(93405, 1, it, 1000L)
        } + (1..9).map {
            com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                93405,
                2,
                it,
                2000L + it
            )
        }

        coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 9) } returns false
        coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns allWatched.reversed()
        coEvery { mockShowWatchProgressDao.getProgress(93405) } returns
                com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                    showId = 93405,
                    showTitle = "Severance",
                    showPosterPath = null,
                    seasonNumber = 2,
                    episodeNumber = 8,
                    episodeTitle = "Sweet Vitriol",
                    seasonCompleted = 8,
                    seasonTotalAired = 9,
                    totalCompleted = 18,
                    totalAired = 19,
                    lastWatchedAt = 1000L
                )

        val progressSlot =
            io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
        coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

        repository.markEpisodeWatched(
            accessToken = null,
            showTmdbId = 93405,
            season = 2,
            episode = 9,
            showTitle = "Severance",
            totalAired = 19
        )

        // Then Up Next marks series as 100% completed
        assertThat(progressSlot.isCaptured).isTrue()
        val captured = progressSlot.captured
        assertThat(captured.seasonNumber).isEqualTo(2)
        assertThat(captured.episodeNumber).isEqualTo(9)
        assertThat(captured.totalCompleted).isEqualTo(19)
        assertThat(captured.totalAired).isEqualTo(19)
        assertThat(captured.seasonCompleted).isEqualTo(9)
        assertThat(captured.seasonTotalAired).isEqualTo(9)
    }

    @Test
    fun `markEpisodeWatched unmarking episode rewinds anchor to previous watched episode in history`() =
        runTest {
            isMockTraktFlow.value = false

            // Given user had watched S2E1 (t=1000) and S2E2 (t=2000), and now unmarks S2E2
            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 2) } returns true
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns listOf(
                com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                    93405,
                    2,
                    1,
                    1000L
                )
            )
            coEvery { mockShowWatchProgressDao.getProgress(93405) } returns
                    com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity(
                        showId = 93405,
                        showTitle = "Severance",
                        showPosterPath = null,
                        seasonNumber = 2,
                        episodeNumber = 3,
                        episodeTitle = "Who Is Alive?",
                        seasonCompleted = 2,
                        seasonTotalAired = 9,
                        totalCompleted = 2,
                        totalAired = 19,
                        lastWatchedAt = 2000L
                    )

            val progressSlot =
                io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
            coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 93405,
                season = 2,
                episode = 2,
                showTitle = "Severance",
                totalAired = 19
            )

            // Then Up Next rewinds to S2E2
            assertThat(progressSlot.isCaptured).isTrue()
            val captured = progressSlot.captured
            assertThat(captured.seasonNumber).isEqualTo(2)
            assertThat(captured.episodeNumber).isEqualTo(2)
            assertThat(captured.totalCompleted).isEqualTo(1)
            assertThat(captured.totalAired).isEqualTo(19)
        }

    @Test
    fun `markEpisodeWatched unmarking all episodes deletes show progress from database`() =
        runTest {
            isMockTraktFlow.value = false

            // Given user unmarks the last remaining episode of a show
            coEvery { mockEpisodeWatchHistoryDao.isEpisodeWatched(93405, 2, 1) } returns true
            coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns emptyList()

            repository.markEpisodeWatched(
                accessToken = null,
                showTmdbId = 93405,
                season = 2,
                episode = 1,
                showTitle = "Severance",
                totalAired = 19
            )

            // Then show watch progress is deleted
            coVerify { mockShowWatchProgressDao.deleteByShowId(93405) }
        }

    @Test
    fun `markSeasonWatched unmarking entire season recalculates queue accurately`() = runTest {
        isMockTraktFlow.value = false

        // Given user unmarks Season 2 (leaving Season 1 with 10 eps watched)
        val season1Watched = (1..10).map {
            com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity(
                93405,
                1,
                it,
                1000L + it
            )
        }
        coEvery { mockEpisodeWatchHistoryDao.getAllWatchedEpisodes(93405) } returns season1Watched.reversed()

        val progressSlot =
            io.mockk.slot<com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity>()
        coEvery { mockShowWatchProgressDao.insertOrUpdate(capture(progressSlot)) } returns Unit

        repository.markSeasonWatched(
            accessToken = null,
            showTmdbId = 93405,
            season = 2,
            episodeNumbers = emptyList(),
            showTitle = "Severance",
            totalAired = 19
        )

        // Then Season 2 is deleted from history and Up Next targets Season 2 Episode 1
        coVerify { mockEpisodeWatchHistoryDao.deleteSeason(93405, 2) }
        assertThat(progressSlot.isCaptured).isTrue()
        val captured = progressSlot.captured
        assertThat(captured.seasonNumber).isEqualTo(2)
        assertThat(captured.episodeNumber).isEqualTo(1)
        assertThat(captured.totalCompleted).isEqualTo(10)
        assertThat(captured.totalAired).isEqualTo(19)
    }
}
