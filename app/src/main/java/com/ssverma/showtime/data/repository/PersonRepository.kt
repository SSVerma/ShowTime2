package com.ssverma.showtime.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.showtime.api.TMDB_API_PAGE_SIZE
import com.ssverma.showtime.api.TmdbApiService
import com.ssverma.showtime.api.makeTmdbApiRequest
import com.ssverma.showtime.data.remote.PersonPagingSource
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

    fun fetchPopularPersonsGradually(): Flow<PagingData<Person>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                PersonPagingSource { page ->
                    tmdbApiService.getPopularPersons(page = page)
                }
            }
        ).flow
    }
}