package com.ssverma.showtime.data.repository

import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.api.TmdbApiService
import com.ssverma.showtime.api.makeTmdbApiRequest
import com.ssverma.showtime.domain.model.*
import com.ssverma.showtime.extension.asDomainFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {
    fun fetchMockMovies(): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getMockMovies()
        }.asDomainFlow {
            it.data.results?.asMovies() ?: emptyList()
        }
    }

    fun fetchLatestMovie(): Flow<Result<Movie>> {
        return makeTmdbApiRequest {
            tmdbApiService.getLatestMovie()
        }.asDomainFlow {
            it.data.asMovie()
        }
    }

    fun fetchMovie(movieId: Int): Flow<Result<Movie>> {
        return makeTmdbApiRequest {
            tmdbApiService.getMovie(movieId = movieId)
        }.asDomainFlow {
            it.data.asMovie()
        }
    }

    fun fetchPopularMovies(): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getPopularMovies()
        }.asDomainFlow {
            it.data.results?.asMovies() ?: emptyList()
        }
    }

    fun fetchTopRatedMovies(): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTopRatedMovies()
        }.asDomainFlow {
            it.data.results?.asMovies() ?: emptyList()
        }
    }

    fun fetchDailyTrendingMovies(): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTrendingMovies(timeWindow = ApiTiedConstants.AvailableTimeWindows.DAY)
        }.asDomainFlow {
            it.data.results?.asMovies() ?: emptyList()
        }
    }

    fun discoverMovies(queryMap: Map<String, String> = emptyMap()): Flow<Result<List<Movie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getDiscoveredMovies(
                queryMap = queryMap
            )
        }.asDomainFlow {
            it.data.results?.asMovies() ?: emptyList()
        }
    }

    fun fetchMovieGenre(): Flow<Result<List<Genre>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getMovieGenres()
        }.asDomainFlow {
            it.data.genres?.asGenres() ?: emptyList()
        }
    }
}