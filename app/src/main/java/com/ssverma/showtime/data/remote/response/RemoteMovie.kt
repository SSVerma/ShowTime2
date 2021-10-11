package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class RemoteMovie(
    @SerializedName("id")
    val id: Int,

    @SerializedName("imdb_id")
    val imdbId: String?,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("budget")
    val budget: Long,

    @SerializedName("title")
    val title: String?,

    @SerializedName("tagline")
    val tagline: String?,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("video")
    val videoAvailable: Boolean,

    @SerializedName("vote_average")
    val voteAvg: Float,

    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName("release_date")
    val releaseDate: String?,

    @SerializedName("revenue")
    val revenue: Long,

    @SerializedName("runtime")
    val runtime: Int,

    @SerializedName("popularity")
    val popularity: Float,

    @SerializedName("original_language")
    val originalLanguage: String?,

    @SerializedName("credits")
    val credit: RemoteCredit?,

    @SerializedName("keywords")
    val keywordPayload: KeywordPayload?,

    @SerializedName("images")
    val imagePayload: ImagePayload?,

    @SerializedName("videos")
    val videoPayload: VideoPayload?,

    @SerializedName("genres")
    val genres: List<RemoteGenre>?,

    @SerializedName("belongs_to_collection")
    val collection: RemoteMovieCollection?,

    @SerializedName("reviews")
    val reviews: PagedPayload<RemoteReview>?,

    @SerializedName("similar")
    val similarMovies: PagedPayload<RemoteMovie>?,

    @SerializedName("recommendations")
    val recommendations: PagedPayload<RemoteMovie>?
)