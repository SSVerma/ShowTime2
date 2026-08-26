package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.repository.ConfigurationRepository

class FakeConfigurationRepository(
    var countries: List<WatchProviderRegion> = listOf(
        WatchProviderRegion("US", "United States", "United States"),
        WatchProviderRegion("IN", "India", "भारत"),
        WatchProviderRegion("GB", "United Kingdom", "United Kingdom")
    ),
    var languages: List<Language> = listOf(
        Language("en", "English", "English")
    )
) : ConfigurationRepository {

    override suspend fun fetchCountries(): CoreResult<List<WatchProviderRegion>> {
        return Result.Success(countries)
    }

    override suspend fun fetchLanguages(): CoreResult<List<Language>> {
        return Result.Success(languages)
    }
}
