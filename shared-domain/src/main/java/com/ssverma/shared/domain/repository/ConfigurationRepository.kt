package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.model.WatchProviderRegion

interface ConfigurationRepository {
    suspend fun fetchCountries(): CoreResult<List<WatchProviderRegion>>
    suspend fun fetchLanguages(): CoreResult<List<Language>>
}
