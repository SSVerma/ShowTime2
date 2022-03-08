package com.ssverma.showtime.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.api.service.tmdb.TMDB_API_PAGE_SIZE
import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.api.service.tmdb.paging.MoviePagingSource
import com.ssverma.api.service.tmdb.paging.ReviewsPagingSource
import com.ssverma.api.service.tmdb.response.RemoteGenre
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteReview
import com.ssverma.api.service.tmdb.utils.TmdbFirstPageNumber
import com.ssverma.showtime.data.asDomainResult
import com.ssverma.showtime.data.mapper.ListMapper
import com.ssverma.showtime.data.mapper.Mapper
import com.ssverma.showtime.data.mapper.asTmdbQueryValue
import com.ssverma.showtime.data.remote.MovieRemoteDataSource
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.core.Failure
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.domain.model.Review
import com.ssverma.showtime.domain.movie.MovieFailure
import com.ssverma.showtime.domain.repository.MovieRepository
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

    override fun discoverMoviesGradually(queryMap: Map<String, String>): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                MoviePagingSource(
                    movieApiCall = { pageNumber ->
                        movieRemoteDataSource.discoverMovies(queryMap = queryMap, page = pageNumber)
                    },
                    mapRemoteToDomain = { moviesMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchTrendingMoviesGradually(timeWindow: TimeWindow): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
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
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
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

    override suspend fun fetchTopRatedMovies(): DomainResult<List<Movie>, Failure<MovieFailure>> {
        val apiResponse = movieRemoteDataSource.fetchTopRatedMovies(page = TmdbFirstPageNumber)

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                moviesMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun fetchTrendingMovies(timeWindow: TimeWindow): DomainResult<List<Movie>, Failure<MovieFailure>> {
        val apiResponse = movieRemoteDataSource.fetchTrendingMovies(
            timeWindow = TmdbApiTiedConstants.AvailableTimeWindows.DAY,
            page = TmdbFirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                moviesMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun discoverMovies(queryMap: Map<String, String>): DomainResult<List<Movie>, Failure<MovieFailure>> {
        val apiResponse = movieRemoteDataSource.discoverMovies(
            queryMap = queryMap,
            page = TmdbFirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                moviesMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun fetchMovieGenre(): DomainResult<List<Genre>, Failure<MovieFailure>> {
        val apiResponse = movieRemoteDataSource.fetchMovieGenre()

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                generesMapper.map(it.body.genres.orEmpty())
            }
        )
    }

    override fun fetchMovieReviewsGradually(movieId: Int): Flow<PagingData<Review>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
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
        movieId: Int,
        queryMap: Map<String, String>
    ): DomainResult<Movie, Failure<MovieFailure>> {
        val apiResponse = movieRemoteDataSource.fetchMovieDetails(
            movieId = movieId,
            queryMap = queryMap
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