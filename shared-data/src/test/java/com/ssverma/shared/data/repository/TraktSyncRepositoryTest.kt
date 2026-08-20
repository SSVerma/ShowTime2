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
    private val mockDebugConfigManager: com.ssverma.core.storage.debug.DebugConfigManager = mockk(relaxed = true)

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
            debugConfigManager = mockDebugConfigManager
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
}
