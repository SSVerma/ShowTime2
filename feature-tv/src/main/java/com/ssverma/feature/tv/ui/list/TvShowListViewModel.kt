package com.ssverma.feature.tv.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.domain.usecase.PaginatedTvShowUseCase
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.convertor.asTvShowListingConfigs
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.domain.model.tv.asTvShowPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class TvShowPaginatedListUiState(
    val isGridView: Boolean = true,
    val titleRes: Int = R.string.tv_show,
    val title: String? = null,
    val config: TvShowListingConfig,
    val isFilterApplicable: Boolean = false,
    val isFilterApplied: Boolean = false,
    val filterConfig: TvDiscoverConfig? = null
)

@HiltViewModel
class TvShowListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paginatedTvShowUseCase: PaginatedTvShowUseCase,
) : ViewModel() {

    private val routeWrapper = savedStateHandle.toRoute<TvShowListingRoute>(TvShowListingArgs.TypeMap)
    private val tvShowListingArgs = routeWrapper.args
    internal val tvShowListingConfig = tvShowListingArgs.asTvShowListingConfigs()

    private val _uiState = MutableStateFlow(
        TvShowPaginatedListUiState(
            titleRes = tvShowListingArgs.titleRes ?: R.string.tv_show,
            title = tvShowListingArgs.title,
            config = tvShowListingConfig,
            isFilterApplicable = tvShowListingConfig is TvShowListingConfig.Filterable,
            filterConfig = (tvShowListingConfig as? TvShowListingConfig.Filterable)?.discoverConfig
        )
    )

    val uiState: StateFlow<TvShowPaginatedListUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedTvShows: Flow<PagingData<TvShowPreview>> = _uiState
        .map { state ->
            if (tvShowListingConfig is TvShowListingConfig.Filterable && state.filterConfig != null) {
                tvShowListingConfig.withFilter(state.filterConfig)
            } else {
                tvShowListingConfig
            }
        }
        .distinctUntilChanged()
        .flatMapLatest { config ->
            paginatedTvShowUseCase(config)
        }.map { pagingData ->
            pagingData.map { tvShow -> tvShow.asTvShowPreview() }
        }.cachedIn(viewModelScope)

    fun onFiltersApplied(filterConfig: TvDiscoverConfig) {
        val isApplied = (tvShowListingConfig as? TvShowListingConfig.Filterable)?.let {
            it.discoverConfig != filterConfig && !filterConfig.isBare()
        } ?: false

        _uiState.update {
            it.copy(
                isFilterApplied = isApplied,
                filterConfig = filterConfig
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }
}
