package com.ssverma.showtime.domain.repository

import androidx.paging.PagingData
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.failure.person.PersonFailure
import com.ssverma.core.domain.model.ImageShot
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.model.person.PersonDetailsConfig
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    suspend fun fetchPersonDetails(
        personDetailsConfig: PersonDetailsConfig
    ): Result<Person, Failure<PersonFailure>>

    fun fetchPopularPersonsGradually(): Flow<PagingData<Person>>

    fun fetchPersonImagesGradually(personId: Int): Flow<PagingData<ImageShot>>
}