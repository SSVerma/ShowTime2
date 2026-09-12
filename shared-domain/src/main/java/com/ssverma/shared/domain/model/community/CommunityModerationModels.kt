package com.ssverma.shared.domain.model.community

enum class CommunityReportReason {
    InappropriateOrSexual,
    HateOrHarassment,
    SpamOrCommercial,
    SpoilersWithoutWarning,
    Other
}

data class ReportCommunityListParams(
    val listId: String,
    val authorId: String,
    val reason: CommunityReportReason,
    val details: String? = null
)

sealed interface ContentModerationResult {
    data object Approved : ContentModerationResult

    data class SensitiveWarning(
        val flaggedTerm: String
    ) : ContentModerationResult

    data class Prohibited(
        val blockedTerm: String
    ) : ContentModerationResult
}
