package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RequestTokenPayload(
    @SerializedName("request_token")
    val requestToken: String?
)

class AccessTokenPayload(
    @SerializedName("access_token")
    val accessToken: String?,

    @SerializedName("account_id")
    val accountId: String?
)