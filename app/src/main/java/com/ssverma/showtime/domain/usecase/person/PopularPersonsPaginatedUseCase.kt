package com.ssverma.showtime.domain.usecase.person

import androidx.paging.PagingData
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.domain.repository.PersonRepository
import com.ssverma.showtime.domain.usecase.NoParamFlowUseCase
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