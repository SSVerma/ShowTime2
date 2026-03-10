package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteWatchProviderRegion(
    @SerializedName("iso_3166_1")
    val iso31661: String,

    @SerializedName("english_name")
    val englishName: String,

    @SerializedName("native_name")
    val nativeName: String
)

class WatchProviderRegionPayload(
    @SerializedName("results")
    val results: List<RemoteWatchProviderRegion>
)
