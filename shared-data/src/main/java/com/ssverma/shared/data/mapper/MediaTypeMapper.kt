package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.shared.domain.model.MediaType
import javax.inject.Inject

class TmdbMediaTypeMapper @Inject constructor() : Mapper<String, MediaType>() {
    override suspend fun map(input: String): MediaType {
        return when (input) {
            TmdbApiTiedConstants.AvailableMediaTypes.Movie -> {
                MediaType.Movie
            }
            TmdbApiTiedConstants.AvailableMediaTypes.Tv -> {
                MediaType.Tv
            }
            TmdbApiTiedConstants.AvailableMediaTypes.Person -> {
                MediaType.Person
            }
            else -> {
                MediaType.Unknown
            }
        }
    }
}