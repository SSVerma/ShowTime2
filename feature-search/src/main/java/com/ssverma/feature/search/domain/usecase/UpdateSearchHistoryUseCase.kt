package com.ssverma.feature.search.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.domain.repository.SearchRepository
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.usecase.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpdateSearchHistoryUseCase @Inject constructor(
    @DefaultDispatcher
    private val coroutineDispatcher: CoroutineDispatcher,
    private val searchRepository: SearchRepository
) : UseCase<SearchSuggestion, Unit>(coroutineDispatcher) {

    override suspend fun execute(params: SearchSuggestion) {
        val searchHistory = when (params) {
            is SearchSuggestion.Movie -> {
                SearchHistory(
                    id = params.id,
                    name = params.title,
                    mediaType = MediaType.Movie
                )
            }
            is SearchSuggestion.Person -> {
                SearchHistory(
                    id = params.id,
                    name = params.name,
                    mediaType = MediaType.Person
                )
            }
            is SearchSuggestion.TvShow -> {
                SearchHistory(
                    id = params.id,
                    name = params.title,
                    mediaType = MediaType.Tv
                )
            }
            SearchSuggestion.None -> {
                throw IllegalArgumentException("Invalid suggestion type")
            }
        }
        searchRepository.insertSearchedEntry(searchHistory = searchHistory)
    }
}