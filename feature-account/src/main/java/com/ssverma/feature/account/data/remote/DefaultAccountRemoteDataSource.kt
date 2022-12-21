package com.ssverma.feature.account.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.request.FavoriteMediaBody
import com.ssverma.api.service.tmdb.request.WatchlistMediaBody
import com.ssverma.api.service.tmdb.response.*
import javax.inject.Inject

internal class DefaultAccountRemoteDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : AccountRemoteDataSource {

    override suspend fun fetchAccountDetails(sessionId: String): TmdbApiResponse<AccountPayload> {
        return tmdbApiService.getAccount(sessionId = sessionId)
    }

    override suspend fun fetchMovieAccountStats(
        movieId: Int,
        sessionId: String
    ): TmdbApiResponse<MediaStatsPayload> {
        return tmdbApiService.getMovieAccountStats(
            movieId = movieId,
            sessionId = sessionId
        )
    }

    override suspend fun fetchTvShowAccountStats(
        tvShowId: Int,
        sessionId: String
    ): TmdbApiResponse<MediaStatsPayload> {
        return tmdbApiService.getTvShowAccountStats(
            tvShowId = tvShowId,
            sessionId = sessionId
        )
    }

    override suspend fun markMediaAsFavorite(
        sessionId: String,
        accountId: Int,
        favoriteBody: FavoriteMediaBody
    ): TmdbApiResponse<Unit> {
        return tmdbApiService.markMediaAsFavorite(
            sessionId = sessionId,
            accountId = accountId,
            mediaBody = favoriteBody
        )
    }

    override suspend fun markMediaInWatchlist(
        sessionId: String,
        accountId: Int,
        watchlistBody: WatchlistMediaBody
    ): TmdbApiResponse<Unit> {
        return tmdbApiService.markMediaInWatchlist(
            sessionId = sessionId,
            accountId = accountId,
            watchlistBody = watchlistBody
        )
    }

    override suspend fun fetchFavoriteMovies(
        sessionId: String,
        accountId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>> {
        return tmdbApiService.getFavoriteMovies(
            sessionId = sessionId,
            accountId = accountId,
            page = page
        )
    }

    override suspend fun fetchFavoriteTvShows(
        sessionId: String,
        accountId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>> {
        return tmdbApiService.getFavoriteTvShows(
            sessionId = sessionId,
            accountId = accountId,
            page = page
        )
    }

    override suspend fun fetchWatchlistMovies(
        sessionId: String,
        accountId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>> {
        return tmdbApiService.getWatchlistMovies(
            sessionId = sessionId,
            accountId = accountId,
            page = page
        )
    }

    override suspend fun fetchWatchlistTvShows(
        sessionId: String,
        accountId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>> {
        return tmdbApiService.getWatchlistTvShows(
            sessionId = sessionId,
            accountId = accountId,
            page = page
        )
    }
}