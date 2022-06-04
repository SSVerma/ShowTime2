package com.ssverma.feature.person.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.model.ImageShot
import com.ssverma.core.domain.usecase.FlowUseCase
import com.ssverma.feature.person.domain.repository.PersonRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PersonImagesPaginatedUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val personRepository: PersonRepository
) : FlowUseCase<Int, PagingData<ImageShot>>(coroutineDispatcher) {

    override fun execute(params: Int): Flow<PagingData<ImageShot>> {
        return personRepository.fetchPersonImagesGradually(
            personId = params
        )
    }
}