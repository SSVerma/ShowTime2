package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.media.menu.MediaOmniActionMenu
import com.ssverma.shared.ui.component.media.menu.MediaOmniMenuConfig

@Composable
fun UniversalMediaCard(
    item: UniversalMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGridView: Boolean = true,
    onToggleFavorite: (() -> Unit)? = null,
    onToggleWatchlist: (() -> Unit)? = null,
    onToggleWatched: (() -> Unit)? = null,
    onLogToDiary: (() -> Unit)? = null,
    onCustomListClick: (() -> Unit)? = null,
    onOpenDiscussions: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onShowFeedback: ((message: String, actionLabel: String?, targetCustomListId: String?) -> Unit)? = null,
    menuConfig: MediaOmniMenuConfig = MediaOmniMenuConfig.Default,
    topStartSlot: (@Composable () -> Unit)? = null,
    topEndSlot: (@Composable () -> Unit)? = null,
    bottomStartSlot: (@Composable () -> Unit)? = null,
    subtitleSlot: (@Composable () -> Unit)? = null,
    trailingActionSlot: (@Composable () -> Unit)? = null
) {
    val isActionActive = item.isInWatchlist || item.isWatched || item.isFavorite

    val hasOverflowOptions = onToggleWatchlist != null ||
            onToggleWatched != null ||
            onToggleFavorite != null ||
            onLogToDiary != null ||
            onCustomListClick != null ||
            onOpenDiscussions != null ||
            onShare != null ||
            menuConfig.showCustomList

    @Composable
    fun RenderOmniAction(isOverPoster: Boolean) {
        MediaOmniActionMenu(
            mediaId = item.id,
            mediaType = item.mediaType,
            title = item.title,
            posterImageUrl = item.posterImageUrl,
            backdropImageUrl = item.backdropImageUrl,
            voteAvg = item.voteAvg,
            releaseDate = item.releaseDate,
            isActionActive = isActionActive,
            isInWatchlist = item.isInWatchlist,
            isWatched = item.isWatched,
            isFavorite = item.isFavorite,
            onToggleWatchlist = onToggleWatchlist,
            onToggleWatched = onToggleWatched,
            onToggleFavorite = onToggleFavorite,
            onLogToDiary = onLogToDiary,
            onCustomListClick = onCustomListClick,
            onOpenDiscussions = onOpenDiscussions,
            onShare = onShare,
            onShowFeedback = onShowFeedback,
            config = menuConfig,
            isOverPoster = isOverPoster
        )
    }

    val resolvedTopEndAction: (@Composable () -> Unit)? = when {
        topEndSlot != null -> topEndSlot
        hasOverflowOptions -> {
            { RenderOmniAction(isOverPoster = true) }
        }

        else -> null
    }

    val resolvedTopStartBadge: (@Composable () -> Unit)? = when {
        topStartSlot != null -> topStartSlot
        item.voteAvg > 0f -> {
            { MediaCardRatingBadge(rating = item.voteAvg) }
        }

        else -> null
    }

    val resolvedBottomStartBadge: (@Composable () -> Unit)? = when {
        bottomStartSlot != null -> bottomStartSlot
        item.isWatched -> {
            { MediaCardWatchedBadge(onClick = onToggleWatched) }
        }

        else -> null
    }

    val resolvedSubtitle: (@Composable () -> Unit) = subtitleSlot ?: {
        val year = item.releaseDate.take(4).ifEmpty { stringResource(R.string.na) }
        val typeLabel = if (item.mediaType == MediaType.Movie) {
            stringResource(R.string.movie_badge)
        } else {
            stringResource(R.string.tv_badge)
        }
        Text(
            text = "$typeLabel • $year",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val border = if (item.isWatched) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    if (isGridView) {
        val resolvedTrailingAction: (@Composable () -> Unit)? = when {
            trailingActionSlot != null -> trailingActionSlot
            onToggleFavorite != null -> {
                {
                    MediaCardFavoriteButton(
                        isFavorite = item.isFavorite,
                        onClick = onToggleFavorite
                    )
                }
            }

            else -> null
        }

        ShowTimeMediaGridCard(
            title = item.title,
            posterImageUrl = item.posterImageUrl,
            onClick = onClick,
            modifier = modifier,
            topStartBadge = resolvedTopStartBadge,
            topEndAction = resolvedTopEndAction,
            bottomStartBadge = resolvedBottomStartBadge,
            subtitle = resolvedSubtitle,
            trailingAction = resolvedTrailingAction,
            border = border
        )
    } else {
        ShowTimeMediaListCard(
            title = item.title,
            posterImageUrl = item.posterImageUrl,
            onClick = onClick,
            modifier = modifier,
            topStartBadge = resolvedTopStartBadge,
            bottomStartBadge = resolvedBottomStartBadge,
            subtitle = resolvedSubtitle,
            overview = item.overview.ifEmpty { null },
            border = border,
            trailingActions = {
                if (onToggleFavorite != null) {
                    MediaCardFavoriteButton(
                        isFavorite = item.isFavorite,
                        onClick = onToggleFavorite
                    )
                }
                if (topEndSlot != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    topEndSlot()
                } else if (hasOverflowOptions) {
                    Spacer(modifier = Modifier.width(4.dp))
                    RenderOmniAction(isOverPoster = false)
                }
            }
        )
    }
}
