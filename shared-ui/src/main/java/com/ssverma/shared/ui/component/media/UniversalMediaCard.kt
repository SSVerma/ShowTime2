package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.media.menu.MediaOmniActionMenu
import com.ssverma.shared.ui.component.media.menu.MediaOmniMenuConfig
import com.ssverma.shared.ui.component.media.menu.MediaOmniMenuViewModel

@Composable
fun UniversalMediaCard(
    item: UniversalMediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGridView: Boolean = true,
    showMediaType: Boolean = false,
    isFavorite: Boolean? = null,
    isInWatchlist: Boolean? = null,
    isWatched: Boolean? = null,
    isActionActive: Boolean? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onToggleWatchlist: (() -> Unit)? = null,
    onToggleWatched: (() -> Unit)? = null,
    onLogToDiary: (() -> Unit)? = null,
    onCustomListClick: (() -> Unit)? = null,
    onOpenDiscussions: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null,
    menuConfig: MediaOmniMenuConfig = MediaOmniMenuConfig.Default,
    topStartSlot: (@Composable () -> Unit)? = null,
    topEndSlot: (@Composable () -> Unit)? = null,
    bottomStartSlot: (@Composable () -> Unit)? = null,
    subtitleSlot: (@Composable () -> Unit)? = null,
    trailingActionSlot: (@Composable () -> Unit)? = null,
    viewModel: MediaOmniMenuViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val viewInLibraryText = stringResource(R.string.media_menu_view_in_library)
    val mediaTypeStr = if (item.mediaType == MediaType.Movie) "movie" else "tv"

    val effectiveInWatchlist = isInWatchlist
        ?: if (item.isInWatchlist) true else viewModel.isInWatchlist(item.id)
            .collectAsState(initial = false).value

    val effectiveIsWatched = isWatched
        ?: if (item.isWatched) true else viewModel.isWatched(item.id)
            .collectAsState(initial = false).value

    val effectiveIsFavorite = isFavorite
        ?: if (item.isFavorite) true else viewModel.isFavorite(item.id)
            .collectAsState(initial = false).value

    val effectiveActionActive = isActionActive
        ?: (if (item.isInWatchlist || item.isWatched || item.isFavorite) true else viewModel.isMediaActionActive(
            item.id
        ).collectAsState(initial = false).value)

    val resolvedToggleFavorite: () -> Unit = {
        if (onToggleFavorite != null) {
            onToggleFavorite()
        } else {
            val wasFavorite = effectiveIsFavorite
            viewModel.toggleFavorite(
                mediaId = item.id,
                mediaType = item.mediaType,
                title = item.title,
                posterImageUrl = item.posterImageUrl,
                backdropImageUrl = item.backdropImageUrl,
                voteAvg = item.voteAvg,
                releaseDate = item.releaseDate
            )
            val feedbackMsg = if (wasFavorite) {
                context.getString(R.string.media_menu_removed_from_favorites)
            } else {
                context.getString(R.string.media_menu_added_to_favorites)
            }
            val destination = if (wasFavorite) null else LibraryHomeNavKey(
                initialTab = LibraryTabDestination.Favorites,
                initialMediaType = mediaTypeStr
            )
            onShowFeedback?.invoke(
                feedbackMsg,
                if (wasFavorite) null else viewInLibraryText,
                destination
            )
        }
    }

    val resolvedToggleWatched: () -> Unit = {
        if (onToggleWatched != null) {
            onToggleWatched()
        } else {
            val wasWatched = effectiveIsWatched
            viewModel.toggleWatched(
                mediaId = item.id,
                mediaType = item.mediaType,
                title = item.title,
                posterImageUrl = item.posterImageUrl,
                voteAvg = item.voteAvg
            )
            val feedbackMsg = if (wasWatched) {
                context.getString(R.string.media_menu_removed_from_watched)
            } else {
                context.getString(R.string.media_menu_marked_as_watched)
            }
            val destination = if (wasWatched) null else LibraryHomeNavKey(
                initialTab = LibraryTabDestination.History,
                initialMediaType = mediaTypeStr
            )
            onShowFeedback?.invoke(
                feedbackMsg,
                if (wasWatched) null else viewInLibraryText,
                destination
            )
        }
    }

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
            isActionActive = effectiveActionActive,
            isInWatchlist = effectiveInWatchlist,
            isWatched = effectiveIsWatched,
            isFavorite = effectiveIsFavorite,
            onToggleWatchlist = onToggleWatchlist,
            onToggleWatched = onToggleWatched,
            onToggleFavorite = onToggleFavorite,
            onLogToDiary = onLogToDiary,
            onCustomListClick = onCustomListClick,
            onOpenDiscussions = onOpenDiscussions,
            onShare = onShare,
            onShowFeedback = onShowFeedback,
            config = menuConfig,
            isOverPoster = isOverPoster,
            viewModel = viewModel
        )
    }

    val resolvedTopEndAction: (@Composable () -> Unit)? = when {
        topEndSlot != null -> topEndSlot
        else -> {
            { RenderOmniAction(isOverPoster = true) }
        }
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
        effectiveIsWatched -> {
            { MediaCardWatchedBadge(onClick = resolvedToggleWatched) }
        }

        else -> null
    }

    val resolvedSubtitle: (@Composable () -> Unit) = subtitleSlot ?: {
        val yearText = item.displayYear.ifEmpty { item.releaseDate }
        val text = if (showMediaType) {
            val typeLabel = if (item.mediaType == MediaType.Movie) {
                stringResource(R.string.movie_badge)
            } else {
                stringResource(R.string.tv_badge)
            }
            if (yearText.isNotEmpty()) "$typeLabel • $yearText" else typeLabel
        } else {
            yearText.ifEmpty { stringResource(R.string.na) }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val border = if (effectiveIsWatched) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    if (isGridView) {
        val resolvedTrailingAction: (@Composable () -> Unit)? = when {
            trailingActionSlot != null -> trailingActionSlot
            menuConfig.showFavorite -> {
                {
                    MediaCardFavoriteButton(
                        isFavorite = effectiveIsFavorite,
                        onClick = resolvedToggleFavorite
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
                if (menuConfig.showFavorite) {
                    MediaCardFavoriteButton(
                        isFavorite = effectiveIsFavorite,
                        onClick = resolvedToggleFavorite
                    )
                }
                if (topEndSlot != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    topEndSlot()
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                    RenderOmniAction(isOverPoster = false)
                }
            }
        )
    }
}
