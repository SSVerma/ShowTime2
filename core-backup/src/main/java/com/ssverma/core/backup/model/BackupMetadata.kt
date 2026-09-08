package com.ssverma.core.backup.model

data class BackupMetadata(
    val timestamp: Long,
    val formattedDate: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val deviceName: String,
    val featureCounts: Map<String, Int> = emptyMap()
) {
    constructor(
        timestamp: Long,
        formattedDate: String,
        sizeBytes: Long,
        formattedSize: String,
        deviceName: String,
        favoritesCount: Int = 0,
        watchlistCount: Int = 0,
        historyCount: Int = 0,
        customListsCount: Int = 0,
        customListItemsCount: Int = 0,
        diaryEntriesCount: Int = 0,
        showProgressCount: Int = 0,
        episodeHistoryCount: Int = 0,
        challengesCount: Int = 0,
        blindspotsCount: Int = 0,
        featureCounts: Map<String, Int> = emptyMap()
    ) : this(
        timestamp = timestamp,
        formattedDate = formattedDate,
        sizeBytes = sizeBytes,
        formattedSize = formattedSize,
        deviceName = deviceName,
        featureCounts = if (featureCounts.isNotEmpty()) {
            featureCounts
        } else {
            buildMap {
                if (favoritesCount > 0) put(KEY_FAVORITES, favoritesCount)
                if (watchlistCount > 0) put(KEY_WATCHLIST, watchlistCount)
                if (historyCount > 0) put(KEY_HISTORY, historyCount)
                if (customListsCount > 0) put(KEY_CUSTOM_LISTS, customListsCount)
                if (customListItemsCount > 0) put(KEY_CUSTOM_LIST_ITEMS, customListItemsCount)
                if (diaryEntriesCount > 0) put(KEY_DIARY_ENTRIES, diaryEntriesCount)
                if (showProgressCount > 0) put(KEY_SHOW_PROGRESS, showProgressCount)
                if (episodeHistoryCount > 0) put(KEY_EPISODE_HISTORY, episodeHistoryCount)
                if (challengesCount > 0) put(KEY_CHALLENGES, challengesCount)
                if (blindspotsCount > 0) put(KEY_BLINDSPOTS, blindspotsCount)
            }
        }
    )

    val favoritesCount: Int get() = featureCounts[KEY_FAVORITES] ?: 0
    val watchlistCount: Int get() = featureCounts[KEY_WATCHLIST] ?: 0
    val historyCount: Int get() = featureCounts[KEY_HISTORY] ?: 0
    val customListsCount: Int get() = featureCounts[KEY_CUSTOM_LISTS] ?: 0
    val customListItemsCount: Int get() = featureCounts[KEY_CUSTOM_LIST_ITEMS] ?: 0
    val diaryEntriesCount: Int get() = featureCounts[KEY_DIARY_ENTRIES] ?: 0
    val showProgressCount: Int get() = featureCounts[KEY_SHOW_PROGRESS] ?: 0
    val episodeHistoryCount: Int get() = featureCounts[KEY_EPISODE_HISTORY] ?: 0
    val challengesCount: Int get() = featureCounts[KEY_CHALLENGES] ?: 0
    val blindspotsCount: Int get() = featureCounts[KEY_BLINDSPOTS] ?: 0

    companion object {
        const val KEY_FAVORITES = "favorites"
        const val KEY_WATCHLIST = "watchlist"
        const val KEY_HISTORY = "history"
        const val KEY_CUSTOM_LISTS = "customLists"
        const val KEY_CUSTOM_LIST_ITEMS = "customListItems"
        const val KEY_DIARY_ENTRIES = "diaryEntries"
        const val KEY_SHOW_PROGRESS = "showProgress"
        const val KEY_EPISODE_HISTORY = "episodeHistory"
        const val KEY_CHALLENGES = "challenges"
        const val KEY_BLINDSPOTS = "blindspots"
    }
}
