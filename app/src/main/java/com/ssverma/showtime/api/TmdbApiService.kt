package com.ssverma.showtime.api

import com.ssverma.showtime.data.remote.response.GenrePayload
import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.data.remote.response.RemoteMovie
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface TmdbApiService {
    @GET("https://demo3320320.mockable.io/movies")
    suspend fun getMockMovies(@Query("page") page: Int): Response<PagedPayload<RemoteMovie>>

    @GET("3/movie/latest")
    suspend fun getLatestMovie(): Response<RemoteMovie>

    @GET("3/movie/{movieId}")
    suspend fun getMovie(
        @Path("movieId") movieId: Int
    ): Response<RemoteMovie>

    @GET("3/trending/${TmdbApiTiedConstants.AvailableMediaTypes.MOVIE}/{timeWindow}")
    suspend fun getTrendingMovies(
        @Path("timeWindow") timeWindow: String,
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteMovie>>

    @GET("3/movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteMovie>>

    @GET("3/movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteMovie>>

    @GET("4/discover/movie")
    suspend fun getDiscoveredMovies(
        @QueryMap(encoded = false) queryMap: Map<String, String?>,
        @Query("page", encoded = false) page: Int = 1
    ): Response<PagedPayload<RemoteMovie>>

    @GET("3/genre/movie/list")
    suspend fun getMovieGenres(): Response<GenrePayload>
}