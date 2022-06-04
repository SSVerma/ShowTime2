package com.ssverma.feature.person.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.api.service.tmdb.TMDB_API_PAGE_SIZE
import com.ssverma.api.service.tmdb.paging.ImagePagingSource
import com.ssverma.api.service.tmdb.paging.PersonPagingSource
import com.ssverma.api.service.tmdb.response.RemoteImageShot
import com.ssverma.api.service.tmdb.response.RemotePerson
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.feature.person.data.remote.PersonRemoteDataSource
import com.ssverma.feature.person.domain.failure.PersonFailure
import com.ssverma.feature.person.domain.model.Person
import com.ssverma.feature.person.domain.model.PersonDetailsConfig
import com.ssverma.feature.person.domain.repository.PersonRepository
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.Mapper
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.data.mapper.asQueryMap
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultPersonRepository @Inject constructor(
    private val personRemoteDataSource: PersonRemoteDataSource,
    private val personsMapper: @JvmSuppressWildcards ListMapper<RemotePerson, Person>,
    private val imageShotsMapper: @JvmSuppressWildcards ListMapper<RemoteImageShot, ImageShot>,
    private val personMapper: @JvmSuppressWildcards Mapper<RemotePerson, Person>,
) : PersonRepository {

    override suspend fun fetchPersonDetails(
        personDetailsConfig: PersonDetailsConfig
    ): Result<Person, Failure<PersonFailure>> {
        val apiResponse = personRemoteDataSource.fetchPersonDetails(
            personId = personDetailsConfig.personId,
            queryMap = personDetailsConfig.appendable.asQueryMap()
        )

        return apiResponse.asDomainResult { personMapper.map(it.body) }
    }

    override fun fetchPopularPersonsGradually(): Flow<PagingData<Person>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                PersonPagingSource(
                    personApiCall = { pageNumber ->
                        personRemoteDataSource.fetchPopularPersonsGradually(
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { personsMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchPersonImagesGradually(personId: Int): Flow<PagingData<ImageShot>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                ImagePagingSource(
                    imagesApiCall = { pageNumber ->
                        personRemoteDataSource.fetchPersonImagesGradually(
                            personId = personId,
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { imageShotsMapper.map(it) }
                )
            }
        ).flow
    }
}