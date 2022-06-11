package com.ssverma.feature.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.api.service.tmdb.TmdbDefaults
import com.ssverma.api.service.tmdb.paging.MoviePagingSource
import com.ssverma.api.service.tmdb.paging.ReviewsPagingSource
import com.ssverma.api.service.tmdb.response.RemoteGenre
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteReview
import com.ssverma.feature.movie.data.remote.MovieRemoteDataSource
import com.ssverma.feature.movie.domain.failure.MovieFailure
import com.ssverma.feature.movie.domain.model.Movie
import com.ssverma.feature.movie.domain.model.MovieDetailsConfig
import com.ssverma.feature.movie.domain.repository.MovieRepository
import com.ssverma.shared.data.mapper.*
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Review
import kotlinx.coroutines.flow.Flow
import java.net.HttpURLConnection
import javax.inject.Inject

class DefaultMovieRepository @Inject constructor(
    private val movieRemoteDataSource: MovieRemoteDataSource,
    private val moviesMapper: @JvmSuppressWildcards ListMapper<RemoteMovie, Movie>,
    private val generesMapper: @JvmSuppressWildcards ListMapper<RemoteGenre, Genre>,
    private val reviewsMapper: @JvmSuppressWildcards ListMapper<RemoteReview, Review>,
    private val movieMapper: @JvmSuppressWildcards Mapper<RemoteMovie, Movie>
) : MovieRepository {

    override fun discoverMoviesGradually(discoverConfig: MovieDiscoverConfig): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
            pagingSourceFactory = {
                MoviePagingSource(
                    movieApiCall = { pageNumber ->
                        movieRemoteDataSource.discoverMovies(
                            queryMap = discoverConfig.asQueryMap(),
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { moviesMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchTrendingMoviesGradually(timeWindow: TimeWindow): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
            pagingSourceFactory = {
                MoviePagingSource(
                    movieApiCall = { pageNumber ->
                        movieRemoteDataSource.fetchTrendingMovies(
                            timeWindow = timeWindow.asTmdbQueryValue(),
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { moviesMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchTopRatedMoviesGradually(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
            pagingSourceFactory = {
                MoviePagingSource(
                    movieApiCall = { pageNumber ->
                        movieRemoteDataSource.fetchTopRatedMovies(
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { moviesMapper.map(it) }
                )
            }
        ).flow
    }

    override suspend fun fetchTopRatedMovies(): Result<List<Movie>, Failure<MovieFailure>> {
        val apiResponse =
            movieRemoteDataSource.fetchTopRatedMovies(page = TmdbDefaults.ApiDefaults.FirstPageNumber)

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                moviesMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun fetchTrendingMovies(timeWindow: TimeWindow): Result<List<Movie>, Failure<MovieFailure>> {
        val apiResponse = movieRemoteDataSource.fetchTrendingMovies(
            timeWindow = TmdbApiTiedConstants.AvailableTimeWindows.DAY,
            page = TmdbDefaults.ApiDefaults.FirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                moviesMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun discoverMovies(discoverConfig: MovieDiscoverConfig): Result<List<Movie>, Failure<MovieFailure>> {
        val apiResponse = movieRemoteDataSource.discoverMovies(
            queryMap = discoverConfig.asQueryMap(),
            page = TmdbDefaults.ApiDefaults.FirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                moviesMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun fetchMovieGenre(): Result<List<Genre>, Failure.CoreFailure> {
        val apiResponse = movieRemoteDataSource.fetchMovieGenre()

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                generesMapper.map(it.body.genres.orEmpty())
            }
        )
    }

    override fun fetchMovieReviewsGradually(movieId: Int): Flow<PagingData<Review>> {
        return Pager(
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
            pagingSourceFactory = {
                ReviewsPagingSource(
                    reviewsApiCall = { pageNumber ->
                        movieRemoteDataSource.fetchMovieReviewsGradually(
                            movieId = movieId,
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { reviewsMapper.map(it) }
                )
            }
        ).flow
    }

    override suspend fun fetchMovieDetails(
        movieDetailsConfig: MovieDetailsConfig
    ): Result<Movie, Failure<MovieFailure>> {
        val apiResponse = movieRemoteDataSource.fetchMovieDetails(
            movieId = movieDetailsConfig.movieId,
            queryMap = movieDetailsConfig.appendable.asQueryMap()
        )

        return apiResponse.asDomainResult(
            mapFeatureFailure = {
                if (it.payload.httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    Failure.FeatureFailure(MovieFailure.NotFound)
                } else {
                    Failure.CoreFailure.UnexpectedFailure
                }
            },
            mapRemoteToDomain = {
                movieMapper.map(it.body)
            }
        )
    }
}