package com.ssverma.showtime.domain.usecase.movie

import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.*
import com.ssverma.showtime.domain.defaults.movie.MovieDefaults
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.movie.MovieFailure
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.repository.MovieRepository
import com.ssverma.showtime.domain.usecase.NoParamUseCase
import com.ssverma.showtime.utils.DateUtils
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpcomingMoviesUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val movieRepository: MovieRepository
) : NoParamUseCase<List<Movie>, Failure<MovieFailure>>(coroutineDispatcher) {

    override suspend fun execute(): DomainResult<List<Movie>, Failure<MovieFailure>> {
        val movieConfig = MovieDiscoverConfig
            .builder(sortBy = SortBy.ReleaseDate(order = Order.Ascending))
            .with(
                MovieDefaults.DefaultMovieReleaseType,
                DiscoverOption.ReleaseDate.From(date = DateUtils.currentDate().plusDays(1)),
            )
            .build()

        return movieRepository.discoverMovies(movieConfig)
    }
}