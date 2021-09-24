package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class TvSeason(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val title: String?,

    @SerializedName("poster_path")
    val posterPath: String?,

    @SerializedName("overview")
    val overview: String?,

    @SerializedName("air_date")
    val airDate: String?,

    @SerializedName("season_number")
    val seasonNumber: Int,

    @SerializedName("episodes")
    val episodes: List<TvEpisode>?,

    @SerializedName("credits")
    val credit: RemoteCredit?,

    @SerializedName("images")
    val imagePayload: ImagePayload?,

    @SerializedName("videos")
    val videoPayload: VideoPayload?,
)