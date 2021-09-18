package com.ssverma.showtime.api

import com.ssverma.showtime.data.remote.response.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface TmdbApiService {
    @GET("https://demo3320320.mockable.io/movies")
    suspend fun getMockMovies(@Query("page") page: Int): Response<PagedPayload<RemoteMovie>>

    @GET("https://demo3320320.mockable.io/movieDetails")
    suspend fun getMockMovieDetails(): Response<RemoteMovie>

    @GET("3/movie/latest")
    suspend fun getLatestMovie(): Response<RemoteMovie>

    @GET("3/movie/{movieId}")
    suspend fun getMovieDetails(
        @Path("movieId") movieId: Int,
        @QueryMap queryMap: Map<String, String>,
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
        @QueryMap queryMap: Map<String, String>,
        @Query("page", encoded = false) page: Int = 1
    ): Response<PagedPayload<RemoteMovie>>

    @GET("3/genre/movie/list")
    suspend fun getMovieGenres(): Response<GenrePayload>

    @GET("3/movie/{movieId}/reviews")
    suspend fun getMovieReviews(
        @Path("movieId") movieId: Int,
        @Query("page") page: Int
    ): Response<PagedPayload<RemoteReview>>

    @GET("3/person/{personId}")
    suspend fun getPersonDetails(
        @Path("personId") personId: Int,
        @QueryMap queryMap: Map<String, String>,
    ): Response<RemotePerson>

    @GET("3/person/popular")
    suspend fun getPopularPersons(
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemotePerson>>
}