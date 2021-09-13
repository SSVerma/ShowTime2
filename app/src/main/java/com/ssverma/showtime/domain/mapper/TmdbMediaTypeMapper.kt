package com.ssverma.showtime.domain.mapper

import com.ssverma.showtime.R
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.domain.model.MediaType

object TmdbMediaTypeMapper : Mapper<String, MediaType>() {
    override suspend fun map(input: String): MediaType {
        return when (input) {
            TmdbApiTiedConstants.AvailableMediaType.Movie -> {
                MediaType.Movie(displayNameRes = R.string.movie, input)
            }
            TmdbApiTiedConstants.AvailableMediaType.Tv -> {
                MediaType.Tv(displayNameRes = R.string.tv, input)
            }
            else -> {
                MediaType.Unknown
            }
        }
    }
}