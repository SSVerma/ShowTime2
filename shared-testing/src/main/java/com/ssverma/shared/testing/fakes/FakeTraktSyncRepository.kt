package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.model.trakt.TraktSyncResult
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.repository.TraktSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeTraktSyncRepository : TraktSyncRepository {

    val upNextQueueFlow = MutableStateFlow<List<TraktUpNextEpisode>>(emptyList())
    var syncResult: Result<TraktSyncResult> = Result.success(
        TraktSyncResult(
            itemsImportedToWatchlist = 0,
            itemsImportedToHistory = 0,
            itemsExportedToTrakt = 0
        )
    )

    var lastMarkedShowTmdbId: Int? = null
    var lastMarkedSeason: Int? = null
    var lastMarkedEpisode: Int? = null
    var lastMarkedShowTitle: String? = null
    var lastMarkedPosterPath: String? = null
    var lastMarkedTotalAired: Int? = null

    override suspend fun syncLibrary(accessToken: String): Result<TraktSyncResult> {
        return syncResult
    }

    override suspend fun getUpNextQueue(accessToken: String): Result<List<TraktUpNextEpisode>> {
        return Result.success(upNextQueueFlow.value)
    }

    override fun getUpNextQueueFlow(accessToken: String): Flow<List<TraktUpNextEpisode>> {
        return upNextQueueFlow
    }

    override fun getWatchedEpisodesFlow(showId: Int, seasonNumber: Int): Flow<Set<Int>> {
        return flowOf(emptySet())
    }

    override fun getWatchedSeasonsFlow(showId: Int): Flow<Set<Int>> {
        return flowOf(emptySet())
    }

    override fun getSeasonWatchCountsFlow(showId: Int): Flow<Map<Int, Int>> {
        return flowOf(emptyMap())
    }

    override fun isEpisodeWatchedFlow(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<Boolean> {
        return flowOf(false)
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
    ): Result<Unit> {
        lastMarkedShowTmdbId = showTmdbId
        lastMarkedSeason = season
        lastMarkedEpisode = episode
        lastMarkedShowTitle = showTitle
        lastMarkedPosterPath = showPosterPath
        lastMarkedTotalAired = totalAired

        // Update upNextQueueFlow items (filter out completed shows)
        upNextQueueFlow.value = upNextQueueFlow.value.mapNotNull { item ->
            if (item.showTmdbId == showTmdbId) {
                val nextCompleted = item.totalCompleted + 1
                if (nextCompleted >= item.totalAired) {
                    null
                } else {
                    item.copy(
                        totalCompleted = nextCompleted,
                        episodeNumber = item.episodeNumber + 1,
                        episodeTitle = "Episode ${item.episodeNumber + 1}"
                    )
                }
            } else item
        }
        return Result.success(Unit)
    }

    override suspend fun markSeasonWatched(
        accessToken: String?,
        showTmdbId: Int,
        season: Int,
        episodeNumbers: List<Int>,
        showTitle: String,
        showPosterPath: String?,
        totalAired: Int
    ): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun markMovieWatched(
        accessToken: String?,
        movieTmdbId: Int
    ): Result<Unit> {
        return Result.success(Unit)
    }
}
