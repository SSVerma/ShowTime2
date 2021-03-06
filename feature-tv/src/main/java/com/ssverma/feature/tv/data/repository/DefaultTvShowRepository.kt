package com.ssverma.feature.tv.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.api.service.tmdb.TmdbDefaults
import com.ssverma.api.service.tmdb.paging.ReviewsPagingSource
import com.ssverma.api.service.tmdb.paging.TvShowsPagingSource
import com.ssverma.api.service.tmdb.response.*
import com.ssverma.feature.tv.data.remote.TvShowRemoteDataSource
import com.ssverma.feature.tv.domain.failure.TvEpisodeFailure
import com.ssverma.feature.tv.domain.failure.TvSeasonFailure
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.feature.tv.domain.model.TvEpisodeConfig
import com.ssverma.feature.tv.domain.model.TvSeasonConfig
import com.ssverma.feature.tv.domain.model.TvShowDetailsConfig
import com.ssverma.feature.tv.domain.repository.TvShowRepository
import com.ssverma.shared.data.mapper.*
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TimeWindow
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Review
import com.ssverma.shared.domain.model.tv.TvEpisode
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.domain.model.tv.TvShow
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
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
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
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
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
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
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

    override suspend fun fetchTopRatedTvShows(): Result<List<TvShow>, Failure<TvShowFailure>> {
        val apiResponse = tvShowRemoteDataSource.fetchTopRatedTvShows(
            page = TmdbDefaults.ApiDefaults.FirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvShowsMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun fetchTrendingTvShows(
        timeWindow: TimeWindow
    ): Result<List<TvShow>, Failure<TvShowFailure>> {
        val apiResponse = tvShowRemoteDataSource.fetchTrendingTvShows(
            timeWindow = timeWindow.asTmdbQueryValue(),
            page = TmdbDefaults.ApiDefaults.FirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvShowsMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun discoverTvShows(
        discoverConfig: TvDiscoverConfig
    ): Result<List<TvShow>, Failure<TvShowFailure>> {
        val apiResponse = tvShowRemoteDataSource.discoverTvShows(
            queryMap = discoverConfig.asQueryMap(),
            page = TmdbDefaults.ApiDefaults.FirstPageNumber
        )

        return apiResponse.asDomainResult(
            mapRemoteToDomain = {
                tvShowsMapper.map(it.body.results.orEmpty())
            }
        )
    }

    override suspend fun fetchTvShowGenre(): Result<List<Genre>, Failure.CoreFailure> {
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
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
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
    ): Result<TvShow, Failure<TvShowFailure>> {
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
    ): Result<TvSeason, Failure<TvSeasonFailure>> {
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
    ): Result<TvEpisode, Failure<TvEpisodeFailure>> {
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