package com.ssverma.showtime.domain.model

import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.data.remote.response.RemoteMovieCollection

data class MovieCollection(
    val id: Int,
    val name: String,
    val posterImageUrl: String,
    val backdropImageUrl: String
)

fun RemoteMovieCollection.asMovieCollection(): MovieCollection {
    return MovieCollection(
        id = id,
        name = name ?: "",
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        backdropImageUrl = backdropPath.convertToFullTmdbImageUrl()
    )
}