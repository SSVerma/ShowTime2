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

    @GET("3/person/{personId}/tagged_images")
    suspend fun getPersonTaggedImages(
        @Path("personId") personId: Int,
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteImageShot>>

    @GET("3/genre/tv/list")
    suspend fun getTvGenres(): Response<GenrePayload>

    @GET("3/tv/popular")
    suspend fun getPopularTvShows(
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteTvShow>>

    @GET("3/tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteTvShow>>

    @GET("3/tv/on_the_air")
    suspend fun getOnTheAirTvShows(
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteTvShow>>

    @GET("3/tv/airing_today")
    suspend fun getTodayAiringTvShows(
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteTvShow>>

    @GET("3/tv/{tvShowId}")
    suspend fun getTvShowDetails(
        @Path("tvShowId") tvShowId: Int,
        @QueryMap queryMap: Map<String, String>,
    ): Response<RemoteTvShow>

    @GET("4/discover/tv")
    suspend fun getDiscoveredTvShows(
        @QueryMap queryMap: Map<String, String>,
        @Query("page", encoded = false) page: Int = 1
    ): Response<PagedPayload<RemoteTvShow>>

    @GET("3/trending/${TmdbApiTiedConstants.AvailableMediaTypes.TV}/{timeWindow}")
    suspend fun getTrendingTvShows(
        @Path("timeWindow") timeWindow: String,
        @Query("page") page: Int = 1
    ): Response<PagedPayload<RemoteTvShow>>

    @GET("3/tv/{tvShowId}/reviews")
    suspend fun getTvShowReviews(
        @Path("tvShowId") tvShowId: Int,
        @Query("page") page: Int
    ): Response<PagedPayload<RemoteReview>>

    @GET("3/tv/{tvShowId}/season/{seasonNumber}")
    suspend fun getTvSeason(
        @Path("tvShowId") tvShowId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @QueryMap queryMap: Map<String, String>,
    ): Response<RemoteTvSeason>
}