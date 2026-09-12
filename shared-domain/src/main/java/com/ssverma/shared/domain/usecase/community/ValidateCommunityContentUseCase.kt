package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.ContentModerationResult
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class ValidateCommunityContentUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(title: String, description: String? = null): ContentModerationResult {
        val combinedText = buildString {
            append(title)
            if (!description.isNullOrBlank()) {
                append(" ")
                append(description)
            }
        }.trim()

        if (combinedText.isEmpty()) {
            return ContentModerationResult.Approved
        }

        // 1. Severe / Illegal Prohibited Check (Hard Block)
        val severePattern = communityRepository.getCommunitySevereBlockedRegex()
        if (severePattern.isNotBlank()) {
            runCatching {
                val regex = Regex(severePattern, RegexOption.IGNORE_CASE)
                val match = regex.find(combinedText)
                if (match != null) {
                    return ContentModerationResult.Prohibited(match.value)
                }
            }
        }

        // 2. Sensitive Cinema Themes (Requires user acknowledgment, NOT a false positive block)
        val sensitivePattern = communityRepository.getCommunitySensitiveConfirmRegex()
        if (sensitivePattern.isNotBlank()) {
            runCatching {
                val regex = Regex(sensitivePattern, RegexOption.IGNORE_CASE)
                val match = regex.find(combinedText)
                if (match != null) {
                    return ContentModerationResult.SensitiveWarning(match.value)
                }
            }
        }

        return ContentModerationResult.Approved
    }
}
