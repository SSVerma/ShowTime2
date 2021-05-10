package com.ssverma.showtime.domain.model

import com.ssverma.showtime.data.remote.response.RemoteGenre

data class Genre(
    val id: Int,
    val name: String
)

fun RemoteGenre.asGenre(): Genre {
    return Genre(id = id, name = name ?: "")
}

fun List<RemoteGenre>.asGenres(): List<Genre> {
    return map { it.asGenre() }
}