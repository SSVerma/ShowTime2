package com.ssverma.shared.ui.component.media.menu

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.media.MediaCardOverflowAction

@Composable
fun MediaOmniActionMenu(
    mediaId: Int,
    mediaType: MediaType,
    title: String,
    modifier: Modifier = Modifier,
    posterImageUrl: String = "",
    backdropImageUrl: String = "",
    voteAvg: Float = 0f,
    releaseDate: String = "",
    isActionActive: Boolean = false,
    isInWatchlist: Boolean = false,
    isWatched: Boolean = false,
    isFavorite: Boolean = false,
    onToggleWatchlist: (() -> Unit)? = null,
    onToggleWatched: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onLogToDiary: (() -> Unit)? = null,
    onCustomListClick: (() -> Unit)? = null,
    onOpenDiscussions: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onShowFeedback: ((message: String, actionLabel: String?, targetCustomListId: String?) -> Unit)? = null,
    config: MediaOmniMenuConfig = MediaOmniMenuConfig.Default,
    customLists: List<CustomListOption>? = null,
    onToggleCustomList: ((CustomListOption) -> Unit)? = null,
    isOverPoster: Boolean = true,
    viewModel: MediaOmniMenuViewModel = hiltViewModel()
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    val hasTrackingSection = (config.showWatchlist && onToggleWatchlist != null) ||
            (config.showWatched && onToggleWatched != null) ||
            (config.showFavorite && onToggleFavorite != null)

    val hasLoggingSection = (config.showDiaryLog && onLogToDiary != null) || config.showCustomList

    val hasSocialSection = (config.showDiscussions && onOpenDiscussions != null) ||
            (config.showShare && onShare != null)

    if (!hasTrackingSection && !hasLoggingSection && !hasSocialSection) return

    MediaCardOverflowAction(
        expanded = isMenuExpanded,
        onToggleExpand = { isMenuExpanded = !isMenuExpanded },
        onDismissRequest = { isMenuExpanded = false },
        showActiveDot = isActionActive,
        isOverPoster = isOverPoster,
        modifier = modifier
    ) {
        // Group 1: Core Library Tracking (Watchlist, Watched, Favorites)
        if (hasTrackingSection) {
            if (config.showWatchlist && onToggleWatchlist != null) {
                ExpressiveMenuItem(
                    title = if (isInWatchlist) {
                        stringResource(R.string.remove_from_watchlist)
                    } else {
                        stringResource(R.string.add_to_watchlist)
                    },
                    icon = if (isInWatchlist) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isActive = isInWatchlist,
                    onClick = {
                        isMenuExpanded = false
                        onToggleWatchlist()
                    }
                )
            }

            if (config.showWatched && onToggleWatched != null) {
                ExpressiveMenuItem(
                    title = if (isWatched) {
                        stringResource(R.string.media_card_action_mark_unwatched)
                    } else {
                        stringResource(R.string.media_card_action_mark_watched)
                    },
                    icon = if (isWatched) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    isActive = isWatched,
                    onClick = {
                        isMenuExpanded = false
                        onToggleWatched()
                    }
                )
            }

            if (config.showFavorite && onToggleFavorite != null) {
                ExpressiveMenuItem(
                    title = if (isFavorite) {
                        stringResource(R.string.remove_from_favorite)
                    } else {
                        stringResource(R.string.add_to_favorite)
                    },
                    icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    iconTint = MaterialTheme.colorScheme.error,
                    isActive = isFavorite,
                    onClick = {
                        isMenuExpanded = false
                        onToggleFavorite()
                    }
                )
            }
        }

        // Section Divider between Tracking and Logging
        if (hasTrackingSection && (hasLoggingSection || hasSocialSection)) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        }

        // Group 2: Cinephile Logging & Custom Lists
        if (hasLoggingSection) {
            if (config.showDiaryLog && onLogToDiary != null) {
                ExpressiveMenuItem(
                    title = stringResource(R.string.media_card_action_log_diary),
                    icon = Icons.Rounded.EditCalendar,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isActive = false,
                    onClick = {
                        isMenuExpanded = false
                        onLogToDiary()
                    }
                )
            }

            if (config.showCustomList) {
                CustomListsMenuItems(
                    mediaId = mediaId,
                    mediaType = mediaType,
                    title = title,
                    posterImageUrl = posterImageUrl,
                    backdropImageUrl = backdropImageUrl,
                    voteAvg = voteAvg,
                    customListsOverride = customLists,
                    onToggleCustomListOverride = onToggleCustomList,
                    onCustomListClick = onCustomListClick,
                    onDismissMenu = { isMenuExpanded = false },
                    onShowFeedback = onShowFeedback,
                    viewModel = viewModel
                )
            }
        }

        // Section Divider between Logging and Social/Share
        if (hasLoggingSection && hasSocialSection) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        }

        // Group 3: Community Discussions & Social Share
        if (hasSocialSection) {
            if (config.showDiscussions && onOpenDiscussions != null) {
                ExpressiveMenuItem(
                    title = stringResource(R.string.media_menu_discussions),
                    icon = Icons.AutoMirrored.Rounded.Comment,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isActive = false,
                    onClick = {
                        isMenuExpanded = false
                        onOpenDiscussions()
                    }
                )
            }

            if (config.showShare && onShare != null) {
                ExpressiveMenuItem(
                    title = stringResource(R.string.share),
                    icon = Icons.Rounded.Share,
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    isActive = false,
                    onClick = {
                        isMenuExpanded = false
                        onShare()
                    }
                )
            }
        }
    }
}

