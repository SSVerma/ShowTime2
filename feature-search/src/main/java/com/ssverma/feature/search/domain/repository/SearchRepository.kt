package com.ssverma.feature.search.domain.repository

import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun performMultiSearch(
        query: String
    ): Result<List<SearchSuggestion>, Failure.CoreFailure>

    fun loadLatestHistories(limit: Int): Flow<List<SearchHistory>>

    suspend fun insertSearchedEntry(searchHistory: SearchHistory)

    suspend fun deleteSearchedEntry(historyEntryId: Int)
}