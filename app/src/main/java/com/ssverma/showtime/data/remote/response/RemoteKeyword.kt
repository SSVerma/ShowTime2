package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class RemoteKeyword(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?
)

class KeywordPayload(
    @SerializedName("keywords")
    val keywords: List<RemoteKeyword>?
)