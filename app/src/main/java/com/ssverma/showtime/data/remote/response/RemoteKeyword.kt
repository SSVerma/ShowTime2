package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

@Deprecated("use -> api.service.tmdb")
class RemoteKeyword(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?
)

@Deprecated("use -> api.service.tmdb")
class KeywordPayload(
    @SerializedName("keywords", alternate = ["results"])
    val keywords: List<RemoteKeyword>?
)