package com.ssverma.shared.domain.model.community

/**
 * Single Source of Truth (SSOT) for discussion and comment moderation thresholds.
 */
object CommunityModerationConfig {
    /**
     * Default maximum report threshold before a comment is completely quarantined/filtered from queries.
     * Can be overridden dynamically via Firebase Remote Config ("remote_discussions_max_report_threshold").
     */
    const val DEFAULT_MAX_REPORT_THRESHOLD = 5L

    /**
     * Threshold of reports at which a comment is soft-flagged with a community warning shield ("Show anyway").
     */
    const val DEFAULT_FLAG_REPORT_THRESHOLD = 3
}