@Composable
private fun CustomListsMenuItems(
    mediaId: Int,
    mediaType: MediaType,
    title: String,
    posterImageUrl: String,
    backdropImageUrl: String,
    voteAvg: Float,
    customListsOverride: List<CustomListOption>?,
    onToggleCustomListOverride: ((CustomListOption) -> Unit)?,
    onCustomListClick: (() -> Unit)?,
    onDismissMenu: () -> Unit,
    onShowFeedback: ((message: String, actionLabel: String?, targetCustomListId: String?) -> Unit)?,
    viewModel: MediaOmniMenuViewModel
) {
    val context = LocalContext.current
    val viewInLibraryText = stringResource(R.string.media_menu_view_in_library)

    val options: List<CustomListOption> = if (customListsOverride != null) {
        customListsOverride
    } else {
        val allLists by viewModel.customLists.collectAsState(initial = emptyList())
        val mediaListIds by viewModel.getCustomListIdsForMedia(mediaId)
            .collectAsState(initial = emptyList())
        allLists.map { list ->
            CustomListOption(
                listId = list.listId,
                title = list.title,
                isContained = mediaListIds.contains(list.listId)
            )
        }
    }

    if (options.isNotEmpty()) {
        options.forEach { option ->
            ExpressiveMenuItem(
                title = option.title,
                icon = Icons.Rounded.FolderSpecial,
                iconTint = if (option.isContained) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                isActive = option.isContained,
                onClick = {
                    onDismissMenu()
                    if (onToggleCustomListOverride != null) {
                        onToggleCustomListOverride(option)
                    } else {
                        viewModel.toggleMediaCustomList(
                            listId = option.listId,
                            mediaId = mediaId,
                            mediaType = mediaType,
                            title = title,
                            posterImageUrl = posterImageUrl,
                            backdropImageUrl = backdropImageUrl,
                            voteAvg = voteAvg,
                            isCurrentlyInList = option.isContained
                        )
                    }
                    val feedbackMessage = if (option.isContained) {
                        context.getString(R.string.media_menu_removed_from_list, option.title)
                    } else {
                        context.getString(R.string.media_menu_added_to_list, option.title)
                    }
                    onShowFeedback?.invoke(
                        feedbackMessage,
                        if (option.isContained) null else viewInLibraryText,
                        if (option.isContained) null else option.listId
                    )
                }
            )
        }

        if (onCustomListClick != null) {
            ExpressiveMenuItem(
                title = stringResource(R.string.media_menu_new_collection),
                icon = Icons.Rounded.CreateNewFolder,
                iconTint = MaterialTheme.colorScheme.secondary,
                isActive = false,
                onClick = {
                    onDismissMenu()
                    onCustomListClick()
                }
            )
        }
    } else if (onCustomListClick != null) {
        ExpressiveMenuItem(
            title = stringResource(R.string.media_menu_add_to_collection),
            icon = Icons.Rounded.FolderSpecial,
            iconTint = MaterialTheme.colorScheme.secondary,
            isActive = false,
            onClick = {
                onDismissMenu()
                onCustomListClick()
            }
        )
    }
}
