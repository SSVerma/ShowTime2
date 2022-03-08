package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.response.RemoteMovieCollection
import com.ssverma.showtime.api.convertToFullTmdbImageUrl
import com.ssverma.showtime.domain.model.MovieCollection

fun RemoteMovieCollection.asMovieCollection(): MovieCollection {
    return MovieCollection(
        id = id,
        name = name.orEmpty(),
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        backdropImageUrl = backdropPath.convertToFullTmdbImageUrl()
    )
}