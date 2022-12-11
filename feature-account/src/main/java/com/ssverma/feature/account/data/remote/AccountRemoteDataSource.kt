package com.ssverma.feature.account.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.request.FavoriteMediaBody
import com.ssverma.api.service.tmdb.request.WatchlistMediaBody
import com.ssverma.api.service.tmdb.response.AccountPayload
import com.ssverma.api.service.tmdb.response.MediaStatsPayload

interface AccountRemoteDataSource {
    suspend fun fetchAccountDetails(
        sessionId: String
    ): TmdbApiResponse<AccountPayload>

    suspend fun fetchMovieAccountStats(
        movieId: Int,
        sessionId: String
    ): TmdbApiResponse<MediaStatsPayload>

    suspend fun fetchTvShowAccountStats(
        tvShowId: Int,
        sessionId: String
    ): TmdbApiResponse<MediaStatsPayload>

    suspend fun markMediaAsFavorite(
        sessionId: String,
        accountId: Int,
        favoriteBody: FavoriteMediaBody
    ): TmdbApiResponse<Unit>

    suspend fun markMediaInWatchlist(
        sessionId: String,
        accountId: Int,
        watchlistBody: WatchlistMediaBody
    ): TmdbApiResponse<Unit>
}