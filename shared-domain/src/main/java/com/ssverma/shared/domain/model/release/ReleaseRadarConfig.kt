package com.ssverma.shared.domain.model.release

/**
 * Configuration and remote feature flag constants for the Smart Release Radar worker.
 */
object ReleaseRadarConfig {
    /**
     * Remote config flag to remotely enable or disable the Release Radar background worker and UI toggle.
     */
    const val REMOTE_KEY_RELEASE_RADAR_ENABLED = "remote_release_radar_enabled"

    /**
     * Default value for [REMOTE_KEY_RELEASE_RADAR_ENABLED].
     */
    const val DEFAULT_RELEASE_RADAR_ENABLED = true

    /**
     * Strict rate limit on TMDB watch provider API calls per daily execution run.
     */
    const val MAX_STREAMING_CALLS_PER_DAY = 3

    /**
     * Minimum days elapsed after theatrical release before querying TMDB for streaming availability.
     */
    const val STREAMING_WINDOW_MIN_DAYS = 30L

    /**
     * Maximum days after theatrical release after which streaming polling ceases.
     */
    const val STREAMING_WINDOW_MAX_DAYS = 120L

    /**
     * Recheck cooldown in days before checking TMDB watch providers again if a movie hasn't arrived on streaming.
     */
    const val STREAMING_RECHECK_COOLDOWN_DAYS = 7L

    /**
     * Default fallback ISO-3166-1 alpha-2 country code for watch provider lookups.
     */
    const val DEFAULT_REGION = "US"
}
