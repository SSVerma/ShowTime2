package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class GenrePayload(
    @SerializedName("genres")
    val genres: List<RemoteGenre>?
)

class RemoteGenre(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?
)