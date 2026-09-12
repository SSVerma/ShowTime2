package com.ssverma.shared.domain.model.community

/**
 * Single Source of Truth (SSOT) for discussion, comment, and curated list content moderation.
 *
 * Defines Firebase Remote Config parameter keys and local default fallback constants for:
 * 1. Community Curated List moderation thresholds (soft-flagging vs. auto-quarantine).
 * 2. Discussion & comment report thresholds.
 * 3. Pre-publish text moderation regex rules (zero-tolerance severe terms vs. sensitive themes).
 */
object CommunityModerationConfig {
    /**
     * Default maximum report threshold before a discussion comment is completely quarantined and
     * filtered from public media query results.
     *
     * Can be overridden dynamically via Firebase Remote Config parameter:
     * `"remote_discussions_max_report_threshold"`.
     */
    const val DEFAULT_MAX_REPORT_THRESHOLD = 5L

    /**
     * Default threshold of reports at which a discussion comment is soft-flagged with a community
     * warning shield ("Show anyway"), allowing users to opt in to view potentially sensitive discussions.
     */
    const val DEFAULT_FLAG_REPORT_THRESHOLD = 3

    /**
     * Firebase Remote Config key for the dynamic threshold of unique reports at which a
     * Community Curated List is soft-flagged with a warning shield ("Show Collection").
     *
     * Value in Remote Config should be an integer/long (e.g., `5`).
     */
    const val REMOTE_KEY_COMMUNITY_LISTS_FLAG_THRESHOLD = "remote_community_lists_flag_threshold"

    /**
     * Default fallback threshold of unique user reports at which a Community Curated List is soft-flagged.
     *
     * When reached, the collection remains discoverable in public feeds, but its card is visually
     * dimmed with a "Community Flagged" warning overlay. Card click navigation is locked until the
     * cinephile explicitly taps "Show Collection" to unmask it.
     */
    const val DEFAULT_COMMUNITY_LISTS_FLAG_THRESHOLD = 5L

    /**
     * Firebase Remote Config key for the maximum unique reports threshold before a Community Curated
     * List is automatically quarantined from public feeds.
     *
     * Value in Remote Config should be an integer/long (e.g., `10`).
     */
    const val REMOTE_KEY_COMMUNITY_LISTS_MAX_REPORT_THRESHOLD =
        "remote_community_lists_max_report_threshold"

    /**
     * Default fallback maximum unique reports threshold before a Community Curated List is
     * automatically quarantined from public category query results pending human admin triage.
     *
     * Quarantined lists are never permanently deleted from Firestore, preserving creator data
     * and allowing manual restoration or whitelisting via the Admin Panel.
     */
    const val DEFAULT_COMMUNITY_LISTS_MAX_REPORT_THRESHOLD = 10L

    /**
     * Firebase Remote Config key for dynamic updates to the severe/illegal terms regular expression.
     *
     * Allows remotely updating content moderation rules without requiring an app update or release.
     */
    const val REMOTE_KEY_COMMUNITY_SEVERE_BLOCKED_REGEX = "remote_community_severe_blocked_regex"

    /**
     * Default fallback regular expression matching severe, illegal, hate speech, or sexually explicit terms.
     *
     * Content matching this regex is hard-blocked prior to publishing with an immediate error dialog.
     * Employs strict word boundaries (`\b`) and case-insensitive matching (`(?i)`) to prevent false positives
     * on legitimate cinema vocabulary.
     */
    const val DEFAULT_SEVERE_BLOCKED_REGEX =
        """\b(?i)(cp|child\s*porn(?:ography)?|child\s*exploitation|underage\s*exploitation|bestiality|rape|nazi|hitler|faggot|nigger|kike)\b"""

    /**
     * Firebase Remote Config key for dynamic updates to the sensitive cinema themes regular expression.
     *
     * Allows remotely adjusting keywords that trigger a confirmation dialog prior to publishing.
     */
    const val REMOTE_KEY_COMMUNITY_SENSITIVE_CONFIRM_REGEX =
        "remote_community_sensitive_confirm_regex"

    /**
     * Default fallback regular expression matching mature or sensitive cinema themes (e.g., erotica, nudity).
     *
     * Content matching this pattern prompts the creator with a confirmation dialog to verify that the collection
     * complies with ShowTime's community guidelines before publishing.
     *
     * Uses strict word boundaries (`\b`) so benign film titles (such as "Classic", "Cocktail",
     * "Passionate", or "The Assassin") never trigger false positive warnings.
     */
    const val DEFAULT_SENSITIVE_CONFIRM_REGEX =
        """\b(?i)(sex|porn|porno|erotic|erotica|nudity|naked|fetish|hentai|incest|bdsm|hardcore|explicit)\b"""
}
