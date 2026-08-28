package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.trakt.TraktSyncResult
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import kotlinx.coroutines.flow.Flow

interface TraktSyncRepository {
    suspend fun syncLibrary(accessToken: String): Result<TraktSyncResult>
    suspend fun getUpNextQueue(accessToken: String): Result<List<TraktUpNextEpisode>>
    fun getUpNextQueueFlow(accessToken: String): Flow<List<TraktUpNextEpisode>>
    fun getWatchedEpisodesFlow(showId: Int, seasonNumber: Int): Flow<Set<Int>>
    fun getWatchedSeasonsFlow(showId: Int): Flow<Set<Int>>
    fun getSeasonWatchCountsFlow(showId: Int): Flow<Map<Int, Int>>
    fun isEpisodeWatchedFlow(showId: Int, seasonNumber: Int, episodeNumber: Int): Flow<Boolean>
    suspend fun markEpisodeWatched(
        accessToken: String?,
        showTmdbId: Int,
        season: Int,
        episode: Int,
        showTitle: String = "",
        showPosterPath: String? = null,
        episodeTitle: String? = null,
        totalAired: Int = 0
    ): Result<Unit>

    suspend fun markSeasonWatched(
        accessToken: String?,
        showTmdbId: Int,
        season: Int,
        episodeNumbers: List<Int> = emptyList(),
        showTitle: String = "",
        showPosterPath: String? = null,
        totalAired: Int = 0
    ): Result<Unit>

    suspend fun markMovieWatched(accessToken: String?, movieTmdbId: Int): Result<Unit>
}
