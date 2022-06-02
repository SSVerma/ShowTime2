package com.ssverma.showtime.domain.usecase.person

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.UseCase
import com.ssverma.showtime.domain.failure.person.PersonFailure
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.model.person.PersonDetailsConfig
import com.ssverma.showtime.domain.repository.PersonRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class PersonDetailsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val personRepository: PersonRepository
) : UseCase<PersonDetailsConfig, Result<Person, Failure<PersonFailure>>>(coroutineDispatcher) {

    override suspend fun execute(
        params: PersonDetailsConfig
    ): Result<Person, Failure<PersonFailure>> {
        return personRepository.fetchPersonDetails(
            personDetailsConfig = params
        )
    }
}