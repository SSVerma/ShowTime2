package com.ssverma.shared.data.repository

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.api.service.tmdb.response.RemoteTvShow
import com.ssverma.shared.data.mapper.GenresMapper
import com.ssverma.shared.data.mapper.ListMapper
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.data.mapper.asQueryMap
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.repository.DiscoveryRepository
import javax.inject.Inject

import com.ssverma.shared.data.mapper.asProviderInfos
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.first

class DefaultDiscoveryRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService,
    private val appConfigRepository: AppConfigRepository,
    private val moviesMapper: @JvmSuppressWildcards ListMapper<RemoteMovie, Movie>,
    private val tvShowsMapper: @JvmSuppressWildcards ListMapper<RemoteTvShow, TvShow>,
    private val genresMapper: GenresMapper
) : DiscoveryRepository {

    override suspend fun discoverMovies(discoverConfig: MovieDiscoverConfig): Result<List<Movie>, Failure.CoreFailure> {
        return tmdbApiService.getDiscoveredMovies(
            queryMap = discoverConfig.asQueryMap(),
            page = 1
        ).asDomainResult { moviesMapper.map(it.body.results.orEmpty()) }
    }

    override suspend fun discoverTvShows(discoverConfig: TvDiscoverConfig): Result<List<TvShow>, Failure.CoreFailure> {
        return tmdbApiService.getDiscoveredTvShows(
            queryMap = discoverConfig.asQueryMap(),
            page = 1
        ).asDomainResult { tvShowsMapper.map(it.body.results.orEmpty()) }
    }

    override suspend fun discoverUniversal(
        filter: UniversalDiscoveryFilter,
        page: Int
    ): Result<List<UniversalMediaItem>, Failure.CoreFailure> {
        val queryMap = mutableMapOf<String, String>()

        queryMap["sort_by"] = filter.sortOrder.apiValue

        val minVoteAvg = filter.minRating ?: filter.vibePreset.minVoteAverage
        queryMap["vote_average.gte"] = minVoteAvg.toString()
        queryMap["vote_count.gte"] = filter.vibePreset.minVoteCount.toString()

        if (filter.mediaType == MediaType.Movie) {
            if (filter.vibePreset.movieGenreIds.isNotEmpty()) {
                queryMap["with_genres"] = filter.vibePreset.movieGenreIds.joinToString(",")
            }
        } else {
            if (filter.vibePreset.tvGenreIds.isNotEmpty()) {
                queryMap["with_genres"] = filter.vibePreset.tvGenreIds.joinToString(",")
            }
        }

        if (filter.selectedProviderIds.isNotEmpty()) {
            queryMap["with_watch_providers"] = filter.selectedProviderIds.joinToString("|")
            queryMap["watch_region"] = filter.watchRegion
        }

        filter.studioHub?.let { hub ->
            queryMap["with_companies"] = hub.companyId.toString()
        }

        filter.decade.startYear?.let { start ->
            if (filter.mediaType == MediaType.Movie) {
                queryMap["primary_release_date.gte"] = "$start-01-01"
            } else {
                queryMap["first_air_date.gte"] = "$start-01-01"
            }
        }
        filter.decade.endYear?.let { end ->
            if (filter.mediaType == MediaType.Movie) {
                queryMap["primary_release_date.lte"] = "$end-12-31"
            } else {
                queryMap["first_air_date.lte"] = "$end-12-31"
            }
        }

        filter.vibePreset.maxRuntimeMinutes?.let { maxRuntime ->
            if (filter.mediaType == MediaType.Movie) {
                queryMap["with_runtime.lte"] = maxRuntime.toString()
            }
        }

        return if (filter.mediaType == MediaType.Movie) {
            tmdbApiService.getDiscoveredMovies(queryMap, page).asDomainResult { response ->
                response.body.results.orEmpty().map { remote ->
                    UniversalMediaItem(
                        id = remote.id,
                        mediaType = MediaType.Movie,
                        title = remote.title.orEmpty(),
                        overview = remote.overview.orEmpty(),
                        posterImageUrl = remote.posterPath.orEmpty(),
                        backdropImageUrl = remote.backdropPath.orEmpty(),
                        voteAvg = remote.voteAvg,
                        voteCount = remote.voteCount,
                        releaseDate = remote.releaseDate.orEmpty()
                    )
                }
            }
        } else {
            tmdbApiService.getDiscoveredTvShows(queryMap, page).asDomainResult { response ->
                response.body.results.orEmpty().map { remote ->
                    UniversalMediaItem(
                        id = remote.id,
                        mediaType = MediaType.Tv,
                        title = remote.title.orEmpty(),
                        overview = remote.overview.orEmpty(),
                        posterImageUrl = remote.posterPath.orEmpty(),
                        backdropImageUrl = remote.backdropPath.orEmpty(),
                        voteAvg = remote.voteAvg,
                        voteCount = remote.voteCount,
                        releaseDate = remote.firstAirDate.orEmpty()
                    )
                }
            }
        }
    }

    override suspend fun fetchMovieGenre(): Result<List<Genre>, Failure.CoreFailure> {
        return tmdbApiService.getMovieGenres()
            .asDomainResult { genresMapper.map(it.body.genres.orEmpty()) }
    }

    override suspend fun fetchTvShowGenre(): Result<List<Genre>, Failure.CoreFailure> {
        return tmdbApiService.getTvGenres()
            .asDomainResult { genresMapper.map(it.body.genres.orEmpty()) }
    }

    override suspend fun fetchWatchProviders(isMovie: Boolean): Result<List<ProviderInfo>, Failure.CoreFailure> {
        val region = appConfigRepository.watchProviderRegion.first()
        return if (isMovie) {
            tmdbApiService.getAllMovieWatchProviders(watchRegion = region).asDomainResult {
                it.body.asProviderInfos()
            }
        } else {
            tmdbApiService.getAllTvWatchProviders(watchRegion = region).asDomainResult {
                it.body.asProviderInfos()
            }
        }
    }
}
