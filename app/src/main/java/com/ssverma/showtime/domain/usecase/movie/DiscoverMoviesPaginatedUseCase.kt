package com.ssverma.showtime.domain.usecase.movie

import androidx.paging.PagingData
import com.ssverma.core.domain.usecase.FlowUseCase
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.MovieDiscoverConfig
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.repository.MovieRepository
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