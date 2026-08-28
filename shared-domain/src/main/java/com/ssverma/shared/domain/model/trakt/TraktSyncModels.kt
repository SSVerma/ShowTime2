package com.ssverma.shared.domain.model.trakt

/**
 * Domain model representing the next unwatched episode in an in-progress TV show.
 */
data class TraktUpNextEpisode(
    val showTmdbId: Int,
    val showTitle: String,
    val showPosterPath: String? = null,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeTitle: String?,
    val episodeOverview: String? = null,
    val seasonCompleted: Int = 0,
    val seasonTotalAired: Int = 0,
    val totalAired: Int,
    val totalCompleted: Int
) {
    val seasonProgressPercentage: Float
        get() = if (seasonTotalAired > 0) seasonCompleted.toFloat() / seasonTotalAired.toFloat() else progressPercentage

    val progressPercentage: Float
        get() = if (totalAired > 0) totalCompleted.toFloat() / totalAired.toFloat() else 0f
}

/**
 * Summary of a 2-way Trakt sync operation.
 */
data class TraktSyncResult(
    val itemsImportedToWatchlist: Int,
    val itemsImportedToHistory: Int,
    val itemsExportedToTrakt: Int
)

/**
 * State for showing the Season Completion celebration dialog.
 */
data class CompletedShowDialogState(
    val showTmdbId: Int,
    val showTitle: String,
    val showPosterPath: String? = null,
    val seasonNumber: Int = 1,
    val totalCompleted: Int = 0,
    val totalAired: Int = 0
)
