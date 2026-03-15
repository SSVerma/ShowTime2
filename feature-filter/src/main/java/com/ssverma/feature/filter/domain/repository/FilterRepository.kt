package com.ssverma.feature.filter.domain.repository

import com.ssverma.api.service.tmdb.response.RemoteCompany
import com.ssverma.api.service.tmdb.response.RemoteKeyword
import com.ssverma.api.service.tmdb.response.RemoteNetwork
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion

interface FilterRepository {
    suspend fun fetchMovieGenres(): Result<List<Genre>, Failure.CoreFailure>
    suspend fun fetchTvGenres(): Result<List<Genre>, Failure.CoreFailure>
    suspend fun fetchLanguages(): Result<List<Language>, Failure.CoreFailure>
    suspend fun fetchCountries(): Result<List<WatchProviderRegion>, Failure.CoreFailure>
    suspend fun searchKeywords(query: String): Result<List<RemoteKeyword>, Failure.CoreFailure>

    suspend fun searchCompanies(query: String): Result<List<RemoteCompany>, Failure.CoreFailure>

    suspend fun searchNetworks(query: String): Result<List<RemoteNetwork>, Failure.CoreFailure>
}
