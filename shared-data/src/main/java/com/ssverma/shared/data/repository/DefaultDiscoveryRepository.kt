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

class DefaultDiscoveryRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService,
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

    override suspend fun fetchMovieGenre(): Result<List<Genre>, Failure.CoreFailure> {
        return tmdbApiService.getMovieGenres()
            .asDomainResult { genresMapper.map(it.body.genres.orEmpty()) }
    }

    override suspend fun fetchTvShowGenre(): Result<List<Genre>, Failure.CoreFailure> {
        return tmdbApiService.getTvGenres()
            .asDomainResult { genresMapper.map(it.body.genres.orEmpty()) }
    }
}
