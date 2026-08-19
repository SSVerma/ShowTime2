package com.ssverma.feature.tv.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.response.GenrePayload
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteReview
import com.ssverma.api.service.tmdb.response.RemoteTvEpisode
import com.ssverma.api.service.tmdb.response.RemoteTvSeason
import com.ssverma.api.service.tmdb.response.RemoteTvShow
import com.ssverma.api.service.tmdb.response.RemoteWatchProviderResponse

interface TvShowRemoteDataSource {
    suspend fun discoverTvShows(
        queryMap: Map<String, String>,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    suspend fun fetchTrendingTvShows(
        timeWindow: String,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    suspend fun fetchTvShowGenre(): TmdbApiResponse<GenrePayload>

    suspend fun fetchTvShowReviewsGradually(
        tvShowId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteReview>>

    suspend fun fetchTvShowDetails(
        tvShowId: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemoteTvShow>

    suspend fun fetchTvSeasonDetails(
        tvShowId: Int,
        seasonNumber: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemoteTvSeason>

    suspend fun fetchTvEpisodeDetails(
        tvShowId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemoteTvEpisode>

    suspend fun fetchTvShowWatchProviders(
        tvShowId: Int
    ): TmdbApiResponse<RemoteWatchProviderResponse>
}