package com.ssverma.feature.search.data.repository

import com.ssverma.api.service.tmdb.response.RemoteMultiSearchSuggestion
import com.ssverma.core.di.AppScoped
import com.ssverma.feature.search.data.local.SearchLocalDataSource
import com.ssverma.feature.search.data.mapper.HistoryToLocalHistoryMapper
import com.ssverma.feature.search.data.mapper.LocalHistoryToHistoryListMapper
import com.ssverma.feature.search.data.remote.SearchRemoteDataSource
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.domain.repository.SearchRepository
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultSearchRepository @Inject constructor(
    @AppScoped
    private val externalScope: CoroutineScope,
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val searchLocalDataSource: SearchLocalDataSource,
    private val searchMapper: @JvmSuppressWildcards ListMapper<RemoteMultiSearchSuggestion, SearchSuggestion>,
    private val searchHistoryMapper: HistoryToLocalHistoryMapper,
    private val searchHistoriesMapper: LocalHistoryToHistoryListMapper
) : SearchRepository {

    override suspend fun performMultiSearch(
        query: String
    ): Result<List<SearchSuggestion>, Failure.CoreFailure> {
        val apiResponse = searchRemoteDataSource.performMultiSearch(query = query)

        return apiResponse.asDomainResult { searchMapper.map(it.body.results.orEmpty()) }
    }

    override fun loadLatestHistories(limit: Int): Flow<List<SearchHistory>> {
        return searchLocalDataSource.loadLatestHistoryEntries(limit).mapLatest { localHistory ->
            searchHistoriesMapper.map(localHistory)
        }
    }

    override suspend fun insertSearchedEntry(searchHistory: SearchHistory) {
        externalScope.launch {
            val localHistory = searchHistoryMapper.map(searchHistory)
            searchLocalDataSource.insertHistory(localHistory)
        }.join()
    }

    override suspend fun deleteSearchedEntry(historyEntryId: Int) {
        externalScope.launch {
            searchLocalDataSource.deleteHistory(historyEntryId)
        }.join()
    }
}