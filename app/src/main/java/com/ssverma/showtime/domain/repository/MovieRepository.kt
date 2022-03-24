package com.ssverma.showtime.domain.repository

import androidx.paging.PagingData
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.MovieDiscoverConfig
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.movie.MovieFailure
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.Review
import com.ssverma.showtime.domain.model.movie.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    /**
     * Allows to find movies based on various query params in paginated form.
     */
    fun discoverMoviesGradually(
        queryMap: Map<String, String> = emptyMap()
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
    suspend fun fetchTopRatedMovies(): DomainResult<List<Movie>, Failure<MovieFailure>>

    /**
     * Fetch first collection of trending movies based on the given [TimeWindow].
     */
    suspend fun fetchTrendingMovies(
        timeWindow: TimeWindow
    ): DomainResult<List<Movie>, Failure<MovieFailure>>

    /**
     * Allows to find movies based on various query params.
     */
    suspend fun discoverMovies(
        discoverConfig: MovieDiscoverConfig
    ): DomainResult<List<Movie>, Failure<MovieFailure>>

    /**
     * Fetch all movie genres.
     */
    suspend fun fetchMovieGenre(): DomainResult<List<Genre>, Failure.CoreFailure>

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
        movieId: Int,
        queryMap: Map<String, String>
    ): DomainResult<Movie, Failure<MovieFailure>>
}