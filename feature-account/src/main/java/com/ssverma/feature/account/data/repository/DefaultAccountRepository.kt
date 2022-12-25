package com.ssverma.feature.account.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.api.service.tmdb.TmdbDefaults
import com.ssverma.api.service.tmdb.paging.MoviePagingSource
import com.ssverma.api.service.tmdb.paging.TvShowsPagingSource
import com.ssverma.api.service.tmdb.request.FavoriteMediaBody
import com.ssverma.api.service.tmdb.request.WatchlistMediaBody
import com.ssverma.feature.account.data.local.AccountLocalDataSource
import com.ssverma.feature.account.data.mapper.MediaStatsMapper
import com.ssverma.feature.account.data.mapper.ProfileMapper
import com.ssverma.feature.account.data.remote.AccountRemoteDataSource
import com.ssverma.feature.account.domain.model.MediaStats
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.sessionIdOrNull
import com.ssverma.shared.data.mapper.MoviesMapper
import com.ssverma.shared.data.mapper.TvShowsMapper
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class DefaultAccountRepository @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val authManager: AuthManager,
    private val profileMapper: ProfileMapper,
    private val mediaStatsMapper: MediaStatsMapper,
    private val moviesMapper: MoviesMapper,
    private val tvShowsMapper: TvShowsMapper
) : AccountRepository {

    override suspend fun fetchProfile(sessionId: String): Result<Profile, Failure.CoreFailure> {
        val userAccount = accountLocalDataSource.loadUserAccount()
        profileMapper.mapLocal(userAccount)?.let { return Result.Success(it) }

        return accountRemoteDataSource
            .fetchAccountDetails(sessionId = sessionId)
            .asDomainResult {
                profileMapper.mapRemote(it.body).also { profile ->
                    accountLocalDataSource.persistUserAccount(
                        accountId = profile.id,
                        userName = profile.userName,
                        displayName = profile.displayName,
                        imageUrl = profile.imageUrl
                    )
                }
            }
    }

    override suspend fun removeUserAccount() {
        accountLocalDataSource.clearUserAccount()
    }

    override suspend fun fetchAccountStats(
        sessionId: String,
        mediaType: MediaType,
        mediaId: Int
    ): CoreResult<MediaStats> {
        return when (mediaType) {
            MediaType.Movie -> {
                accountRemoteDataSource.fetchMovieAccountStats(
                    movieId = mediaId,
                    sessionId = sessionId
                ).asDomainResult { mediaStatsMapper.map(it.body) }
            }
            MediaType.Tv -> {
                accountRemoteDataSource.fetchTvShowAccountStats(
                    tvShowId = mediaId,
                    sessionId = sessionId
                ).asDomainResult { mediaStatsMapper.map(it.body) }
            }
            else -> {
                Result.Error(Failure.CoreFailure.UnexpectedFailure)
            }
        }
    }

    override suspend fun toggleMediaFavoriteStatus(
        sessionId: String,
        mediaType: MediaType,
        mediaId: Int,
        favorite: Boolean
    ): CoreResult<Unit> {
        val accountId = accountLocalDataSource.loadUserAccount().accountId

        val mediaTypeAsString = when (mediaType) {
            MediaType.Movie -> {
                TmdbApiTiedConstants.AvailableMediaTypes.Movie
            }
            MediaType.Tv -> {
                TmdbApiTiedConstants.AvailableMediaTypes.Tv
            }
            MediaType.Person,
            MediaType.Unknown -> {
                return Result.Error(Failure.CoreFailure.UnexpectedFailure)
            }
        }

        val favoriteBody = FavoriteMediaBody(
            mediaType = mediaTypeAsString,
            mediaId = mediaId,
            favorite = favorite
        )

        return accountRemoteDataSource.markMediaAsFavorite(
            sessionId = sessionId,
            accountId = accountId,
            favoriteBody = favoriteBody
        ).asDomainResult {}
    }

    override suspend fun toggleMediaWatchlistStatus(
        sessionId: String,
        mediaType: MediaType,
        mediaId: Int,
        inWatchlist: Boolean
    ): CoreResult<Unit> {
        val accountId = accountLocalDataSource.loadUserAccount().accountId

        val mediaTypeAsString = when (mediaType) {
            MediaType.Movie -> {
                TmdbApiTiedConstants.AvailableMediaTypes.Movie
            }
            MediaType.Tv -> {
                TmdbApiTiedConstants.AvailableMediaTypes.Tv
            }
            MediaType.Person,
            MediaType.Unknown -> {
                return Result.Error(Failure.CoreFailure.UnexpectedFailure)
            }
        }

        val watchlistBody = WatchlistMediaBody(
            mediaType = mediaTypeAsString,
            mediaId = mediaId,
            inWatchlist = inWatchlist
        )

        return accountRemoteDataSource.markMediaInWatchlist(
            sessionId = sessionId,
            accountId = accountId,
            watchlistBody = watchlistBody
        ).asDomainResult {}
    }

    override fun fetchFavoriteMoviesGradually(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
            pagingSourceFactory = {
                MoviePagingSource(
                    movieApiCall = { pageNumber ->
                        val accountId = accountLocalDataSource.loadUserAccount().accountId

                        accountRemoteDataSource.fetchFavoriteMovies(
                            accountId = accountId,
                            sessionId = authManager.sessionIdOrNull().orEmpty(),
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { moviesMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchFavoriteTvShowsGradually(): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
            pagingSourceFactory = {
                TvShowsPagingSource(
                    tvShowApiCall = { pageNumber ->
                        val accountId = accountLocalDataSource.loadUserAccount().accountId

                        accountRemoteDataSource.fetchFavoriteTvShows(
                            accountId = accountId,
                            sessionId = authManager.sessionIdOrNull().orEmpty(),
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { tvShowsMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchWatchlistMoviesGradually(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
            pagingSourceFactory = {
                MoviePagingSource(
                    movieApiCall = { pageNumber ->
                        val accountId = accountLocalDataSource.loadUserAccount().accountId

                        accountRemoteDataSource.fetchWatchlistMovies(
                            accountId = accountId,
                            sessionId = authManager.sessionIdOrNull().orEmpty(),
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { moviesMapper.map(it) }
                )
            }
        ).flow
    }

    override fun fetchWatchlistTvShowsGradually(): Flow<PagingData<TvShow>> {
        return Pager(
            config = PagingConfig(pageSize = TmdbDefaults.ApiDefaults.PageSize),
            pagingSourceFactory = {
                TvShowsPagingSource(
                    tvShowApiCall = { pageNumber ->
                        val accountId = accountLocalDataSource.loadUserAccount().accountId

                        accountRemoteDataSource.fetchWatchlistTvShows(
                            accountId = accountId,
                            sessionId = authManager.sessionIdOrNull().orEmpty(),
                            page = pageNumber
                        )
                    },
                    mapRemoteToDomain = { tvShowsMapper.map(it) }
                )
            }
        ).flow
    }
}