package com.ssverma.shared.domain.usecase

import com.ssverma.shared.domain.CoreResult
import com.ssverma.shared.domain.model.Language
import com.ssverma.shared.domain.repository.WatchProviderRepository
import javax.inject.Inject

class GetAvailableLanguagesUseCase @Inject constructor(
    private val watchProviderRepository: WatchProviderRepository
) {
    suspend operator fun invoke(): CoreResult<List<Language>> {
        return watchProviderRepository.fetchAvailableLanguages().asSuccess { languages ->
            languages.sortedBy { it.englishName }
        }
    }
}
