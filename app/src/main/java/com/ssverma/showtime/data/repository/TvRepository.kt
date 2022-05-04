package com.ssverma.showtime.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.showtime.api.TMDB_API_PAGE_SIZE
import com.ssverma.showtime.api.TmdbApiService
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.api.makeTmdbApiRequest
import com.ssverma.showtime.data.remote.ReviewsPagingSource
import com.ssverma.showtime.data.remote.TvShowsPagingSource
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.*
import com.ssverma.showtime.extension.asDomainFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TvRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService
) {

    fun fetchPopularTvShowsGradually(): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource {
                    tmdbApiService.getPopularTvShows(page = it)
                }
            }
        ).flow
    }

    fun fetchTopRatedTvShowsGradually(): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource {
                    tmdbApiService.getTopRatedTvShows(page = it)
                }
            }
        ).flow
    }

    fun fetchCurrentlyAiringTvShowsGradually(): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource {
                    tmdbApiService.getOnTheAirTvShows(page = it)
                }
            }
        ).flow
    }

    fun fetchTodayAiringTvShowsGradually(): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource {
                    tmdbApiService.getTodayAiringTvShows(page = it)
                }
            }
        ).flow
    }

    fun fetchDailyTrendingTvShowsGradually(): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource { page ->
                    tmdbApiService.getTrendingTvShows(
                        timeWindow = TmdbApiTiedConstants.AvailableTimeWindows.DAY,
                        page = page
                    )
                }
            }
        ).flow
    }

    fun fetchTvGenre(): Flow<Result<List<Genre>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTvGenres()
        }.asDomainFlow {
            it.payload.genres?.asGenres() ?: emptyList()
        }
    }

    fun discoverTvShowsGradually(queryMap: Map<String, String> = emptyMap()): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource {
                    tmdbApiService.getDiscoveredTvShows(
                        queryMap = queryMap,
                        page = it
                    )
                }
            }
        ).flow
    }

    fun discoverTvShows(queryMap: Map<String, String> = emptyMap()): Flow<Result<List<TvShow>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getDiscoveredTvShows(
                queryMap = queryMap
            )
        }.asDomainFlow {
            it.payload.results?.asTvShows() ?: emptyList()
        }
    }

    fun fetchTopRatedTvShows(): Flow<Result<List<TvShow>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTopRatedTvShows()
        }.asDomainFlow {
            it.payload.results?.asTvShows() ?: emptyList()
        }
    }

    fun fetchDailyTrendingTvShows(): Flow<Result<List<TvShow>>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTrendingTvShows(timeWindow = TmdbApiTiedConstants.AvailableTimeWindows.DAY)
        }.asDomainFlow {
            it.payload.results?.asTvShows() ?: emptyList()
        }
    }

    fun fetchTvShowDetails(tvShowId: Int, queryMap: Map<String, String>): Flow<Result<TvShow>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTvShowDetails(tvShowId = tvShowId, queryMap = queryMap)
        }.asDomainFlow { it.payload.asTvShow() }
    }

    fun fetchTvShowReviewsGradually(tvShowId: Int): Flow<PagingData<Review>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                ReviewsPagingSource { page ->
                    tmdbApiService.getTvShowReviews(tvShowId = tvShowId, page = page)
                }
            }
        ).flow
    }

    fun fetchTvSeasonDetails(
        tvShowId: Int,
        seasonNumber: Int,
        queryMap: Map<String, String>,
    ): Flow<Result<TvSeason>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTvSeason(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                queryMap = queryMap
            )
        }.asDomainFlow { it.payload.asTvSeason() }
    }

    fun fetchTvEpisodeDetails(
        tvShowId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        queryMap: Map<String, String>,
    ): Flow<Result<TvEpisode>> {
        return makeTmdbApiRequest {
            tmdbApiService.getTvEpisode(
                tvShowId = tvShowId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                queryMap = queryMap
            )
        }.asDomainFlow { it.payload.asTvEpisode() }
    }
}