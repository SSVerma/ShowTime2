package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

@Deprecated("use-> api.service.tmdb")
class GenrePayload(
    @SerializedName("genres")
    val genres: List<RemoteGenre>?
)

@Deprecated("use-> api.service.tmdb")
class RemoteGenre(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?
)