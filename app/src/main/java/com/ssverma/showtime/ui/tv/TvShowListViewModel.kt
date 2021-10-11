package com.ssverma.showtime.ui.tv

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ssverma.showtime.R
import com.ssverma.showtime.api.DiscoverQueryMap
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.data.repository.TvRepository
import com.ssverma.showtime.domain.model.TvShow
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.utils.DateUtils
import com.ssverma.showtime.utils.formatAsIso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class TvShowListListViewModel @Inject constructor(
    private val tvRepository: TvRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val appliedFilters = MutableStateFlow(mapOf<String, String>())

    private val genreId = savedStateHandle.get<Int>(AppDestination.TvShowList.ArgGenreId)
    private val keywordId = savedStateHandle.get<Int>(AppDestination.TvShowList.ArgKeywordId)

    val listingType =
        savedStateHandle.get<TvShowListingType>(AppDestination.TvShowList.ArgListingType)
            ?: throw IllegalStateException("Movie listing type not provided")

    val titleRes = savedStateHandle.get<Int>(AppDestination.TvShowList.ArgTitleRes)
        ?: R.string.tv_show

    val title = savedStateHandle.get<String>(AppDestination.TvShowList.ArgTitle)

    val filterApplicable = when (listingType) {
        TvShowListingType.TrendingToday,
        TvShowListingType.TopRated -> false
        else -> true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagedTvShows: Flow<PagingData<TvShow>> = appliedFilters.flatMapLatest {
        when (listingType) {
            TvShowListingType.TrendingToday -> {
                tvRepository.fetchDailyTrendingTvShowsGradually()
            }
            TvShowListingType.TopRated -> {
                tvRepository.fetchTopRatedTvShowsGradually()
            }
            else -> {
                tvRepository.discoverTvShowsGradually(appliedFilters.value)
            }
        }
    }.cachedIn(viewModelScope)

    val filters = tvRepository.loadTvFilters()

    init {
        fetchTvShows(listingType)
    }

    private fun fetchTvShows(type: TvShowListingType) {
        val filterMap = when (type) {
            TvShowListingType.Popular -> {
                DiscoverQueryMap.ofTv(
                    sortBy = TmdbApiTiedConstants.AvailableSortingOptions.PopularityDesc,
                )
            }
            TvShowListingType.NowAiring -> {
                DiscoverQueryMap.ofTv(
                    firstAirDateLte = DateUtils.currentDate().formatAsIso()
                )
            }
            TvShowListingType.Upcoming -> {
                DiscoverQueryMap.ofTv(
                    firstAirDateGte = DateUtils.currentDate().plusDays(1).formatAsIso(),
                    sortBy = TmdbApiTiedConstants.AvailableSortingOptions.FirstAirDateAsc,
                )
            }
            TvShowListingType.AiringToday -> {
                DiscoverQueryMap.ofTv(
                    airDateLte = DateUtils.currentDate().formatAsIso(),
                    airDateGte = DateUtils.currentDate().formatAsIso(),
                )
            }

            TvShowListingType.Genre -> {
                val genreId = genreId
                    ?: throw IllegalArgumentException("Provide genre id when listing type is $type")

                DiscoverQueryMap.ofTv(
                    genres = QueryMultiValue.orBuilder().or(genreId).build()
                )
            }

            TvShowListingType.Keyword -> {
                val keywordId = keywordId
                    ?: throw IllegalArgumentException("Provide keyword id when listing type is $type")

                DiscoverQueryMap.ofTv(
                    keywords = QueryMultiValue.orBuilder().or(keywordId).build()
                )
            }

            else -> DiscoverQueryMap.ofTv()
        }

        appliedFilters.value = filterMap
    }

    fun onFiltersApplied(appliedFilters: Map<String, String>) {
        this.appliedFilters.value = appliedFilters
    }

}