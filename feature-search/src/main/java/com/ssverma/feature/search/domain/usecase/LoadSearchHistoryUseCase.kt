package com.ssverma.feature.search.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.repository.SearchRepository
import com.ssverma.shared.domain.usecase.NoParamFlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoadSearchHistoryUseCase @Inject constructor(
    @DefaultDispatcher
    private val coroutineDispatcher: CoroutineDispatcher,
    private val searchRepository: SearchRepository
) : NoParamFlowUseCase<List<SearchHistory>>(coroutineDispatcher) {

    companion object {
        private const val MAX_HISTORY_ENTRIES = 3
    }

    override fun execute(): Flow<List<SearchHistory>> {
        return searchRepository.loadLatestHistories(limit = MAX_HISTORY_ENTRIES)
    }
}