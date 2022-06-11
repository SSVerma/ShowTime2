package com.ssverma.feature.tv.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.usecase.FlowUseCase
import com.ssverma.feature.tv.domain.model.TvShow
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PaginatedTvShowUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val discoverTvShowsUseCase: DiscoverTvShowsPaginatedUseCase,
    private val topRatedTvShowsUseCase: TopRatedTvShowsPaginatedUseCase,
    private val trendingTvShowsUseCase: TrendingTvShowsPaginatedUseCase
) : FlowUseCase<TvShowListingConfig, PagingData<TvShow>>(
    coroutineDispatcher
) {

    override fun execute(params: TvShowListingConfig): Flow<PagingData<TvShow>> {
        return when (params) {
            TvShowListingConfig.TopRated -> {
                topRatedTvShowsUseCase()
            }
            is TvShowListingConfig.TrendingToday -> {
                trendingTvShowsUseCase(params.timeWindow)
            }
            is TvShowListingConfig.Filterable -> {
                val allOptions = params.filterConfig?.tvOptions?.let { filterOptions ->
                    params.discoverConfig.tvOptions + filterOptions
                } ?: params.discoverConfig.tvOptions

                val discoverConfig = TvDiscoverConfig
                    .builder(sortBy = params.discoverConfig.sortBy)
                    .with(*allOptions.toTypedArray())
                    .build()

                discoverTvShowsUseCase(discoverConfig)
            }
        }
    }
}