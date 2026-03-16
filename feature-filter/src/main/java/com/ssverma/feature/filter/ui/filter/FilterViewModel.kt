package com.ssverma.feature.filter.ui.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiText
import com.ssverma.feature.filter.domain.FilterProvider
import com.ssverma.feature.filter.domain.MovieFilter
import com.ssverma.feature.filter.domain.TvFilter
import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    @param:MovieFilter private val movieFilterProvider: FilterProvider,
    @param:TvFilter private val tvShowFilterProvider: FilterProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState(filters = emptyList(), isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow<SearchQuery?>(null)

    private var isTv: Boolean = false
    private var initialOptions: List<DiscoverOption> = emptyList()
    private val cachedFilterItems = mutableMapOf<FilterId, List<FilterItem>>()

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce { query ->
                    if (query?.query.isNullOrBlank()) 0L else 500L
                }
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query == null) return@collectLatest
                    val groupId = query.groupId
                    val queryString = query.query

                    if (queryString.isBlank()) {
                        val cachedItems = cachedFilterItems[groupId] ?: emptyList()
                        updateFilterItems(groupId = groupId, newItems = cachedItems)
                        return@collectLatest
                    }

                    if (!groupId.isRemoteSearchSupported) return@collectLatest

                    val provider = if (isTv) tvShowFilterProvider else movieFilterProvider
                    _uiState.update { it.copy(isSearching = true) }
                    provider.searchFilterItems(groupId, queryString).onSuccess { items ->
                        val dynamicItems = items.map {
                            FilterItem.Dynamic(
                                id = it.id,
                                text = UiText.DynamicText(it.displayText),
                                iconUrl = it.iconUrl
                            )
                        }
                        updateFilterItems(groupId, dynamicItems)
                        _uiState.update { it.copy(isSearching = false) }
                    }.onFailure {
                        _uiState.update { it.copy(isSearching = false) }
                    }
                }
        }
    }

    fun init(
        isTv: Boolean,
        initialOptions: List<DiscoverOption> = emptyList(),
        initialSortBy: SortBy? = null,
        initialOrder: Order? = null
    ) {
        this.isTv = isTv
        this.initialOptions = initialOptions
        if (_uiState.value.filters.isNotEmpty()) return

        viewModelScope.launch {
            val flow = if (isTv) {
                tvShowFilterProvider.provideFilters()
            } else {
                movieFilterProvider.provideFilters()
            }

            flow.collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                filters = result.data.asUiFilters(
                                    initialOptions = initialOptions,
                                    initialSortBy = initialSortBy,
                                    initialOrder = initialOrder
                                ),
                                isLoading = false
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    fun onSearchQueryChanged(groupId: FilterId, query: String) {
        _searchQuery.value = SearchQuery(groupId, query)
    }

    private data class SearchQuery(
        val groupId: FilterId,
        val query: String
    )

    private fun updateFilterItems(groupId: FilterId, newItems: List<FilterItem>) {
        _uiState.update { state ->
            state.copy(
                filters = state.filters.map { filterGroup ->
                    if (filterGroup.groupId == groupId) {
                        when (val content = filterGroup.groupContent) {
                            is FilterGroupContentType.ListType.SingleSelectableListType -> {
                                val selectedItem = newItems.find { item ->
                                    val dynamicItem = item as? FilterItem.Dynamic ?: return@find false
                                    initialOptions.any { option ->
                                        isDynamicOptionMatch(groupId, dynamicItem.id, option)
                                    }
                                }
                                if (selectedItem != null) {
                                    content.selectionState.select(selectedItem)
                                }
                                filterGroup.copy(groupContent = content.copy(items = newItems))
                            }

                            is FilterGroupContentType.ListType.MultiSelectableListType -> {
                                val selectedItems = newItems.filter { item ->
                                    val dynamicItem = item as? FilterItem.Dynamic ?: return@filter false
                                    initialOptions.any { option ->
                                        isDynamicOptionMatch(groupId, dynamicItem.id, option)
                                    }
                                }
                                if (selectedItems.isNotEmpty()) {
                                    content.selectionState.select(selectedItems.toSet())
                                }
                                filterGroup.copy(groupContent = content.copy(items = newItems))
                            }

                            else -> filterGroup
                        }
                    } else filterGroup
                }
            )
        }
    }

    fun onFilterPickerOpened(groupId: FilterId) {
        val provider = if (isTv) tvShowFilterProvider else movieFilterProvider

        // Only fetch if it's a dynamic picker and currently empty
        val group = _uiState.value.filters.find { it.groupId == groupId } ?: return
        val items = when (val content = group.groupContent) {
            is FilterGroupContentType.ListType.SingleSelectableListType -> content.items
            is FilterGroupContentType.ListType.MultiSelectableListType -> content.items
            else -> null
        } ?: return

        if (items.isNotEmpty()) {
            if (cachedFilterItems[groupId] == null) {
                cachedFilterItems[groupId] = items
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            provider.fetchFilterOptions(groupId).onSuccess { items ->
                val dynamicItems = items.map {
                    FilterItem.Dynamic(
                        id = it.id,
                        text = UiText.DynamicText(it.displayText),
                        iconUrl = it.iconUrl
                    )
                }
                cachedFilterItems[groupId] = dynamicItems
                updateFilterItems(groupId = groupId, newItems = dynamicItems)
                _uiState.update { it.copy(isSearching = false) }
            }.onFailure {
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }
}
