package com.ssverma.showtime.domain.usecase.person

import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.person.PersonFailure
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.model.person.PersonDetailsConfig
import com.ssverma.showtime.domain.repository.PersonRepository
import com.ssverma.showtime.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class PersonDetailsUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val personRepository: PersonRepository
) : UseCase<PersonDetailsConfig, DomainResult<Person, Failure<PersonFailure>>>(coroutineDispatcher) {

    override suspend fun execute(
        params: PersonDetailsConfig
    ): DomainResult<Person, Failure<PersonFailure>> {
        return personRepository.fetchPersonDetails(
            personDetailsConfig = params
        )
    }
}