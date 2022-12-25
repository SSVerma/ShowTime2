package com.ssverma.feature.search.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.repository.SearchRepository
import com.ssverma.shared.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class DeleteSearchHistoryUseCase @Inject constructor(
    @DefaultDispatcher
    private val coroutineDispatcher: CoroutineDispatcher,
    private val searchRepository: SearchRepository
) : UseCase<SearchHistory, Unit>(coroutineDispatcher) {

    override suspend fun execute(params: SearchHistory) {
        searchRepository.deleteSearchedEntry(historyEntryId = params.id)
    }
}