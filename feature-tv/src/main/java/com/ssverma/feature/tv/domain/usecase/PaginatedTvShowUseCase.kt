package com.ssverma.feature.tv.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.usecase.FlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class PaginatedTvShowUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val discoverTvShowsUseCase: DiscoverTvShowsPaginatedUseCase,
    private val topRatedTvShowsUseCase: TopRatedTvShowsPaginatedUseCase,
    private val trendingTvShowsUseCase: TrendingTvShowsPaginatedUseCase,
    private val appConfigRepository: AppConfigRepository
) : FlowUseCase<TvShowListingConfig, PagingData<TvShow>>(
    coroutineDispatcher
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(params: TvShowListingConfig): Flow<PagingData<TvShow>> {
        return appConfigRepository.preferredOriginalLanguage.flatMapLatest { preferredLang ->
            executeInternal(params, preferredLang)
        }
    }

    private fun executeInternal(
        params: TvShowListingConfig,
        preferredLang: String
    ): Flow<PagingData<TvShow>> {
        return when (params) {
            is TvShowListingConfig.TrendingToday -> {
                trendingTvShowsUseCase(params.timeWindow)
            }
            is TvShowListingConfig.Filterable -> {
                var allOptions = params.filterConfig?.tvOptions?.let { filterOptions ->
                    params.discoverConfig.tvOptions + filterOptions
                } ?: params.discoverConfig.tvOptions

                if (preferredLang.isNotEmpty() && allOptions.none { it is DiscoverOption.OriginalLanguage }) {
                    allOptions = allOptions + DiscoverOption.OriginalLanguage(preferredLang)
                }

                val discoverConfig = TvDiscoverConfig
                    .builder(sortBy = params.filterConfig?.sortBy ?: params.discoverConfig.sortBy)
                    .with(*allOptions.toTypedArray())
                    .build()

                discoverTvShowsUseCase(discoverConfig)
            }
        }
    }
}