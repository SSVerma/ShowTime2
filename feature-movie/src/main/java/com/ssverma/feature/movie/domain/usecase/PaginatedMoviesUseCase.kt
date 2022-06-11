package com.ssverma.feature.movie.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.usecase.FlowUseCase
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.model.MovieListingConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PaginatedMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val discoverMoviesPaginatedUseCase: DiscoverMoviesPaginatedUseCase,
    private val topRatedPaginatedMoviesUseCase: TopRatedPaginatedMoviesUseCase,
    private val trendingPaginatedMoviesUseCase: TrendingPaginatedMoviesUseCase
) : FlowUseCase<MovieListingConfig, PagingData<Movie>>(coroutineDispatcher) {

    override fun execute(params: MovieListingConfig): Flow<PagingData<Movie>> {
        return when (params) {
            MovieListingConfig.TopRated -> {
                topRatedPaginatedMoviesUseCase()
            }
            is MovieListingConfig.TrendingToday -> {
                trendingPaginatedMoviesUseCase(params.timeWindow)
            }
            is MovieListingConfig.Filterable -> {
                val allOptions = params.filterConfig?.movieOptions?.let { filterOptions ->
                    params.discoverConfig.movieOptions + filterOptions
                } ?: params.discoverConfig.movieOptions

                val discoverConfig = MovieDiscoverConfig
                    .builder(sortBy = params.discoverConfig.sortBy)
                    .with(*allOptions.toTypedArray())
                    .build()

                discoverMoviesPaginatedUseCase(discoverConfig)
            }
        }
    }
}