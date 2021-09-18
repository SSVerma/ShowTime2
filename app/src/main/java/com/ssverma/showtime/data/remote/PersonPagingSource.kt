package com.ssverma.showtime.data.remote

import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.data.remote.response.RemotePerson
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.model.asPersons
import retrofit2.Response

class PersonPagingSource(
    private val personApiCall: suspend (page: Int) -> Response<PagedPayload<RemotePerson>>
) : TmdbPagingSource<RemotePerson, Person>(
    tmdbApiCall = { page -> personApiCall(page) },
    mapRemoteToDomain = { it.asPersons() }
)