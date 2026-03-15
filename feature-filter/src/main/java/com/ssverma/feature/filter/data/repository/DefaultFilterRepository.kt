package com.ssverma.feature.filter.data.repository

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.RemoteCompany
import com.ssverma.api.service.tmdb.response.RemoteKeyword
import com.ssverma.api.service.tmdb.response.RemoteNetwork
import com.ssverma.feature.filter.domain.repository.FilterRepository
import com.ssverma.shared.data.mapper.asDomainResult
import com.ssverma.shared.data.mapper.asGenres
import com.ssverma.shared.data.mapper.asLanguages
import com.ssverma.shared.data.mapper.asWatchProviderRegions
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultFilterRepository @Inject constructor(
    private val tmdbApiService: TmdbApiService
) : FilterRepository {

    private var cachedMovieGenres: List<Genre>? = null
    private var cachedTvGenres: List<Genre>? = null
    private var cachedLanguages: List<Language>? = null
    private var cachedCountries: List<WatchProviderRegion>? = null

    override suspend fun fetchMovieGenres(): Result<List<Genre>, Failure.CoreFailure> {
        cachedMovieGenres?.let { return Result.Success(it) }

        return tmdbApiService.getMovieGenres().asDomainResult {
            val genres = it.body.genres?.asGenres().orEmpty()
            cachedMovieGenres = genres
            genres
        }
    }

    override suspend fun fetchTvGenres(): Result<List<Genre>, Failure.CoreFailure> {
        cachedTvGenres?.let { return Result.Success(it) }

        return tmdbApiService.getTvGenres().asDomainResult {
            val genres = it.body.genres?.asGenres().orEmpty()
            cachedTvGenres = genres
            genres
        }
    }

    override suspend fun fetchLanguages(): Result<List<Language>, Failure.CoreFailure> {
        cachedLanguages?.let { return Result.Success(it) }

        return tmdbApiService.getLanguages().asDomainResult {
            val languages = it.body.asLanguages()
            cachedLanguages = languages
            languages
        }
    }

    override suspend fun fetchCountries(): Result<List<WatchProviderRegion>, Failure.CoreFailure> {
        cachedCountries?.let { return Result.Success(it) }

        return tmdbApiService.getCountries().asDomainResult {
            val countries = it.body.asWatchProviderRegions()
            cachedCountries = countries
            countries
        }
    }

    override suspend fun searchKeywords(query: String): Result<List<RemoteKeyword>, Failure.CoreFailure> {
        return tmdbApiService.searchKeywords(query).asDomainResult { 
            it.body.results ?: emptyList<RemoteKeyword>()
        }
    }

    override suspend fun searchCompanies(query: String): Result<List<RemoteCompany>, Failure.CoreFailure> {
        return tmdbApiService.searchCompanies(query).asDomainResult { 
            it.body.results ?: emptyList()
        }
    }

    override suspend fun searchNetworks(query: String): Result<List<RemoteNetwork>, Failure.CoreFailure> {
        return tmdbApiService.searchNetworks(query).asDomainResult { 
            it.body.results ?: emptyList()
        }
    }
}
