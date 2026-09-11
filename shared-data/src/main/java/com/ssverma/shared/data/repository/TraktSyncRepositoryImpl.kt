package com.ssverma.shared.data.repository

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.data.debug.DebugConfigManager
import com.ssverma.shared.data.local.db.dao.EpisodeWatchHistoryDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.data.local.mock.MockTraktDataSource
import com.ssverma.shared.data.remote.TraktIds
import com.ssverma.shared.data.remote.TraktMediaItemIdentifier
import com.ssverma.shared.data.remote.TraktSyncBody
import com.ssverma.shared.data.remote.TraktSyncService
import com.ssverma.shared.domain.auth.TraktAuthProvider
import com.ssverma.shared.domain.model.trakt.TraktSyncResult
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.notifier.WidgetSyncNotifier
import com.ssverma.shared.domain.repository.TraktSyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktSyncRepositoryImpl @Inject constructor(
    private val traktSyncService: TraktSyncService,
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val favoriteDao: FavoriteDao,
    private val episodeWatchHistoryDao: EpisodeWatchHistoryDao,
    private val showWatchProgressDao: ShowWatchProgressDao,
    private val debugConfigManager: DebugConfigManager,
    private val mockTraktDataSource: MockTraktDataSource,
    private val tmdbApiService: TmdbApiService? = null,
    private val widgetSyncNotifier: WidgetSyncNotifier? = null,
    private val traktAuthProvider: TraktAuthProvider? = null
) : TraktSyncRepository {

    private fun getActiveClientId(): String {
        return debugConfigManager.customTraktClientId.value.ifBlank {
            "38848a60debb2652b41295b9588ebbf45b14f6bdf6a22f77ffbcad5ce29aaeb5"
        }
    }

    private suspend fun resolveAccessToken(passedToken: String?): String? {
        return passedToken?.takeIf { it.isNotBlank() } ?: traktAuthProvider?.getAccessToken()
    }

    override fun getUpNextQueueFlow(accessToken: String?): Flow<List<TraktUpNextEpisode>> {
        return showWatchProgressDao.getUpNextQueueFlow().map { progressList ->
            if (debugConfigManager.isMockTraktEnabled.value) {
                mockTraktDataSource.getUpNextQueue()
            } else {
                progressList.map { progress ->
                    TraktUpNextEpisode(
                        showTmdbId = progress.showId,
                        showTitle = progress.showTitle,
                        showPosterPath = progress.showPosterPath,
                        seasonNumber = progress.seasonNumber,
                        episodeNumber = progress.episodeNumber,
                        episodeTitle = progress.episodeTitle,
                        seasonCompleted = progress.seasonCompleted,
                        seasonTotalAired = progress.seasonTotalAired,
                        totalCompleted = progress.totalCompleted,
                        totalAired = progress.totalAired
                    )
                }
            }
        }
    }

    override fun getWatchedEpisodesFlow(showId: Int, seasonNumber: Int): Flow<Set<Int>> {
        return episodeWatchHistoryDao.getWatchedEpisodeNumbersFlow(showId, seasonNumber)
            .map { it.toSet() }
    }

    override fun getWatchedSeasonsFlow(showId: Int): Flow<Set<Int>> {
        return episodeWatchHistoryDao.getWatchedSeasonsFlow(showId)
            .map { it.toSet() }
    }

    override fun getSeasonWatchCountsFlow(showId: Int): Flow<Map<Int, Int>> {
        return episodeWatchHistoryDao.getSeasonWatchCountsFlow(showId).map { counts ->
            counts.associate { it.seasonNumber to it.watchedCount }
        }
    }

    override fun isEpisodeWatchedFlow(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<Boolean> {
        return episodeWatchHistoryDao.isEpisodeWatchedFlow(showId, seasonNumber, episodeNumber)
    }

    override suspend fun syncLibrary(accessToken: String?): Result<TraktSyncResult> =
        withContext(Dispatchers.IO) {
            try {
                if (debugConfigManager.isMockTraktEnabled.value) {
                    val mockWatchlist = mockTraktDataSource.getMockWatchlist()
                    val mockHistory = mockTraktDataSource.getMockHistory()
                    watchlistDao.insertAll(mockWatchlist)
                    watchHistoryDao.insertAll(mockHistory)

                    widgetSyncNotifier?.notifyWidgetDataChanged()

                    return@withContext Result.success(
                        TraktSyncResult(
                            itemsImportedToWatchlist = mockWatchlist.size,
                            itemsImportedToHistory = mockHistory.size,
                            itemsExportedToTrakt = 2
                        )
                    )
                }

                val token = resolveAccessToken(accessToken).orEmpty()
                val clientId = getActiveClientId()
                val bearer = "Bearer $token"
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
                        val remoteIds =
                            traktWatchlist.mapNotNull { it.movie?.ids?.tmdb ?: it.show?.ids?.tmdb }
                                .toSet()
                        val missingMovies =
                            localWatchlist.filter { it.mediaType == "Movie" && it.mediaId !in remoteIds }
                                .map {
                                    TraktMediaItemIdentifier(
                                        ids = TraktIds(tmdb = it.mediaId),
                                        title = it.title
                                    )
                                }
                        val missingShows =
                            localWatchlist.filter { it.mediaType == "Tv" && it.mediaId !in remoteIds }
                                .map {
                                    TraktMediaItemIdentifier(
                                        ids = TraktIds(tmdb = it.mediaId),
                                        title = it.title
                                    )
                                }

                        if (missingMovies.isNotEmpty() || missingShows.isNotEmpty()) {
                            traktSyncService.addToWatchlist(
                                bearerToken = bearer,
                                clientId = clientId,
                                payload = TraktSyncBody(
                                    movies = missingMovies,
                                    shows = missingShows
                                )
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

                widgetSyncNotifier?.notifyWidgetDataChanged()

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

    override suspend fun getUpNextQueue(accessToken: String?): Result<List<TraktUpNextEpisode>> =
        withContext(Dispatchers.IO) {
            try {
                if (debugConfigManager.isMockTraktEnabled.value) {
                    return@withContext Result.success(mockTraktDataSource.getUpNextQueue())
                }

                val token = resolveAccessToken(accessToken)
                if (!token.isNullOrBlank()) {
                    val clientId = getActiveClientId()
                    val bearer = "Bearer $token"
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
                                        seasonCompleted = showProgress.completed,
                                        seasonTotalAired = showProgress.aired,
                                        totalAired = showProgress.aired,
                                        totalCompleted = showProgress.completed
                                    )
                                } else null
                            }
                            return@withContext Result.success(upNextList)
                        }

                        is ApiResponse.Error -> {
                            // Fall back to local DB if Trakt network error
                        }
                    }
                }

                // Production: 100% dynamic Up Next queue from SQLite Room DB
                val localQueue = showWatchProgressDao.getUpNextQueue().map { progress ->
                    TraktUpNextEpisode(
                        showTmdbId = progress.showId,
                        showTitle = progress.showTitle,
                        showPosterPath = progress.showPosterPath,
                        seasonNumber = progress.seasonNumber,
                        episodeNumber = progress.episodeNumber,
                        episodeTitle = progress.episodeTitle,
                        seasonCompleted = progress.seasonCompleted,
                        seasonTotalAired = progress.seasonTotalAired,
                        totalCompleted = progress.totalCompleted,
                        totalAired = progress.totalAired
                    )
                }
                Result.success(localQueue)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun markEpisodeWatched(
        accessToken: String?,
        showTmdbId: Int,
        season: Int,
        episode: Int,
        showTitle: String,
        showPosterPath: String?,
        episodeTitle: String?,
        totalAired: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val isCurrentlyWatched =
                episodeWatchHistoryDao.isEpisodeWatched(showTmdbId, season, episode)
            if (isCurrentlyWatched) {
                episodeWatchHistoryDao.deleteEpisode(showTmdbId, season, episode)
            } else {
                episodeWatchHistoryDao.insertEpisode(
                    EpisodeWatchHistoryEntity(
                        showId = showTmdbId,
                        seasonNumber = season,
                        episodeNumber = episode,
                        watchedAt = System.currentTimeMillis()
                    )
                )
            }

            val currentProgress = showWatchProgressDao.getProgress(showTmdbId)
            val watchedEpisodes = episodeWatchHistoryDao.getAllWatchedEpisodes(showTmdbId)
            val watchedSet = watchedEpisodes.map { Pair(it.seasonNumber, it.episodeNumber) }.toSet()
            val actualCompleted = watchedSet.size

            val actualTitle = if (showTitle.isNotBlank()) {
                if (showTitle.matches(Regex("Season \\d+.*", RegexOption.IGNORE_CASE))) {
                    currentProgress?.showTitle.orEmpty().ifBlank { showTitle }
                } else {
                    showTitle
                }
            } else {
                currentProgress?.showTitle.orEmpty().ifBlank { "TV Show" }
            }
            val actualPoster = showPosterPath ?: currentProgress?.showPosterPath

            if (actualCompleted == 0) {
                showWatchProgressDao.deleteByShowId(showTmdbId)
            } else {
                val resolution = resolveNextEpisodeResolution(
                    showTmdbId = showTmdbId,
                    watchedSet = watchedSet,
                    currentProgress = currentProgress,
                    passedSeason = season,
                    passedTotalAired = totalAired
                )
                val nextEpTitle = resolveEpisodeTitle(
                    showTmdbId = showTmdbId,
                    seasonNumber = resolution.seasonNumber,
                    episodeNumber = resolution.episodeNumber,
                    explicitTitle = episodeTitle
                )

                showWatchProgressDao.insertOrUpdate(
                    ShowWatchProgressEntity(
                        showId = showTmdbId,
                        showTitle = actualTitle,
                        showPosterPath = actualPoster,
                        seasonNumber = resolution.seasonNumber,
                        episodeNumber = resolution.episodeNumber,
                        episodeTitle = nextEpTitle ?: "Episode ${resolution.episodeNumber}",
                        seasonCompleted = resolution.seasonCompleted,
                        seasonTotalAired = resolution.seasonTotalAired,
                        totalCompleted = resolution.totalCompleted,
                        totalAired = resolution.totalAired,
                        lastWatchedAt = currentProgress?.lastWatchedAt ?: System.currentTimeMillis()
                    )
                )
            }

            if (debugConfigManager.isMockTraktEnabled.value) {
                mockTraktDataSource.onEpisodeWatchedToggled(showTmdbId, isCurrentlyWatched)
            }

            val token = resolveAccessToken(accessToken)
            if (!token.isNullOrBlank() && !debugConfigManager.isMockTraktEnabled.value) {
                val clientId = getActiveClientId()
                val bearer = "Bearer $token"
                val payload = TraktSyncBody(
                    shows = listOf(
                        TraktMediaItemIdentifier(ids = TraktIds(tmdb = showTmdbId))
                    )
                )
                traktSyncService.addToHistory(bearer, clientId, payload)
            }

            widgetSyncNotifier?.notifyWidgetDataChanged()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markSeasonWatched(
        accessToken: String?,
        showTmdbId: Int,
        season: Int,
        episodeNumbers: List<Int>,
        showTitle: String,
        showPosterPath: String?,
        totalAired: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentProgress = showWatchProgressDao.getProgress(showTmdbId)
            val actualTitle = if (showTitle.isNotBlank()) {
                if (showTitle.matches(Regex("Season \\d+.*", RegexOption.IGNORE_CASE))) {
                    currentProgress?.showTitle.orEmpty().ifBlank { showTitle }
                } else {
                    showTitle
                }
            } else {
                currentProgress?.showTitle.orEmpty().ifBlank { "TV Show" }
            }
            val actualPoster = showPosterPath ?: currentProgress?.showPosterPath

            if (episodeNumbers.isEmpty()) {
                // Unmark season
                episodeWatchHistoryDao.deleteSeason(showTmdbId, season)
                val remainingWatched = episodeWatchHistoryDao.getAllWatchedEpisodes(showTmdbId)
                if (remainingWatched.isEmpty()) {
                    showWatchProgressDao.deleteByShowId(showTmdbId)
                } else {
                    val watchedSet =
                        remainingWatched.map { Pair(it.seasonNumber, it.episodeNumber) }.toSet()
                    val resolution = resolveNextEpisodeResolution(
                        showTmdbId = showTmdbId,
                        watchedSet = watchedSet,
                        currentProgress = currentProgress,
                        passedSeason = season,
                        passedTotalAired = totalAired
                    )
                    val nextEpTitle = resolveEpisodeTitle(
                        showTmdbId = showTmdbId,
                        seasonNumber = resolution.seasonNumber,
                        episodeNumber = resolution.episodeNumber,
                        explicitTitle = null
                    )

                    showWatchProgressDao.insertOrUpdate(
                        ShowWatchProgressEntity(
                            showId = showTmdbId,
                            showTitle = actualTitle,
                            showPosterPath = actualPoster,
                            seasonNumber = resolution.seasonNumber,
                            episodeNumber = resolution.episodeNumber,
                            episodeTitle = nextEpTitle ?: "Episode ${resolution.episodeNumber}",
                            seasonCompleted = resolution.seasonCompleted,
                            seasonTotalAired = resolution.seasonTotalAired,
                            totalCompleted = resolution.totalCompleted,
                            totalAired = resolution.totalAired,
                            lastWatchedAt = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                // Mark all episodes in season
                val entities = episodeNumbers.map { epNum ->
                    EpisodeWatchHistoryEntity(
                        showId = showTmdbId,
                        seasonNumber = season,
                        episodeNumber = epNum,
                        watchedAt = System.currentTimeMillis()
                    )
                }
                episodeWatchHistoryDao.insertAll(entities)

                val watchedEpisodes = episodeWatchHistoryDao.getAllWatchedEpisodes(showTmdbId)
                val watchedSet =
                    watchedEpisodes.map { Pair(it.seasonNumber, it.episodeNumber) }.toSet()
                val resolution = resolveNextEpisodeResolution(
                    showTmdbId = showTmdbId,
                    watchedSet = watchedSet,
                    currentProgress = currentProgress,
                    passedSeason = season,
                    passedTotalAired = totalAired,
                    isSeasonCompleteAction = true
                )
                val nextEpTitle = resolveEpisodeTitle(
                    showTmdbId = showTmdbId,
                    seasonNumber = resolution.seasonNumber,
                    episodeNumber = resolution.episodeNumber,
                    explicitTitle = null
                )

                showWatchProgressDao.insertOrUpdate(
                    ShowWatchProgressEntity(
                        showId = showTmdbId,
                        showTitle = actualTitle,
                        showPosterPath = actualPoster,
                        seasonNumber = resolution.seasonNumber,
                        episodeNumber = resolution.episodeNumber,
                        episodeTitle = nextEpTitle ?: "Episode ${resolution.episodeNumber}",
                        seasonCompleted = resolution.seasonCompleted,
                        seasonTotalAired = resolution.seasonTotalAired,
                        totalCompleted = resolution.totalCompleted,
                        totalAired = resolution.totalAired,
                        lastWatchedAt = System.currentTimeMillis()
                    )
                )
            }

            if (debugConfigManager.isMockTraktEnabled.value) {
                mockTraktDataSource.onSeasonWatchedToggled(showTmdbId, episodeNumbers)
            }

            val token = resolveAccessToken(accessToken)
            if (!token.isNullOrBlank() && !debugConfigManager.isMockTraktEnabled.value) {
                val clientId = getActiveClientId()
                val bearer = "Bearer $token"
                val payload = TraktSyncBody(
                    shows = listOf(
                        TraktMediaItemIdentifier(ids = TraktIds(tmdb = showTmdbId))
                    )
                )
                traktSyncService.addToHistory(bearer, clientId, payload)
            }

            widgetSyncNotifier?.notifyWidgetDataChanged()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMovieWatched(
        accessToken: String?,
        movieTmdbId: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = resolveAccessToken(accessToken)
            if (token.isNullOrBlank() || debugConfigManager.isMockTraktEnabled.value) {
                return@withContext Result.success(Unit)
            }

            val clientId = getActiveClientId()
            val bearer = "Bearer $token"
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

    private data class NextEpisodeResolution(
        val seasonNumber: Int,
        val episodeNumber: Int,
        val seasonCompleted: Int,
        val seasonTotalAired: Int,
        val totalCompleted: Int,
        val totalAired: Int
    )

    private suspend fun resolveNextEpisodeResolution(
        showTmdbId: Int,
        watchedSet: Set<Pair<Int, Int>>,
        currentProgress: ShowWatchProgressEntity?,
        passedSeason: Int,
        passedTotalAired: Int,
        isSeasonCompleteAction: Boolean = false
    ): NextEpisodeResolution {
        if (watchedSet.isEmpty()) {
            return NextEpisodeResolution(
                seasonNumber = 1,
                episodeNumber = 1,
                seasonCompleted = 0,
                seasonTotalAired = 1,
                totalCompleted = 0,
                totalAired = maxOf(passedTotalAired, 1)
            )
        }

        // 1. Fetch real season metadata from TMDB if available
        val remoteShow = try {
            val res = tmdbApiService?.getTvShowDetails(showTmdbId, emptyMap())
            (res as? ApiResponse.Success)?.body
        } catch (e: Exception) {
            null
        }

        val remoteSeasons: List<Pair<Int, Int>> = remoteShow?.seasons
            ?.filter { it.seasonNumber > 0 }
            ?.sortedBy { it.seasonNumber }
            ?.map { Pair(it.seasonNumber, it.episodeCount) }
            .orEmpty()

        val minSeason = watchedSet.minOf { it.first }
        val maxSeason = maxOf(watchedSet.maxOf { it.first }, passedSeason)

        // 2. Build complete seasons list combining remote TMDB, watched set, and passed params
        val allSeasonNumbers = if (remoteSeasons.isNotEmpty()) {
            remoteSeasons.map { it.first }
        } else {
            (minSeason..maxSeason).toList()
        }

        val totalShowAired = remoteShow?.episodeCount?.takeIf { it > 0 }
            ?: maxOf(
                currentProgress?.totalAired ?: 0,
                passedTotalAired,
                watchedSet.size
            )

        val seasonMetaList = allSeasonNumbers.map { sNum ->
            val remoteCount = remoteSeasons.firstOrNull { it.first == sNum }?.second ?: 0
            val maxWatchedInSeason =
                watchedSet.filter { it.first == sNum }.maxOfOrNull { it.second } ?: 1
            val seasonCount = when {
                remoteCount > 0 -> remoteCount
                currentProgress?.seasonNumber == sNum && currentProgress.seasonTotalAired > 0 -> currentProgress.seasonTotalAired
                sNum == passedSeason && passedTotalAired > 0 && passedTotalAired < totalShowAired -> passedTotalAired
                sNum == passedSeason && isSeasonCompleteAction -> maxWatchedInSeason
                allSeasonNumbers.size > 1 && sNum < maxSeason -> maxWatchedInSeason
                else -> maxOf(maxWatchedInSeason + 1, 1)
            }
            Pair(sNum, maxOf(seasonCount, maxWatchedInSeason, 1))
        }

        // 3. Find earliest unwatched episode across seasons (Option A: Continuous Chronological Watch Front)
        var targetSeason = seasonMetaList.firstOrNull()?.first ?: minSeason
        var targetEpisode = 1
        var targetSeasonCompleted = 0
        var targetSeasonTotalAired = seasonMetaList.firstOrNull()?.second ?: 1
        var foundUnwatched = false

        for ((sNum, sCount) in seasonMetaList) {
            val sCompleted = watchedSet.count { it.first == sNum }
            for (e in 1..sCount) {
                if (Pair(sNum, e) !in watchedSet) {
                    targetSeason = sNum
                    targetEpisode = e
                    targetSeasonCompleted = sCompleted
                    targetSeasonTotalAired = sCount
                    foundUnwatched = true
                    break
                }
            }
            if (foundUnwatched) break
        }

        if (!foundUnwatched) {
            if (watchedSet.size < totalShowAired) {
                // Advance to next season episode 1
                targetSeason = maxSeason + 1
                targetEpisode = 1
                targetSeasonCompleted = 0
                targetSeasonTotalAired =
                    remoteSeasons.firstOrNull { it.first == targetSeason }?.second ?: 10
            } else {
                // All episodes across all seasons completed
                val lastSeason = seasonMetaList.lastOrNull() ?: Pair(passedSeason, 1)
                targetSeason = lastSeason.first
                targetEpisode = lastSeason.second
                targetSeasonCompleted = lastSeason.second
                targetSeasonTotalAired = lastSeason.second
            }
        }

        return NextEpisodeResolution(
            seasonNumber = targetSeason,
            episodeNumber = targetEpisode,
            seasonCompleted = targetSeasonCompleted,
            seasonTotalAired = targetSeasonTotalAired,
            totalCompleted = watchedSet.size,
            totalAired = maxOf(totalShowAired, watchedSet.size)
        )
    }

    private suspend fun resolveEpisodeTitle(
        showTmdbId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        explicitTitle: String? = null
    ): String? {
        if (!explicitTitle.isNullOrBlank()) {
            return explicitTitle
        }
        val mockTitle =
            mockTraktDataSource.getEpisodeTitle(showTmdbId, seasonNumber, episodeNumber)
        if (!mockTitle.isNullOrBlank()) {
            return mockTitle
        }
        return try {
            val res = tmdbApiService?.getTvSeason(showTmdbId, seasonNumber, emptyMap())
            if (res is ApiResponse.Success) {
                res.body.episodes?.find { it.episodeNumber == episodeNumber }?.title
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
