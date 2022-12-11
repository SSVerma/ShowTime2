package com.ssverma.api.service.tmdb.request

import com.google.gson.annotations.SerializedName

class FavoriteMediaBody(
    @SerializedName("media_type")
    val mediaType: String,

    @SerializedName("media_id")
    val mediaId: Int,

    @SerializedName("favorite")
    val favorite: Boolean
)