package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class MediaStatsPayload(
    @SerializedName("id")
    val mediaId: Int,

    @SerializedName("favorite")
    val favorite: Boolean,

    @SerializedName("watchlist")
    val inWatchlist: Boolean,

    @SerializedName("rated")
    val rating: Any?
)

class MediaRating(
    @SerializedName("value")
    val value: Int
)