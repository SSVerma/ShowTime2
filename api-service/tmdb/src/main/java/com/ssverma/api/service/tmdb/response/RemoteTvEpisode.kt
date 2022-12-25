package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteTvEpisode(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val title: String?,

    @SerializedName("still_path")
    val posterPath: String?,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("air_date")
    val airDate: String?,

    @SerializedName("episode_number")
    val episodeNumber: Int,

    @SerializedName("season_number")
    val seasonNumber: Int,

    @SerializedName("vote_average")
    val voteAvg: Float,

    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName("credits")
    val credit: RemoteCredit?,

    @SerializedName("guest_stars")
    val guestStars: List<RemoteCast>?,

    @SerializedName("images")
    val imagePayload: ImagePayload?,

    @SerializedName("videos")
    val videoPayload: VideoPayload?,
)