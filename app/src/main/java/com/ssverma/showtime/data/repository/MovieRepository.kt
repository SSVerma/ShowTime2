package com.ssverma.showtime.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.showtime.api.TMDB_API_PAGE_SIZE
import com.ssverma.showtime.api.TmdbApiService
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.api.makeTmdbApiRequest
import com.ssverma.showtime.data.FilterGroup
import com.ssverma.showtime.data.movieFilterGroups
import com.ssverma.showtime.data.remote.MoviePagingSource
import com.ssverma.showtime.data.remote.ReviewsPagingSource
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.*
import com.ssverma.showtime.extension.asDomainFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@Deprecated("use-> com.ssverma.showtime.domain.repository.MovieRepository")
class MovieRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {
    fun fetchMockMoviesGradually(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = { MoviePagingSource { tmdbApiService.getMockMovies(page = it) } }
        ).flow
    }

    fun fetchMockMovieDetails(): Flow<Result<Movie>> {
        return makeTmdbApiRequest {
            tmdbApiService.getMockMovieDetails()
        }.asDomainFlow { it.payload.asMovie() }
    }

    fun discoverMoviesGradually(queryMap: Map<String, String> = emptyMap()): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                MoviePagingSource {
                    tmdbApiService.getDiscoveredMovies(
                        queryMap = queryMap,
                        page = it
                    )
                }
            }
        ).flow
    }

    fun fetchTrendingMoviesGradually(
        timeWindow: String = TmdbApiTiedConstants.AvailableTimeWindows.DAY
    ): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                MoviePagingSource {
                    tmdbApiService.getTrendingMovies(
                        timeWindow = timeWindow,
                        page = it
                    )
                }
            }
        ).flow
    }

    fun fetchTopRatedMoviesGradually(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                MoviePagingSource {
                    tmdbApiService.getTopRatedMovies(page = it)
                }
            }
        ).flow
    }

    fun fetchLatestMovie(): Flow<Result<Movie>> {
        return makeTmdbApiRequest {
            tmdbApiService.getLatestMovie()
        }.asDomainFlow {
            it.payload.asMovie()
        }
    }

    fun fetchMovieDetails(movieId: Int, queryMap: Map<String, String>): Flow<Result<Movie>> {
        return makeTmdbApiRequest {
            tmdbApiService.getMovieDetails(movieId = movieId, queryMap = queryMap)
        }.asDomainFlow {
            it.payload.asMovie()
        }
    }

    fun fetchPopularMovies(): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getPopularMovies()
        }.asDomainFlow {
            it.payload.results?.asMovies() ?: emptyList()
        }
    }

    fun fetchTopRatedMovies(): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTopRatedMovies()
        }.asDomainFlow {
            it.payload.results?.asMovies() ?: emptyList()
        }
    }

    fun fetchDailyTrendingMovies(): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTrendingMovies(timeWindow = TmdbApiTiedConstants.AvailableTimeWindows.DAY)
        }.asDomainFlow {
            it.payload.results?.asMovies() ?: emptyList()
        }
    }

    fun discoverMovies(queryMap: Map<String, String> = emptyMap()): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getDiscoveredMovies(
                queryMap = queryMap
            )
        }.asDomainFlow {
            it.payload.results?.asMovies() ?: emptyList()
        }
    }

    fun fetchMovieGenre(): Flow<Result<List<Genre>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getMovieGenres()
        }.asDomainFlow {
            it.payload.genres?.asGenres() ?: emptyList()
        }
    }

    fun loadMovieFilters(): Flow<List<FilterGroup>> {
        //Todo: Fetch dynamic filters like genre, language etc and combine
        return flow {
            emit(movieFilterGroups())
        }
    }

    fun fetchMovieReviewsGradually(movieId: Int): Flow<PagingData<Review>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                ReviewsPagingSource { page ->
                    tmdbApiService.getMovieReviews(movieId = movieId, page = page)
                }
            }
        ).flow
    }
}