package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteLanguage(
    @SerializedName("iso_639_1")
    val iso6391: String,

    @SerializedName("english_name")
    val englishName: String,

    @SerializedName("name")
    val name: String
)
