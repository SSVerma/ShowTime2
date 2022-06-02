package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.core.domain.model.MediaType
import javax.inject.Inject

class TmdbMediaTypeMapper @Inject constructor() : Mapper<String, MediaType>() {
    override suspend fun map(input: String): MediaType {
        return when (input) {
            TmdbApiTiedConstants.AvailableMediaType.Movie -> {
                MediaType.Movie
            }
            TmdbApiTiedConstants.AvailableMediaType.Tv -> {
                MediaType.Tv
            }
            else -> {
                MediaType.Unknown
            }
        }
    }
}