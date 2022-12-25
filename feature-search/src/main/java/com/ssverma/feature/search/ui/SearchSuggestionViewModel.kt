package com.ssverma.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.domain.usecase.DeleteSearchHistoryUseCase
import com.ssverma.feature.search.domain.usecase.LoadSearchHistoryUseCase
import com.ssverma.feature.search.domain.usecase.MultiSearchUseCase
import com.ssverma.feature.search.domain.usecase.UpdateSearchHistoryUseCase
import com.ssverma.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchSuggestionViewModel @Inject constructor(
    searchUseCase: MultiSearchUseCase,
    loadSearchHistoryUseCase: LoadSearchHistoryUseCase,
    private val updateSearchHistoryUseCase: UpdateSearchHistoryUseCase,
    private val deleteHistoryUseCase: DeleteSearchHistoryUseCase
) : ViewModel() {

    private val _searchSuggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val searchSuggestions = _searchSuggestions.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        val suggestionsStream = searchUseCase(searchQuery)

        viewModelScope.launch {
            suggestionsStream.collect { result ->
                if (result is Result.Success) {
                    _searchSuggestions.value = result.data
                }
            }
        }

        val historyItemsStream = loadSearchHistoryUseCase()

        viewModelScope.launch {
            historyItemsStream.collectLatest { historyItems ->
                _searchHistory.value = historyItems
            }
        }
    }

    fun onQueryUpdated(query: String) {
        _searchQuery.update { query }
    }

    fun saveSearchHistory(suggestion: SearchSuggestion) {
        viewModelScope.launch { updateSearchHistoryUseCase(suggestion) }
    }

    fun clearHistoryItem(history: SearchHistory) {
        viewModelScope.launch {
            deleteHistoryUseCase(history)
        }
    }

}