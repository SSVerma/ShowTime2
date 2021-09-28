package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class RemoteTvShow(
    @SerializedName("id")
    val id: Int,

    @SerializedName("backdrop_path")
    val backdropPath: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("name")
    val title: String?,

    @SerializedName("tagline")
    val tagline: String?,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("vote_average")
    val voteAvg: Float,

    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName("first_air_date")
    val firstAirDate: String?,

    @SerializedName("popularity")
    val popularity: Float,

    @SerializedName("original_language")
    val originalLanguage: String?,

    @SerializedName("number_of_seasons")
    val seasonCount: Int,

    @SerializedName("number_of_episodes")
    val episodeCount: Int,

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

    @SerializedName("reviews")
    val reviews: PagedPayload<RemoteReview>?,

    @SerializedName("similar")
    val similarTvShows: PagedPayload<RemoteTvShow>?,

    @SerializedName("recommendations")
    val recommendations: PagedPayload<RemoteTvShow>?,

    @SerializedName("seasons")
    val seasons: List<RemoteTvSeason>?
)