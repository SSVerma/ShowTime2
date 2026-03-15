package com.ssverma.api.service.tmdb

import com.ssverma.api.service.tmdb.request.AccessTokenBody
import com.ssverma.api.service.tmdb.request.FavoriteMediaBody
import com.ssverma.api.service.tmdb.request.LogoutBody
import com.ssverma.api.service.tmdb.request.RequestTokenBody
import com.ssverma.api.service.tmdb.request.SessionBody
import com.ssverma.api.service.tmdb.request.WatchlistMediaBody
import com.ssverma.api.service.tmdb.response.AccessTokenPayload
import com.ssverma.api.service.tmdb.response.AccountPayload
import com.ssverma.api.service.tmdb.response.GenrePayload
import com.ssverma.api.service.tmdb.response.MediaStatsPayload
import com.ssverma.api.service.tmdb.response.PagedPayload
import com.ssverma.api.service.tmdb.response.RemoteCompany
import com.ssverma.api.service.tmdb.response.RemoteImageShot
import com.ssverma.api.service.tmdb.response.RemoteKeyword
import com.ssverma.api.service.tmdb.response.RemoteLanguage
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.api.service.tmdb.response.RemoteNetwork
import com.ssverma.api.service.tmdb.response.RemotePerson
import com.ssverma.api.service.tmdb.response.RemoteReview
import com.ssverma.api.service.tmdb.response.RemoteTvEpisode
import com.ssverma.api.service.tmdb.response.RemoteTvSeason
import com.ssverma.api.service.tmdb.response.RemoteTvShow
import com.ssverma.api.service.tmdb.response.RemoteWatchProviderRegion
import com.ssverma.api.service.tmdb.response.RemoteWatchProviderResponse
import com.ssverma.api.service.tmdb.response.RequestTokenPayload
import com.ssverma.api.service.tmdb.response.SessionPayload
import com.ssverma.api.service.tmdb.response.TmdbErrorPayload
import com.ssverma.api.service.tmdb.response.WatchProviderRegionPayload
import com.ssverma.core.networking.adapter.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

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

    @GET("3/discover/movie")
    suspend fun getDiscoveredMovies(
        @QueryMap queryMap: Map<String, String>,
        @Query("page", encoded = false) page: Int
    ): TmdbApiResponse<PagedPayload<RemoteMovie>>

    @GET("3/genre/movie/list")
    suspend fun getMovieGenres(): TmdbApiResponse<GenrePayload>

    @GET("3/configuration/countries")
    suspend fun getCountries(): TmdbApiResponse<List<RemoteWatchProviderRegion>>

    @GET("3/configuration/languages")
    suspend fun getLanguages(): TmdbApiResponse<List<RemoteLanguage>>

    @GET("3/watch/providers/regions")
    suspend fun getWatchProviderRegions(): TmdbApiResponse<WatchProviderRegionPayload>

    @GET("3/movie/{movieId}/watch/providers")
    suspend fun getMovieWatchProviders(
        @Path("movieId") movieId: Int
    ): TmdbApiResponse<RemoteWatchProviderResponse>

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

    @GET("3/discover/tv")
    suspend fun getDiscoveredTvShows(
        @QueryMap queryMap: Map<String, String>,
        @Query("page", encoded = false) page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/trending/${TmdbApiTiedConstants.AvailableMediaTypes.Tv}/{timeWindow}")
    suspend fun getTrendingTvShows(
        @Path("timeWindow") timeWindow: String,
        @Query("page") page: Int
    ): TmdbApiResponse<PagedPayload<RemoteTvShow>>

    @GET("3/tv/{tvShowId}/watch/providers")
    suspend fun getTvShowWatchProviders(
        @Path("tvShowId") tvShowId: Int
    ): TmdbApiResponse<RemoteWatchProviderResponse>

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

    @GET("3/search/keyword")
    suspend fun searchKeywords(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbApiResponse<PagedPayload<RemoteKeyword>>

    @GET("3/search/company")
    suspend fun searchCompanies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbApiResponse<PagedPayload<RemoteCompany>>

    @GET("3/search/network")
    suspend fun searchNetworks(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbApiResponse<PagedPayload<RemoteNetwork>>

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
