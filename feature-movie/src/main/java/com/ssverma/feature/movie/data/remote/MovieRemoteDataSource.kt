package com.ssverma.feature.movie.data.remote

import com.ssverma.api.service.tmdb.TmdbApiResponse
import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.api.service.tmdb.response.GenrePayload
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteReview

interface MovieRemoteDataSource {
    suspend fun discoverMovies(
        queryMap: Map<String, String>,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    suspend fun fetchTrendingMovies(
        timeWindow: String = TmdbApiTiedConstants.AvailableTimeWindows.DAY,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    suspend fun fetchTopRatedMovies(page: Int): TmdbApiResponse<PagedPayload<RemoteMovie>>

    suspend fun fetchMovieGenre(): TmdbApiResponse<GenrePayload>

    suspend fun fetchMovieReviewsGradually(
        movieId: Int,
        page: Int
    ): TmdbApiResponse<PagedPayload<RemoteReview>>

    suspend fun fetchMovieDetails(
        movieId: Int,
        queryMap: Map<String, String>
    ): TmdbApiResponse<RemoteMovie>

}