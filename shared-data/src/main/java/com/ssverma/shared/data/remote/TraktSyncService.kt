package com.ssverma.shared.data.remote

import com.google.gson.annotations.SerializedName
import com.ssverma.core.networking.adapter.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface TraktSyncService {

    @Headers(
        "Content-Type: application/json",
        "trakt-api-version: 2"
    )
    @GET("sync/watchlist")
    suspend fun getWatchlist(
        @Header("Authorization") bearerToken: String,
        @Header("trakt-api-key") clientId: String
    ): ApiResponse<List<TraktWatchlistItemPayload>, Any>

    @Headers(
        "Content-Type: application/json",
        "trakt-api-version: 2"
    )
    @POST("sync/watchlist")
    suspend fun addToWatchlist(
        @Header("Authorization") bearerToken: String,
        @Header("trakt-api-key") clientId: String,
        @Body payload: TraktSyncBody
    ): ApiResponse<TraktSyncResponsePayload, Any>

    @Headers(
        "Content-Type: application/json",
        "trakt-api-version: 2"
    )
    @GET("sync/history")
    suspend fun getHistory(
        @Header("Authorization") bearerToken: String,
        @Header("trakt-api-key") clientId: String,
        @Query("limit") limit: Int = 100
    ): ApiResponse<List<TraktHistoryItemPayload>, Any>

    @Headers(
        "Content-Type: application/json",
        "trakt-api-version: 2"
    )
    @POST("sync/history")
    suspend fun addToHistory(
        @Header("Authorization") bearerToken: String,
        @Header("trakt-api-key") clientId: String,
        @Body payload: TraktSyncBody
    ): ApiResponse<TraktSyncResponsePayload, Any>

    @Headers(
        "Content-Type: application/json",
        "trakt-api-version: 2"
    )
    @GET("shows/progress/watched?hidden=false&specials=false&count_specials=false")
    suspend fun getWatchedShowProgress(
        @Header("Authorization") bearerToken: String,
        @Header("trakt-api-key") clientId: String
    ): ApiResponse<List<TraktShowProgressPayload>, Any>
}

// Payloads

data class TraktSyncBody(
    @SerializedName("movies")
    val movies: List<TraktMediaItemIdentifier> = emptyList(),
    @SerializedName("shows")
    val shows: List<TraktMediaItemIdentifier> = emptyList(),
    @SerializedName("episodes")
    val episodes: List<TraktEpisodeIdentifier> = emptyList()
)

data class TraktMediaItemIdentifier(
    @SerializedName("ids")
    val ids: TraktIds,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("year")
    val year: Int? = null
)

data class TraktEpisodeIdentifier(
    @SerializedName("ids")
    val ids: TraktIds,
    @SerializedName("season")
    val season: Int? = null,
    @SerializedName("number")
    val number: Int? = null
)

data class TraktIds(
    @SerializedName("trakt")
    val trakt: Int? = null,
    @SerializedName("tmdb")
    val tmdb: Int? = null,
    @SerializedName("imdb")
    val imdb: String? = null
)

data class TraktWatchlistItemPayload(
    @SerializedName("type")
    val type: String, // "movie" or "show"
    @SerializedName("listed_at")
    val listedAt: String?,
    @SerializedName("movie")
    val movie: TraktMoviePayload?,
    @SerializedName("show")
    val show: TraktShowPayload?
)

data class TraktHistoryItemPayload(
    @SerializedName("id")
    val id: Long,
    @SerializedName("watched_at")
    val watchedAt: String?,
    @SerializedName("action")
    val action: String?,
    @SerializedName("type")
    val type: String, // "movie" or "episode"
    @SerializedName("movie")
    val movie: TraktMoviePayload?,
    @SerializedName("show")
    val show: TraktShowPayload?,
    @SerializedName("episode")
    val episode: TraktEpisodePayload?
)

data class TraktMoviePayload(
    @SerializedName("title")
    val title: String,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("ids")
    val ids: TraktIds
)

data class TraktShowPayload(
    @SerializedName("title")
    val title: String,
    @SerializedName("year")
    val year: Int?,
    @SerializedName("ids")
    val ids: TraktIds
)

data class TraktEpisodePayload(
    @SerializedName("season")
    val season: Int,
    @SerializedName("number")
    val number: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("ids")
    val ids: TraktIds
)

data class TraktShowProgressPayload(
    @SerializedName("aired")
    val aired: Int,
    @SerializedName("completed")
    val completed: Int,
    @SerializedName("last_watched_at")
    val lastWatchedAt: String?,
    @SerializedName("show")
    val show: TraktShowPayload,
    @SerializedName("next_episode")
    val nextEpisode: TraktEpisodePayload?
)

data class TraktSyncResponsePayload(
    @SerializedName("added")
    val added: TraktSyncCountPayload?,
    @SerializedName("existing")
    val existing: TraktSyncCountPayload?,
    @SerializedName("deleted")
    val deleted: TraktSyncCountPayload?,
    @SerializedName("not_found")
    val notFound: TraktSyncNotFoundPayload?
)

data class TraktSyncCountPayload(
    @SerializedName("movies")
    val movies: Int = 0,
    @SerializedName("shows")
    val shows: Int = 0,
    @SerializedName("episodes")
    val episodes: Int = 0
)

data class TraktSyncNotFoundPayload(
    @SerializedName("movies")
    val movies: List<TraktMediaItemIdentifier> = emptyList(),
    @SerializedName("shows")
    val shows: List<TraktMediaItemIdentifier> = emptyList()
)
