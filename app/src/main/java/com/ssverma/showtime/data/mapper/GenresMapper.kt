package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.response.RemoteGenre
import com.ssverma.showtime.domain.model.Genre
import javax.inject.Inject

class GenresMapper @Inject constructor() : ListMapper<RemoteGenre, Genre>() {
    override suspend fun mapItem(input: RemoteGenre): Genre {
        return input.asGenre()
    }
}

fun RemoteGenre.asGenre(): Genre {
    return Genre(id = id, name = name.orEmpty())
}

fun List<RemoteGenre>.asGenres(): List<Genre> {
    return map { it.asGenre() }
}