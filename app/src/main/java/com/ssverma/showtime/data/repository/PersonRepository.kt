package com.ssverma.showtime.data.repository

import com.ssverma.showtime.api.TmdbApiService
import com.ssverma.showtime.api.makeTmdbApiRequest
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.model.asPerson
import com.ssverma.showtime.extension.asDomainFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PersonRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {
    fun fetchPersonDetails(
        personId: Int,
        queryMap: Map<String, String> = emptyMap()
    ): Flow<Result<Person>> {
        return makeTmdbApiRequest {
            tmdbApiService.getPersonDetails(personId = personId, queryMap = queryMap)
        }.asDomainFlow { it.payload.asPerson() }
    }
}