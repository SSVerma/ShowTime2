package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class SessionPayload(
    @SerializedName("session_id")
    val sessionId: String?
)