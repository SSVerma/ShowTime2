package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.trakt.TraktSyncResult
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode

interface TraktSyncRepository {
    suspend fun syncLibrary(accessToken: String): Result<TraktSyncResult>
    suspend fun getUpNextQueue(accessToken: String): Result<List<TraktUpNextEpisode>>
    suspend fun markEpisodeWatched(accessToken: String, showTmdbId: Int, season: Int, episode: Int): Result<Unit>
    suspend fun markMovieWatched(accessToken: String, movieTmdbId: Int): Result<Unit>
}
