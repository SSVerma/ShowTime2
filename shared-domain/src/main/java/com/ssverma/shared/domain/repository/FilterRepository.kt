package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Company
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.Keyword
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.Network
import com.ssverma.shared.domain.model.WatchProviderRegion

interface FilterRepository {
    suspend fun fetchMovieGenres(): Result<List<Genre>, Failure.CoreFailure>
    suspend fun fetchTvGenres(): Result<List<Genre>, Failure.CoreFailure>
    suspend fun fetchLanguages(): Result<List<Language>, Failure.CoreFailure>
    suspend fun fetchCountries(): Result<List<WatchProviderRegion>, Failure.CoreFailure>
    suspend fun searchKeywords(query: String): Result<List<Keyword>, Failure.CoreFailure>
    suspend fun searchCompanies(query: String): Result<List<Company>, Failure.CoreFailure>
    suspend fun searchNetworks(query: String): Result<List<Network>, Failure.CoreFailure>
}
