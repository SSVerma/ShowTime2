package com.ssverma.feature.person.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.usecase.NoParamFlowUseCase
import com.ssverma.feature.person.domain.model.Person
import com.ssverma.feature.person.domain.repository.PersonRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PopularPersonsPaginatedUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val personRepository: PersonRepository
) : NoParamFlowUseCase<PagingData<Person>>(coroutineDispatcher) {

    override fun execute(): Flow<PagingData<Person>> {
        return personRepository.fetchPopularPersonsGradually()
    }
}