package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteNetwork(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("logo_path")
    val logoPath: String?,

    @SerializedName("origin_country")
    val originCountry: String?
)
