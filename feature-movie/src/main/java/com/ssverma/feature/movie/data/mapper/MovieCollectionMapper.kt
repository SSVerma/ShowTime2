package com.ssverma.feature.movie.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteMovieCollection
import com.ssverma.shared.domain.model.movie.MovieCollection

fun RemoteMovieCollection.asMovieCollection(): MovieCollection {
    return MovieCollection(
        id = id,
        name = name.orEmpty(),
        posterImageUrl = posterPath.convertToFullTmdbImageUrl(),
        backdropImageUrl = backdropPath.convertToFullTmdbImageUrl()
    )
}