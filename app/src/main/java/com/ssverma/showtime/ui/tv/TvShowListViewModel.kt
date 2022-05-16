package com.ssverma.showtime.ui.tv

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.R
import com.ssverma.core.domain.Result
import com.ssverma.showtime.domain.TvDiscoverConfig
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.domain.model.tv.TvShowListingConfig
import com.ssverma.showtime.domain.usecase.tv.PaginatedTvShowUseCase
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.ui.filter.FilterUiState
import com.ssverma.showtime.ui.filter.TvShowFilterUseCase
import com.ssverma.showtime.ui.filter.asUiFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowListListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paginatedTvShowUseCase: PaginatedTvShowUseCase,
    private val tvShowFilterUseCase: TvShowFilterUseCase
) : ViewModel() {

    private val appliedFilters = MutableStateFlow(TvDiscoverConfig.builder().build())

    // Put listing args as parcelable on Custom Nav Type
    private val tvShowListingArgs = savedStateHandle.buildTvShowListingArgs()

    private val tvShowListingConfig = tvShowListingArgs.asTvShowListingConfigs()

    val titleRes = if (tvShowListingArgs.titleRes == 0) {
        R.string.tv_show
    } else {
        tvShowListingArgs.titleRes
    }

    val title = tvShowListingArgs.title

    val listingType = tvShowListingArgs.listingType

    val filterApplicable = tvShowListingConfig is TvShowListingConfig.Filterable

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedTvShows: Flow<PagingData<TvShow>> = appliedFilters.flatMapLatest { filterConfig ->
        if (tvShowListingConfig is TvShowListingConfig.Filterable) {
            tvShowListingConfig.filterConfig = filterConfig
        }

        paginatedTvShowUseCase(tvShowListingConfig)

    }.cachedIn(viewModelScope)

    var filterUiState by mutableStateOf(FilterUiState(filters = emptyList()))
        private set

    init {
        if (filterApplicable) {
            loadFilters()
        }
    }

    private fun loadFilters() {
        val discoverOptionResult = tvShowFilterUseCase()
        viewModelScope.launch {
            discoverOptionResult.collect { result ->
                filterUiState = when (result) {
                    is Result.Error -> {
                        FilterUiState(filters = emptyList())
                    }
                    is Result.Success -> {
                        FilterUiState(
                            filters = result.data.asUiFilters()
                        )
                    }
                }
            }
        }
    }

    fun onFiltersApplied(discoverConfig: TvDiscoverConfig) {
        this.appliedFilters.update { discoverConfig }
    }
}

private fun SavedStateHandle.buildTvShowListingArgs(): TvShowListingArgs {
    val genreId = get<Int>(AppDestination.TvShowList.ArgGenreId) ?: 0
    val keywordId = get<Int>(AppDestination.TvShowList.ArgKeywordId) ?: 0

    val listingType = get<Int>(AppDestination.TvShowList.ArgListingType) ?: 0
    val titleRes = get<Int>(AppDestination.TvShowList.ArgTitleRes) ?: 0
    val title = get<String>(AppDestination.TvShowList.ArgTitle)

    return TvShowListingArgs(
        listingType = listingType,
        titleRes = titleRes,
        title = title,
        genreId = genreId,
        keywordId = keywordId
    )
}