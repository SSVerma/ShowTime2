package com.ssverma.showtime.domain.usecase.person

import androidx.paging.PagingData
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.model.ImageShot
import com.ssverma.showtime.domain.repository.PersonRepository
import com.ssverma.showtime.domain.usecase.FlowUseCase
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