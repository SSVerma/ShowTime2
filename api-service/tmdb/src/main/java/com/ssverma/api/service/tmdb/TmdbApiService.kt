package com.ssverma.api.service.tmdb

import com.ssverma.api.service.tmdb.request.*
import com.ssverma.api.service.tmdb.response.*
import com.ssverma.core.networking.adapter.ApiResponse
import retrofit2.http.*

typealias TmdbApiResponse<T> = ApiResponse<T, TmdbErrorPayload>

interface TmdbApiService {
    @GET("3/movie/latest")
    suspend fun getLatestMovie(): TmdbApiResponse<RemoteMovie>

    @GET("3/movie/{movieId}")
    suspend fun getMovieDetails(
        @Path("movieId") movieId: Int,
        @QueryMap queryMap: Map<String, String>,
    ): TmdbApiResponse<RemoteMovie>

    @GET("3/trending/${TmdbApiTiedConstants.AvailableMediaTypes.Movie}/{timeWindow}")
    suspend fun getTrendingMovies(
        @Path("timeWindow") timeWindow: String,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    @GET("3/movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    @GET("3/movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    @GET("4/discover/movie")
    suspend fun getDiscoveredMovies(
        @QueryMap queryMap: Map<String, String>,
        @Query("page", encoded = false) page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    @GET("3/genre/movie/list")
    suspend fun getMovieGenres(): TmdbApiResponse<GenrePayload>

    @GET("3/movie/{movieId}/reviews")
    suspend fun getMovieReviews(
        @Path("movieId") movieId: Int,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteReview>>

    @GET("3/person/{personId}")
    suspend fun getPersonDetails(
        @Path("personId") personId: Int,
        @QueryMap queryMap: Map<String, String>,
    ): TmdbApiResponse<RemotePerson>

    @GET("3/person/popular")
    suspend fun getPopularPersons(
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemotePerson>>

    @GET("3/person/{personId}/tagged_images")
    suspend fun getPersonTaggedImages(
        @Path("personId") personId: Int,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteImageShot>>

    @GET("3/genre/tv/list")
    suspend fun getTvGenres(): TmdbApiResponse<GenrePayload>

    @GET("3/tv/popular")
    suspend fun getPopularTvShows(
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/tv/on_the_air")
    suspend fun getOnTheAirTvShows(
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/tv/airing_today")
    suspend fun getTodayAiringTvShows(
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/tv/{tvShowId}")
    suspend fun getTvShowDetails(
        @Path("tvShowId") tvShowId: Int,
        @QueryMap queryMap: Map<String, String>,
    ): TmdbApiResponse<RemoteTvShow>

    @GET("4/discover/tv")
    suspend fun getDiscoveredTvShows(
        @QueryMap queryMap: Map<String, String>,
        @Query("page", encoded = false) page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/trending/${TmdbApiTiedConstants.AvailableMediaTypes.Tv}/{timeWindow}")
    suspend fun getTrendingTvShows(
        @Path("timeWindow") timeWindow: String,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/tv/{tvShowId}/reviews")
    suspend fun getTvShowReviews(
        @Path("tvShowId") tvShowId: Int,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteReview>>

    @GET("3/tv/{tvShowId}/season/{seasonNumber}")
    suspend fun getTvSeason(
        @Path("tvShowId") tvShowId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @QueryMap queryMap: Map<String, String>,
    ): TmdbApiResponse<RemoteTvSeason>

    @GET("3/tv/{tvShowId}/season/{seasonNumber}/episode/{episodeNumber}")
    suspend fun getTvEpisode(
        @Path("tvShowId") tvShowId: Int,
        @Path("seasonNumber") seasonNumber: Int,
        @Path("episodeNumber") episodeNumber: Int,
        @QueryMap queryMap: Map<String, String>,
    ): TmdbApiResponse<RemoteTvEpisode>

    @GET("3/search/multi")
    suspend fun multiSearch(
        @Query("query") query: String
    ): TmdbApiResponse<PagedPayload<RemoteMultiSearchSuggestion>>

    @POST("4/auth/request_token")
    suspend fun createRequestToken(
        @Body requestTokenBody: RequestTokenBody
    ): TmdbApiResponse<RequestTokenPayload>

    @POST("4/auth/access_token")
    suspend fun createAccessToken(
        @Body accessTokenBody: AccessTokenBody
    ): TmdbApiResponse<AccessTokenPayload>

    @HTTP(method = "DELETE", path = "4/auth/access_token", hasBody = true)
    suspend fun logout(
        @Body logoutBody: LogoutBody
    ): TmdbApiResponse<Unit>

    @POST("3/authentication/session/convert/4")
    suspend fun createSessionId(
        @Body sessionBody: SessionBody
    ): TmdbApiResponse<SessionPayload>

    @GET("3/account")
    suspend fun getAccount(
        @Query("session_id") sessionId: String
    ): TmdbApiResponse<AccountPayload>

    @POST("3/account/{accountId}/favorite")
    suspend fun markMediaAsFavorite(
        @Path("accountId") accountId: Int,
        @Query("session_id") sessionId: String,
        @Body mediaBody: FavoriteMediaBody
    ): TmdbApiResponse<Unit>

    @POST("3/account/{accountId}/watchlist")
    suspend fun markMediaInWatchlist(
        @Path("accountId") accountId: Int,
        @Query("session_id") sessionId: String,
        @Body watchlistBody: WatchlistMediaBody
    ): TmdbApiResponse<Unit>

    @GET("3/account/{accountId}/favorite/movies")
    suspend fun getFavoriteMovies(
        @Path("accountId") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    @GET("3/account/{accountId}/favorite/tv")
    suspend fun getFavoriteTvShows(
        @Path("accountId") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/movie/{movieId}/account_states")
    suspend fun getMovieAccountStats(
        @Path("movieId") movieId: Int,
        @Query("session_id") sessionId: String
    ): TmdbApiResponse<MediaStatsPayload>

    @GET("3/tv/{tvShowId}/account_states")
    suspend fun getTvShowAccountStats(
        @Path("tvShowId") tvShowId: Int,
        @Query("session_id") sessionId: String
    ): TmdbApiResponse<MediaStatsPayload>

    @GET("3/account/{accountId}/watchlist/movies")
    suspend fun getWatchlistMovies(
        @Path("accountId") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    @GET("3/account/{accountId}/watchlist/tv")
    suspend fun getWatchlistTvShows(
        @Path("accountId") accountId: Int,
        @Query("session_id") sessionId: String,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>
}