package com.ssverma.feature.person.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.usecase.UseCase
import com.ssverma.feature.person.domain.failure.PersonFailure
import com.ssverma.feature.person.domain.model.Person
import com.ssverma.feature.person.domain.model.PersonDetailsConfig
import com.ssverma.feature.person.domain.repository.PersonRepository
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