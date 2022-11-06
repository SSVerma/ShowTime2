package com.ssverma.api.service.tmdb.request

import com.google.gson.annotations.SerializedName

class RequestTokenBody(
    @SerializedName("redirect_to")
    val redirectTo: String
)

class AccessTokenBody(
    @SerializedName("request_token")
    val requestToken: String
)

class LogoutBody(
    @SerializedName("access_token")
    val accessToken: String
)

class SessionBody(
    @SerializedName("access_token")
    val accessToken: String
)