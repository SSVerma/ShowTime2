package com.ssverma.showtime.data.repository

import com.ssverma.showtime.R
import com.ssverma.showtime.api.Resource
import com.ssverma.showtime.api.TmdbApiService
import com.ssverma.showtime.api.makeTmdbApiRequest
import com.ssverma.showtime.data.MovieCategory
import com.ssverma.showtime.data.domain.ApiTiedConstants
import com.ssverma.showtime.data.remote.response.PagedPayload
import com.ssverma.showtime.data.remote.response.RemoteMovie
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {
    val movieCategories = listOf(
        MovieCategory(
            id = 1,
            nameRes = R.string.popuplar,
        ),
        MovieCategory(
            id = 2,
            nameRes = R.string.now_playing,
        ),
        MovieCategory(
            id = 3,
            nameRes = R.string.upcoming,
        ),
        MovieCategory(
            id = 4,
            nameRes = R.string.top_rated,
        )
    )

    fun fetchMockMovies(): Flow<Resource<PagedPayload<RemoteMovie>>> {
        return makeTmdbApiRequest { tmdbApiService.getMockMovies() }
    }

    fun fetchLatestMovie(): Flow<Resource<RemoteMovie>> {
        return makeTmdbApiRequest { tmdbApiService.getLatestMovie() }
    }

    fun fetchMovie(movieId: Int): Flow<Resource<RemoteMovie>> {
        return makeTmdbApiRequest { tmdbApiService.getMovie(movieId = movieId) }
    }

    fun fetchPopularMovies(): Flow<Resource<PagedPayload<RemoteMovie>>> {
        return makeTmdbApiRequest { tmdbApiService.getPopularMovies() }
    }

    fun fetchDailyTrendingMovies(): Flow<Resource<PagedPayload<RemoteMovie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTrendingMovies(timeWindow = ApiTiedConstants.AvailableTimeWindows.DAY)
        }
    }

    fun discoverMovies(queryMap: Map<String, String> = emptyMap()): Flow<Resource<PagedPayload<RemoteMovie>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getDiscoveredMovies(
                queryMap = queryMap
            )
        }
    }
}