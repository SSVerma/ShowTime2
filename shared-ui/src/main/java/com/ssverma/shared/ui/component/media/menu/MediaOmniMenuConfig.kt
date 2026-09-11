package com.ssverma.shared.ui.component.media.menu

data class MediaOmniMenuConfig(
    val showWatchlist: Boolean = true,
    val showWatched: Boolean = true,
    val showFavorite: Boolean = true,
    val showDiaryLog: Boolean = true,
    val showCustomList: Boolean = true,
    val showDiscussions: Boolean = true,
    val showShare: Boolean = true,
    val isUpcoming: Boolean = false,
    val isOverPoster: Boolean = true
) {
    val effectiveShowWatched: Boolean
        get() = showWatched && !isUpcoming

    val effectiveShowDiaryLog: Boolean
        get() = showDiaryLog && !isUpcoming

    companion object {
        val Default = MediaOmniMenuConfig()

        val Upcoming = MediaOmniMenuConfig(
            isUpcoming = true
        )

        val Episode = MediaOmniMenuConfig(
            showWatchlist = false,
            showWatched = true,
            showFavorite = false,
            showDiaryLog = true,
            showCustomList = false,
            showDiscussions = true,
            showShare = true
        )

        val Minimal = MediaOmniMenuConfig(
            showWatchlist = true,
            showWatched = true,
            showFavorite = true,
            showDiaryLog = false,
            showCustomList = false,
            showDiscussions = false,
            showShare = true
        )
    }
}

data class CustomListOption(
    val listId: String,
    val title: String,
    val isContained: Boolean = false
)
