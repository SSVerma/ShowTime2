package com.ssverma.showtime.data.mapper

import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.showtime.api.AppendableQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.domain.model.movie.MovieDetailsAppendable
import com.ssverma.showtime.domain.model.movie.MovieDetailsConfig

fun MovieDetailsConfig.asQueryMap(): Map<String, String> {
    val queryBuilder = QueryMultiValue.andBuilder()

    this.appendable.forEach { appendable ->
        when (appendable) {
            MovieDetailsAppendable.Credits -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Credits)
            }
            MovieDetailsAppendable.Images -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Images)
            }
            MovieDetailsAppendable.Keywords -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Keywords)
            }
            MovieDetailsAppendable.Lists -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Lists)
            }
            MovieDetailsAppendable.Recommendations -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Recommendations)
            }
            MovieDetailsAppendable.Reviews -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Reviews)
            }
            MovieDetailsAppendable.Similar -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Similar)
            }
            MovieDetailsAppendable.Videos -> {
                queryBuilder.and(TmdbApiTiedConstants.AppendableResponseTypes.Videos)
            }
        }
    }

    return AppendableQueryMap.of(queryBuilder)
}