package com.ssverma.shared.data.repository

import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.storage.debug.DebugConfigManager
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.data.remote.TraktIds
import com.ssverma.shared.data.remote.TraktMediaItemIdentifier
import com.ssverma.shared.data.remote.TraktSyncBody
import com.ssverma.shared.data.remote.TraktSyncService
import com.ssverma.shared.domain.model.trakt.TraktSyncResult
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.repository.TraktSyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktSyncRepositoryImpl @Inject constructor(
    private val traktSyncService: TraktSyncService,
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val favoriteDao: FavoriteDao,
    private val debugConfigManager: DebugConfigManager
) : TraktSyncRepository {

    private data class MockShowState(
        val showTmdbId: Int,
        val showTitle: String,
        val totalAired: Int,
        var totalCompleted: Int,
        var seasonNumber: Int,
        var episodeNumber: Int,
        val episodeTitles: Map<Pair<Int, Int>, String>
    )

    private val mockShows = mutableMapOf(
        93405 to MockShowState(
            showTmdbId = 93405,
            showTitle = "Severance",
            totalAired = 9,
            totalCompleted = 6,
            seasonNumber = 2,
            episodeNumber = 1,
            episodeTitles = mapOf(
                Pair(2, 1) to "Hello, Innie",
                Pair(2, 2) to "Goodbye, Mrs. Selvig",
                Pair(2, 3) to "Who Is Alive?",
                Pair(2, 4) to "Woe's Hollow",
                Pair(2, 5) to "The Aftermath",
                Pair(2, 6) to "Hide and Seek",
                Pair(2, 7) to "The Grim Barbarity",
                Pair(2, 8) to "Sweet Vitriol",
                Pair(2, 9) to "The We We Are"
            )
        ),
        94997 to MockShowState(
            showTmdbId = 94997,
            showTitle = "House of the Dragon",
            totalAired = 8,
            totalCompleted = 4,
            seasonNumber = 2,
            episodeNumber = 5,
            episodeTitles = mapOf(
                Pair(2, 1) to "A Son for a Son",
                Pair(2, 2) to "Rhaenyra the Cruel",
                Pair(2, 3) to "The Burning Mill",
                Pair(2, 4) to "A Dance of Dragons",
                Pair(2, 5) to "Regent",
                Pair(2, 6) to "Smallfolk",
                Pair(2, 7) to "The Red Sowing",
                Pair(2, 8) to "The Queen Who Ever Was"
            )
        ),
        136315 to MockShowState(
            showTmdbId = 136315,
            showTitle = "The Bear",
            totalAired = 28,
            totalCompleted = 18,
            seasonNumber = 3,
            episodeNumber = 1,
            episodeTitles = mapOf(
                Pair(3, 1) to "Tomorrow",
                Pair(3, 2) to "Next",
                Pair(3, 3) to "Doors",
                Pair(3, 4) to "Violet",
                Pair(3, 5) to "Children",
                Pair(3, 6) to "Napkins",
                Pair(3, 7) to "Legacy",
                Pair(3, 8) to "Ice Chips",
                Pair(3, 9) to "Apologies",
                Pair(3, 10) to "Forever"
            )
        ),
        76331 to MockShowState(
            showTmdbId = 76331,
            showTitle = "Succession",
            totalAired = 39,
            totalCompleted = 37,
            seasonNumber = 4,
            episodeNumber = 9,
            episodeTitles = mapOf(
                Pair(4, 9) to "Church and State",
                Pair(4, 10) to "With Open Eyes"
            )
        )
    )

    private fun getActiveClientId(): String {
        return debugConfigManager.customTraktClientId.value.ifBlank {
            "38848a60debb2652b41295b9588ebbf45b14f6bdf6a22f77ffbcad5ce29aaeb5"
        }
    }

    override suspend fun syncLibrary(accessToken: String): Result<TraktSyncResult> = withContext(Dispatchers.IO) {
        try {
            if (debugConfigManager.isMockTraktEnabled.value) {
                // Mock Sync: Add 3 mock watchlist items and 2 history items to Room DB
                val mockWatchlist = listOf(
                    WatchlistEntity(mediaId = 93405, mediaType = "Tv", title = "Severance", posterImageUrl = "", backdropImageUrl = "", voteAvg = 8.4f, releaseDate = "2022-02-18"),
                    WatchlistEntity(mediaId = 157336, mediaType = "Movie", title = "Interstellar", posterImageUrl = "", backdropImageUrl = "", voteAvg = 8.6f, releaseDate = "2014-11-05"),
                    WatchlistEntity(mediaId = 94997, mediaType = "Tv", title = "House of the Dragon", posterImageUrl = "", backdropImageUrl = "", voteAvg = 8.4f, releaseDate = "2022-08-21")
                )
                val mockHistory = listOf(
                    WatchHistoryEntity(mediaId = 550, mediaType = "Movie", title = "Fight Club", posterImageUrl = "", voteAvg = 8.4f),
                    WatchHistoryEntity(mediaId = 1396, mediaType = "Tv", title = "Breaking Bad", posterImageUrl = "", voteAvg = 8.9f)
                )
                watchlistDao.insertAll(mockWatchlist)
                watchHistoryDao.insertAll(mockHistory)

                return@withContext Result.success(
                    TraktSyncResult(
                        itemsImportedToWatchlist = mockWatchlist.size,
                        itemsImportedToHistory = mockHistory.size,
                        itemsExportedToTrakt = 2
                    )
                )
            }

            val clientId = getActiveClientId()
            val bearer = "Bearer $accessToken"
            var importedWatchlist = 0
            var importedHistory = 0
            var exportedItems = 0

            // 1. Sync Watchlist (Trakt -> Room DB)
            when (val watchlistRes = traktSyncService.getWatchlist(bearer, clientId)) {
                is ApiResponse.Success -> {
                    val traktWatchlist = watchlistRes.body
                    val entitiesToInsert = mutableListOf<WatchlistEntity>()

                    traktWatchlist.forEach { item ->
                        val tmdbId = item.movie?.ids?.tmdb ?: item.show?.ids?.tmdb
                        val title = item.movie?.title ?: item.show?.title ?: ""
                        val mediaType = if (item.movie != null) "Movie" else "Tv"

                        if (tmdbId != null && tmdbId > 0) {
                            entitiesToInsert.add(
                                WatchlistEntity(
                                    mediaId = tmdbId,
                                    mediaType = mediaType,
                                    title = title,
                                    posterImageUrl = "",
                                    backdropImageUrl = "",
                                    voteAvg = 0f,
                                    releaseDate = "",
                                    addedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }

                    if (entitiesToInsert.isNotEmpty()) {
                        watchlistDao.insertAll(entitiesToInsert)
                        importedWatchlist = entitiesToInsert.size
                    }

                    // Export local watchlist items not yet in Trakt
                    val localWatchlist = watchlistDao.getAllWatchlist()
                    val remoteIds = traktWatchlist.mapNotNull { it.movie?.ids?.tmdb ?: it.show?.ids?.tmdb }.toSet()
                    val missingMovies = localWatchlist.filter { it.mediaType == "Movie" && it.mediaId !in remoteIds }
                        .map { TraktMediaItemIdentifier(ids = TraktIds(tmdb = it.mediaId), title = it.title) }
                    val missingShows = localWatchlist.filter { it.mediaType == "Tv" && it.mediaId !in remoteIds }
                        .map { TraktMediaItemIdentifier(ids = TraktIds(tmdb = it.mediaId), title = it.title) }

                    if (missingMovies.isNotEmpty() || missingShows.isNotEmpty()) {
                        traktSyncService.addToWatchlist(
                            bearerToken = bearer,
                            clientId = clientId,
                            payload = TraktSyncBody(movies = missingMovies, shows = missingShows)
                        )
                        exportedItems += (missingMovies.size + missingShows.size)
                    }
                }
                else -> {}
            }

            // 2. Sync History (Trakt -> Room DB)
            when (val historyRes = traktSyncService.getHistory(bearer, clientId, limit = 100)) {
                is ApiResponse.Success -> {
                    val traktHistory = historyRes.body
                    val historyToInsert = mutableListOf<WatchHistoryEntity>()

                    traktHistory.forEach { item ->
                        val tmdbId = item.movie?.ids?.tmdb ?: item.show?.ids?.tmdb
                        val title = item.movie?.title ?: item.show?.title ?: ""
                        val mediaType = if (item.movie != null) "Movie" else "Tv"

                        if (tmdbId != null && tmdbId > 0) {
                            historyToInsert.add(
                                WatchHistoryEntity(
                                    mediaId = tmdbId,
                                    mediaType = mediaType,
                                    title = title,
                                    posterImageUrl = "",
                                    voteAvg = 0f,
                                    watchedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }

                    if (historyToInsert.isNotEmpty()) {
                        watchHistoryDao.insertAll(historyToInsert)
                        importedHistory = historyToInsert.size
                    }
                }
                else -> {}
            }

            Result.success(
                TraktSyncResult(
                    itemsImportedToWatchlist = importedWatchlist,
                    itemsImportedToHistory = importedHistory,
                    itemsExportedToTrakt = exportedItems
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    override suspend fun getUpNextQueue(accessToken: String): Result<List<TraktUpNextEpisode>> = withContext(Dispatchers.IO) {
        try {
            if (debugConfigManager.isMockTraktEnabled.value) {
                val mockQueue = mockShows.values
                    .filter { it.totalCompleted < it.totalAired }
                    .map { show ->
                        val epTitle = show.episodeTitles[Pair(show.seasonNumber, show.episodeNumber)]
                            ?: "Episode ${show.episodeNumber}"
                        TraktUpNextEpisode(
                            showTmdbId = show.showTmdbId,
                            showTitle = show.showTitle,
                            showPosterPath = null,
                            seasonNumber = show.seasonNumber,
                            episodeNumber = show.episodeNumber,
                            episodeTitle = epTitle,
                            totalCompleted = show.totalCompleted,
                            totalAired = show.totalAired
                        )
                    }
                return@withContext Result.success(mockQueue)
            }

            val clientId = getActiveClientId()
            val bearer = "Bearer $accessToken"
            when (val res = traktSyncService.getWatchedShowProgress(bearer, clientId)) {
                is ApiResponse.Success -> {
                    val upNextList = res.body.mapNotNull { showProgress ->
                        val showTmdbId = showProgress.show.ids.tmdb
                        val nextEp = showProgress.nextEpisode

                        if (showTmdbId != null && nextEp != null) {
                            TraktUpNextEpisode(
                                showTmdbId = showTmdbId,
                                showTitle = showProgress.show.title,
                                showPosterPath = null,
                                seasonNumber = nextEp.season,
                                episodeNumber = nextEp.number,
                                episodeTitle = nextEp.title,
                                totalAired = showProgress.aired,
                                totalCompleted = showProgress.completed
                            )
                        } else null
                    }
                    Result.success(upNextList)
                }

                is ApiResponse.Error -> {
                    Result.failure(Exception("Failed to fetch Up Next queue from Trakt"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markEpisodeWatched(
        accessToken: String,
        showTmdbId: Int,
        season: Int,
        episode: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (debugConfigManager.isMockTraktEnabled.value) {
                val show = mockShows[showTmdbId]
                if (show != null) {
                    show.totalCompleted = (show.totalCompleted + 1).coerceAtMost(show.totalAired)
                    show.episodeNumber += 1
                }
                return@withContext Result.success(Unit)
            }

            val clientId = getActiveClientId()
            val bearer = "Bearer $accessToken"
            val payload = TraktSyncBody(
                shows = listOf(
                    TraktMediaItemIdentifier(ids = TraktIds(tmdb = showTmdbId))
                )
            )
            traktSyncService.addToHistory(bearer, clientId, payload)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMovieWatched(
        accessToken: String,
        movieTmdbId: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (debugConfigManager.isMockTraktEnabled.value) {
                return@withContext Result.success(Unit)
            }

            val clientId = getActiveClientId()
            val bearer = "Bearer $accessToken"
            val payload = TraktSyncBody(
                movies = listOf(
                    TraktMediaItemIdentifier(ids = TraktIds(tmdb = movieTmdbId))
                )
            )
            traktSyncService.addToHistory(bearer, clientId, payload)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
