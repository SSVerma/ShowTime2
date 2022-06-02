package com.ssverma.feature.movie.domain.usecase

import androidx.paging.PagingData
import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.core.domain.MovieDiscoverConfig
import com.ssverma.core.domain.usecase.FlowUseCase
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.repository.MovieRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class DiscoverMoviesPaginatedUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : FlowUseCase<MovieDiscoverConfig, PagingData<Movie>>(coroutineDispatcher) {

    override fun execute(params: MovieDiscoverConfig): Flow<PagingData<Movie>> {
        return movieRepository.discoverMoviesGradually(discoverConfig = params)
    }
}