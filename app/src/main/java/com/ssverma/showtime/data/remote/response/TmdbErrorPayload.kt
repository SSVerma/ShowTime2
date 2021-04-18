package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class TmdbErrorPayload(
    @SerializedName("status_message")
    val statusMessage: String?,

    @SerializedName("status_code")
    val statusCode: Int
)