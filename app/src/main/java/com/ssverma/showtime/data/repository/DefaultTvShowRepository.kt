package com.ssverma.showtime.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.api.service.tmdb.TMDB_API_PAGE_SIZE
import com.ssverma.api.service.tmdb.paging.ReviewsPagingSource
import com.ssverma.api.service.tmdb.paging.TvShowsPagingSource
import com.ssverma.api.service.tmdb.response.*
import com.ssverma.api.service.tmdb.utils.TmdbFirstPageNumber
import com.ssverma.showtime.data.mapper.*
import com.ssverma.showtime.data.remote.TvShowRemoteDataSource
import com.ssverma.showtime.domain.DomainResult
import com.ssverma.showtime.domain.TimeWindow
import com.ssverma.showtime.domain.TvDiscoverConfig
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.domain.failure.tv.TvEpisodeFailure
import com.ssverma.showtime.domain.failure.tv.TvSeasonFailure
import com.ssverma.showtime.domain.failure.tv.TvShowFailure
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.Review
import com.ssverma.showtime.domain.model.tv.*
import com.ssverma.showtime.domain.repository.TvShowRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultTvShowRepository @Inject constructor(
    private val tvShowRemoteDataSource: TvShowRemoteDataSource,
    private val tvShowsMapper: @JvmSuppressWildcards ListMapper<RemoteTvShow, TvShow>,
    private val generesMapper: @JvmSuppressWildcards ListMapper<RemoteGenre, Genre>,
    private val reviewsMapper: @JvmSuppressWildcards ListMapper<RemoteReview, Review>,
    private val tvShowMapper: @JvmSuppressWildcards Mapper<RemoteTvShow, TvShow>,
    private val tvSeasonMapper: @JvmSuppressWildcards Mapper<RemoteTvSeason, TvSeason>,
    private val tvEpisodeMapper: @JvmSuppressWildcards Mapper<RemoteTvEpisode, TvEpisode>,
) : TvShowRepository {

    override fun discoverTvShowsGradually(
        discoverConfig: TvDiscoverConfig
    ): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource(
                    tvShowApiCall = { pageNumber ->
                        tvShowRemoteDataSource.discoverTvShows(
                            queryMap = discoverConfig.asQueryMap(),
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { tvShowsMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchTrendingTvShowsGradually(
        timeWindow: TimeWindow
    ): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource(
                    tvShowApiCall = { pageNumber ->
                        tvShowRemoteDataSource.fetchTrendingTvShows(
                            timeWindow = timeWindow.asTmdbQueryValue(),
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { tvShowsMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchTopRatedTvShowsGradually(): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                TvShowsPagingSource(
                    tvShowApiCall = { pageNumber ->
                        tvShowRemoteDataSource.fetchTopRatedTvShows(
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { tvShowsMapper.map(it) }
                )
            }
        ).flow
    }

    override suspend fun fetchTopRatedTvShows(): DomainResult<List<TvShow>, Failure<TvShowFailure>> {
        val apiResponse = tvShowRemoteDataSource.fetchTopRatedTvShows(page = TmdbFirstPageNumber)

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvShowsMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun fetchTrendingTvShows(
        timeWindow: TimeWindow
    ): DomainResult<List<TvShow>, Failure<TvShowFailure>> {
        val apiResponse = tvShowRemoteDataSource.fetchTrendingTvShows(
            timeWindow = timeWindow.asTmdbQueryValue(),
            page = TmdbFirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvShowsMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun discoverTvShows(
        discoverConfig: TvDiscoverConfig
    ): DomainResult<List<TvShow>, Failure<TvShowFailure>> {
        val apiResponse = tvShowRemoteDataSource.discoverTvShows(
            queryMap = discoverConfig.asQueryMap(),
            page = TmdbFirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvShowsMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun fetchTvShowGenre(): DomainResult<List<Genre>, Failure.CoreFailure> {
        val apiResponse = tvShowRemoteDataSource.fetchTvShowGenre()

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                generesMapper.map(it.body.genres.orEmpty())
            }
        )
    }

    override fun fetchTvShowReviewsGradually(
        tvShowId: Int
    ): Flow<PagingData<Review>> {
        return Pager(
            config = PagingConfig(pageSize = TMDB_API_PAGE_SIZE),
            pagingSourceFactory = {
                ReviewsPagingSource(
                    reviewsApiCall = { pageNumber ->
                        tvShowRemoteDataSource.fetchTvShowReviewsGradually(
                            tvShowId = tvShowId,
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { reviewsMapper.map(it) }
                )
            }
        ).flow
    }

    override suspend fun fetchTvShowDetails(
        detailsConfig: TvShowDetailsConfig
    ): DomainResult<TvShow, Failure<TvShowFailure>> {
        val apiResponse = tvShowRemoteDataSource.fetchTvShowDetails(
            tvShowId = detailsConfig.tvShowId,
            queryMap = detailsConfig.appendable.asQueryMap()
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvShowMapper.map(it.body)
            }
        )
    }

    override suspend fun fetchTvSeasonDetails(
        seasonConfig: TvSeasonConfig
    ): DomainResult<TvSeason, Failure<TvSeasonFailure>> {
        val apiResponse = tvShowRemoteDataSource.fetchTvSeasonDetails(
            tvShowId = seasonConfig.tvShowId,
            seasonNumber = seasonConfig.seasonNumber,
            queryMap = seasonConfig.appendable.asQueryMap()
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvSeasonMapper.map(it.body)
            }
        )
    }

    override suspend fun fetchTvEpisodeDetails(
        tvEpisodeConfig: TvEpisodeConfig
    ): DomainResult<TvEpisode, Failure<TvEpisodeFailure>> {
        val apiResponse = tvShowRemoteDataSource.fetchTvEpisodeDetails(
            tvShowId = tvEpisodeConfig.tvShowId,
            seasonNumber = tvEpisodeConfig.seasonNumber,
            episodeNumber = tvEpisodeConfig.episodeNumber,
            queryMap = tvEpisodeConfig.appendable.asQueryMap()
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvEpisodeMapper.map(it.body)
            }
        )
    }
}