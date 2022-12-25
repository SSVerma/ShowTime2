package com.ssverma.api.service.tmdb.request

import com.google.gson.annotations.SerializedName

class WatchlistMediaBody(
    @SerializedName("media_type")
    val mediaType: String,

    @SerializedName("media_id")
    val mediaId: Int,

    @SerializedName("watchlist")
    val inWatchlist: Boolean
)