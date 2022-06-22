package com.ssverma.feature.movie.domain.repository

import androidx.paging.PagingData
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.MovieDetailsConfig
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Review
import com.ssverma.shared.domain.model.movie.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    /**
     * Allows to find movies based on various query params in paginated form.
     */
    fun discoverMoviesGradually(
        discoverConfig: MovieDiscoverConfig
    ): Flow<PagingData<Movie>>

    /**
     * Fetch trending movies based on the given [TimeWindow] and in paginated form.
     */
    fun fetchTrendingMoviesGradually(
        timeWindow: TimeWindow
    ): Flow<PagingData<Movie>>

    /**
     * Fetch movies in order of given rating and in paginated form.
     */
    fun fetchTopRatedMoviesGradually(): Flow<PagingData<Movie>>

    /**
     * Fetch top rated movies. It provides a limited collection of the top rated movies
     * wheres [fetchTopRatedMoviesGradually] can be used to fetch all available top rated movies
     * in paginated form.
     */
    suspend fun fetchTopRatedMovies(): Result<List<Movie>, Failure<MovieFailure>>

    /**
     * Fetch first collection of trending movies based on the given [TimeWindow].
     */
    suspend fun fetchTrendingMovies(
        timeWindow: TimeWindow
    ): Result<List<Movie>, Failure<MovieFailure>>

    /**
     * Allows to find movies based on various query params.
     */
    suspend fun discoverMovies(
        discoverConfig: MovieDiscoverConfig
    ): Result<List<Movie>, Failure<MovieFailure>>

    /**
     * Fetch all movie genres.
     */
    suspend fun fetchMovieGenre(): Result<List<Genre>, Failure.CoreFailure>

    /**
     * Fetch all the movie reviews by given @param[movieId]
     */
    fun fetchMovieReviewsGradually(
        movieId: Int
    ): Flow<PagingData<Review>>

    /**
     * Fetch details of a particular movie.
     */
    suspend fun fetchMovieDetails(
        movieDetailsConfig: MovieDetailsConfig
    ): Result<Movie, Failure<MovieFailure>>
}