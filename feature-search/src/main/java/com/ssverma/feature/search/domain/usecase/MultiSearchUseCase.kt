package com.ssverma.feature.search.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.domain.repository.SearchRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.usecase.FlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MultiSearchUseCase @Inject constructor(
    @DefaultDispatcher dispatcher: CoroutineDispatcher,
    private val searchRepository: SearchRepository
) : FlowUseCase<Flow<String>, Result<List<SearchSuggestion>, Failure.CoreFailure>>(dispatcher) {

    companion object {
        private const val DebounceTimeMs = 300L
    }

    override fun execute(params: Flow<String>): Flow<Result<List<SearchSuggestion>, Failure.CoreFailure>> {
        return params.debounce(DebounceTimeMs)
            .distinctUntilChanged()
            .mapLatest { query ->
                if (query.isBlank()) {
                    Result.Success(emptyList())
                } else {
                    searchRepository.performMultiSearch(query = query)
                }
            }
    }
}