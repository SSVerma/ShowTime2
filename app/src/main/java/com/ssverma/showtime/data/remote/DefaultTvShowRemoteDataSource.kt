package com.ssverma.showtime.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.*
import javax.inject.Inject

class DefaultTvShowRemoteDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : TvShowRemoteDataSource {

    override suspend fun discoverTvShows(
        queryMap: Map<String, String>,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>> {
        return tmdbApiService.getDiscoveredTvShows(
            queryMap = queryMap,
            page = page
        )
    }

    override suspend fun fetchTrendingTvShows(
        timeWindow: String,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>> {
        return tmdbApiService.getTrendingTvShows(
            timeWindow = timeWindow,
            page = page
        )
    }

    override suspend fun fetchTopRatedTvShows(
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>> {
        return tmdbApiService.getTopRatedTvShows(page = page)
    }

    override suspend fun fetchTvShowGenre(): TmdbApiResponse<GenrePayload> {
        return tmdbApiService.getTvGenres()
    }

    override suspend fun fetchTvShowReviewsGradually(
        tvShowId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteReview>> {
        return tmdbApiService.getTvShowReviews(
            tvShowId = tvShowId,
            page = page
        )
    }

    override suspend fun fetchTvShowDetails(
        tvShowId: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemoteTvShow> {
        return tmdbApiService.getTvShowDetails(
            tvShowId = tvShowId,
            queryMap = queryMap
        )
    }

    override suspend fun fetchTvSeasonDetails(
        tvShowId: Int,
        seasonNumber: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemoteTvSeason> {
        return tmdbApiService.getTvSeason(
            tvShowId = tvShowId,
            seasonNumber = seasonNumber,
            queryMap = queryMap
        )
    }

    override suspend fun fetchTvEpisodeDetails(
        tvShowId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemoteTvEpisode> {
        return tmdbApiService.getTvEpisode(
            tvShowId = tvShowId,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            queryMap = queryMap
        )
    }
}