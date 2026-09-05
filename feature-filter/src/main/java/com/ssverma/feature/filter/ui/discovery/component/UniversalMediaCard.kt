package com.ssverma.feature.filter.ui.discovery.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.ui.component.media.menu.MediaOmniMenuConfig
import com.ssverma.shared.ui.component.media.UniversalMediaCard as SharedUniversalMediaCard

@Composable
fun UniversalMediaCard(
    item: UniversalMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGridView: Boolean = true,
    isFavorite: Boolean? = null,
    isInWatchlist: Boolean? = null,
    isWatched: Boolean? = null,
    isActionActive: Boolean? = null,
    onToggleWatchlist: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onToggleWatched: (() -> Unit)? = null,
    onLogToDiary: (() -> Unit)? = null,
    onCustomListClick: (() -> Unit)? = null,
    onOpenDiscussions: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null,
    menuConfig: MediaOmniMenuConfig = MediaOmniMenuConfig.Default
) {
    SharedUniversalMediaCard(
        item = item,
        onClick = onClick,
        modifier = modifier,
        isGridView = isGridView,
        isFavorite = isFavorite,
        isInWatchlist = isInWatchlist,
        isWatched = isWatched,
        isActionActive = isActionActive,
        onToggleWatchlist = onToggleWatchlist,
        onToggleFavorite = onToggleFavorite,
        onToggleWatched = onToggleWatched,
        onLogToDiary = onLogToDiary,
        onCustomListClick = onCustomListClick,
        onOpenDiscussions = onOpenDiscussions,
        onShare = onShare,
        onShowFeedback = onShowFeedback,
        menuConfig = menuConfig
    )
}
