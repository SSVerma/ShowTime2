package com.ssverma.showtime.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.GenrePayload
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteReview
import javax.inject.Inject

class DefaultMovieRemoteDataSource @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : MovieRemoteDataSource {

    override suspend fun discoverMovies(
        queryMap: Map<String, String>,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>> {
        return tmdbApiService.getDiscoveredMovies(
            queryMap = queryMap,
            page = page
        )
    }

    override suspend fun fetchTrendingMovies(
        timeWindow: String,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>> {
        return tmdbApiService.getTrendingMovies(
            timeWindow = timeWindow,
            page = page
        )
    }

    override suspend fun fetchTopRatedMovies(page: Int): TmdbApiResponse<PagedPayload<RemoteMovie>> {
        return tmdbApiService.getTopRatedMovies(page = page)
    }

    override suspend fun fetchMovieGenre(): TmdbApiResponse<GenrePayload> {
        return tmdbApiService.getMovieGenres()
    }

    override suspend fun fetchMovieReviewsGradually(
        movieId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteReview>> {
        return tmdbApiService.getMovieReviews(
            movieId = movieId,
            page = page
        )
    }

    override suspend fun fetchMovieDetails(
        movieId: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemoteMovie> {
        return tmdbApiService.getMovieDetails(
            movieId = movieId,
            queryMap = queryMap
        )
    }

}