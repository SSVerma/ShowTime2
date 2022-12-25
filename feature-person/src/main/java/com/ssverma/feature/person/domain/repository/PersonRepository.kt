package com.ssverma.feature.person.domain.repository

import androidx.paging.PagingData
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.feature.person.domain.failure.PersonFailure
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.feature.person.domain.model.PersonDetailsConfig
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    suspend fun fetchPersonDetails(
        personDetailsConfig: PersonDetailsConfig
    ): Result<Person, Failure<PersonFailure>>

    fun fetchPopularPersonsGradually(): Flow<PagingData<Person>>

    fun fetchPersonImagesGradually(personId: Int): Flow<PagingData<ImageShot>>
}