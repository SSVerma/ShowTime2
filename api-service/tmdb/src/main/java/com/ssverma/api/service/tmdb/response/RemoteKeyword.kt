package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteKeyword(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?
)

class KeywordPayload(
    @SerializedName("keywords", alternate = ["results"])
    val keywords: List<RemoteKeyword>?
)