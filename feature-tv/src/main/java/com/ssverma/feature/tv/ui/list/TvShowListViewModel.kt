package com.ssverma.feature.tv.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ssverma.feature.filter.ui.FilterUiState
import com.ssverma.feature.filter.ui.asUiFilters
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.domain.usecase.PaginatedTvShowUseCase
import com.ssverma.feature.tv.domain.usecase.TvShowFilterUseCase
import com.ssverma.feature.tv.navigation.TvShowListDestination
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingAvailableTypes
import com.ssverma.feature.tv.navigation.convertor.asTvShowListingConfigs
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.domain.model.tv.asTvShowPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TvShowPaginatedListUiState(
    val isGridView: Boolean = true,
    val filterUiState: FilterUiState = FilterUiState(filters = emptyList()),
    val titleRes: Int = R.string.tv_show,
    val title: String? = null,
    val listingType: Int = 0,
    val isFilterApplicable: Boolean = false
)

@HiltViewModel
class TvShowListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paginatedTvShowUseCase: PaginatedTvShowUseCase,
    private val tvShowFilterUseCase: TvShowFilterUseCase
) : ViewModel() {

    private val tvShowListingArgs = savedStateHandle.buildTvShowListingArgs()
    private val tvShowListingConfig = tvShowListingArgs.asTvShowListingConfigs()

    private val _uiState = MutableStateFlow(
        TvShowPaginatedListUiState(
            titleRes = if (tvShowListingArgs.titleRes == 0) R.string.tv_show else tvShowListingArgs.titleRes,
            title = tvShowListingArgs.title,
            listingType = tvShowListingArgs.listingType,
            isFilterApplicable = tvShowListingConfig is TvShowListingConfig.Filterable
        )
    )
    val uiState: StateFlow<TvShowPaginatedListUiState> = _uiState.asStateFlow()

    private val appliedFilters = MutableStateFlow(TvDiscoverConfig.builder().build())

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedTvShows: Flow<PagingData<TvShowPreview>> = appliedFilters
        .flatMapLatest { filterConfig ->
            if (tvShowListingConfig is TvShowListingConfig.Filterable) {
                tvShowListingConfig.filterConfig = filterConfig
            }
            paginatedTvShowUseCase(tvShowListingConfig)
        }
        .map { pagingData ->
            pagingData.map { tvShow -> tvShow.asTvShowPreview() }
        }
        .cachedIn(viewModelScope)

    init {
        if (_uiState.value.isFilterApplicable) {
            loadFilters()
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            tvShowFilterUseCase().collect { result ->
                val filters = when (result) {
                    is Result.Success -> result.data.asUiFilters()
                    is Result.Error -> emptyList()
                }
                _uiState.update { it.copy(filterUiState = FilterUiState(filters = filters)) }
            }
        }
    }

    fun onFiltersApplied(discoverConfig: TvDiscoverConfig) {
        appliedFilters.update { discoverConfig }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }
}

private fun SavedStateHandle.buildTvShowListingArgs(): TvShowListingArgs {
    return TvShowListingArgs(
        listingType = get<Int>(TvShowListDestination.ArgListingType) ?: TvShowListingAvailableTypes.TrendingToday,
        titleRes = get<Int>(TvShowListDestination.ArgTitleRes) ?: 0,
        title = get<String>(TvShowListDestination.ArgTitle),
        genreId = get<Int>(TvShowListDestination.ArgGenreId) ?: 0,
        keywordId = get<Int>(TvShowListDestination.ArgKeywordId) ?: 0
    )
}
