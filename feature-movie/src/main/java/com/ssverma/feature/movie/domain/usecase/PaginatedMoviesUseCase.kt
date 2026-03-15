package com.ssverma.feature.movie.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.usecase.FlowUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class PaginatedMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val discoverMoviesPaginatedUseCase: DiscoverMoviesPaginatedUseCase,
    private val topRatedPaginatedMoviesUseCase: TopRatedPaginatedMoviesUseCase,
    private val trendingPaginatedMoviesUseCase: TrendingPaginatedMoviesUseCase,
    private val appConfigRepository: AppConfigRepository
) : FlowUseCase<MovieListingConfig, PagingData<Movie>>(coroutineDispatcher) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(params: MovieListingConfig): Flow<PagingData<Movie>> {
        return appConfigRepository.preferredOriginalLanguage.flatMapLatest { preferredLang ->
            executeInternal(params, preferredLang)
        }
    }

    private fun executeInternal(
        params: MovieListingConfig,
        preferredLang: String
    ): Flow<PagingData<Movie>> {
        return when (params) {
            is MovieListingConfig.TrendingToday -> {
                trendingPaginatedMoviesUseCase(params.timeWindow)
            }
            is MovieListingConfig.Filterable -> {
                var allOptions = params.filterConfig?.movieOptions?.let { filterOptions ->
                    params.discoverConfig.movieOptions + filterOptions
                } ?: params.discoverConfig.movieOptions

                if (preferredLang.isNotEmpty() && allOptions.none { it is DiscoverOption.OriginalLanguage }) {
                    allOptions = allOptions + DiscoverOption.OriginalLanguage(preferredLang)
                }

                val discoverConfig = MovieDiscoverConfig
                    .builder(sortBy = params.filterConfig?.sortBy ?: params.discoverConfig.sortBy)
                    .with(*allOptions.toTypedArray())
                    .build()

                discoverMoviesPaginatedUseCase(discoverConfig)
            }
        }
    }
}