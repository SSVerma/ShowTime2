package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.convertToTmdbBackdropUrl
import com.ssverma.api.service.tmdb.convertToTmdbPosterUrl
import com.ssverma.api.service.tmdb.response.RemoteMovieCollection
import com.ssverma.shared.domain.model.movie.MovieCollection

fun RemoteMovieCollection.asMovieCollection(): MovieCollection {
    return MovieCollection(
        id = id,
        name = name.orEmpty(),
        posterImageUrl = posterPath.convertToTmdbPosterUrl(),
        backdropImageUrl = backdropPath.convertToTmdbBackdropUrl()
    )
}
