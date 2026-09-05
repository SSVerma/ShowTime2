package com.ssverma.shared.ui.component.media.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssverma.core.navigation.dispatcher.IntentDispatcher
import com.ssverma.core.navigation.nav3.LocalNavigator
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.feature.library.navigation.StandaloneLibraryNavKey
import com.ssverma.feature.movie.navigation.MovieDiscussionsNavKey
import com.ssverma.feature.tv.navigation.TvShowDiscussionsNavKey
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.diary.LogAndRateDialog
import com.ssverma.shared.ui.component.media.MediaCardOverflowAction

private data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

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
    isActionActive: Boolean? = null,
    isInWatchlist: Boolean? = null,
    isWatched: Boolean? = null,
    isFavorite: Boolean? = null,
    onToggleWatchlist: (() -> Unit)? = null,
    onToggleWatched: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onLogToDiary: (() -> Unit)? = null,
    onCustomListClick: (() -> Unit)? = null,
    onOpenDiscussions: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null,
    config: MediaOmniMenuConfig = MediaOmniMenuConfig.Default,
    customLists: List<CustomListOption>? = null,
    onToggleCustomList: ((CustomListOption) -> Unit)? = null,
    isOverPoster: Boolean = true,
    viewModel: MediaOmniMenuViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }

    val effectiveInWatchlist = isInWatchlist
        ?: viewModel.isInWatchlist(mediaId).collectAsState(initial = false).value
    val effectiveIsWatched = isWatched
        ?: viewModel.isWatched(mediaId).collectAsState(initial = false).value
    val effectiveIsFavorite = isFavorite
        ?: viewModel.isFavorite(mediaId).collectAsState(initial = false).value
    val effectiveActionActive = isActionActive
        ?: viewModel.isMediaActionActive(mediaId).collectAsState(initial = false).value

    val canOpenDiscussions =
        config.showDiscussions && (onOpenDiscussions != null || navigator != null)
    val canLogToDiary = config.showDiaryLog
    val canShare = config.showShare

    val hasTrackingSection = config.showWatchlist || config.showWatched || config.showFavorite
    val hasCustomListSection = config.showCustomList
    val hasQuickActionsSection = canOpenDiscussions || canLogToDiary || canShare

    if (!hasTrackingSection && !hasCustomListSection && !hasQuickActionsSection) return

    val viewInLibraryText = stringResource(R.string.media_menu_view_in_library)
    val mediaTypeStr = if (mediaType == MediaType.Movie) "movie" else "tv"

    val quickActionTint = MaterialTheme.colorScheme.primary
    val quickActionContainer = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    val quickActions = buildList {
        if (canOpenDiscussions) {
            add(
                QuickActionItem(
                    label = stringResource(R.string.media_menu_quick_discuss),
                    icon = Icons.AutoMirrored.Rounded.Comment,
                    onClick = {
                        isMenuExpanded = false
                        if (onOpenDiscussions != null) {
                            onOpenDiscussions()
                        } else if (navigator != null) {
                            if (mediaType == MediaType.Movie) {
                                navigator.navigate(
                                    MovieDiscussionsNavKey(
                                        movieId = mediaId,
                                        movieTitle = title,
                                        posterImageUrl = posterImageUrl,
                                        backdropImageUrl = backdropImageUrl
                                    )
                                )
                            } else {
                                navigator.navigate(
                                    TvShowDiscussionsNavKey(
                                        tvShowId = mediaId,
                                        tvShowTitle = title,
                                        posterImageUrl = posterImageUrl,
                                        backdropImageUrl = backdropImageUrl
                                    )
                                )
                            }
                        }
                    }
                )
            )
        }

        if (canLogToDiary) {
            add(
                QuickActionItem(
                    label = stringResource(R.string.media_menu_quick_diary),
                    icon = Icons.Rounded.EditCalendar,
                    onClick = {
                        isMenuExpanded = false
                        if (onLogToDiary != null) {
                            onLogToDiary()
                        } else {
                            showLogDialog = true
                        }
                    }
                )
            )
        }

        if (canShare) {
            add(
                QuickActionItem(
                    label = stringResource(R.string.share),
                    icon = Icons.Rounded.Share,
                    onClick = {
                        isMenuExpanded = false
                        if (onShare != null) {
                            onShare()
                        } else {
                            val tmdbType = if (mediaType == MediaType.Movie) "movie" else "tv"
                            with(IntentDispatcher) {
                                context.dispatchShareTextIntent(
                                    "$title\nhttps://www.themoviedb.org/$tmdbType/$mediaId"
                                )
                            }
                        }
                    }
                )
            )
        }
    }

    MediaCardOverflowAction(
        expanded = isMenuExpanded,
        onToggleExpand = { isMenuExpanded = !isMenuExpanded },
        onDismissRequest = { isMenuExpanded = false },
        showActiveDot = effectiveActionActive,
        isOverPoster = isOverPoster,
        modifier = modifier
    ) {
        // Group 1: Static Actions Bar (- - -)
        if (quickActions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickActions.forEach { action ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = action.onClick)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = quickActionContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.label,
                                    tint = quickActionTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        }

        // Group 2: Core Library Tracking (Watchlist, Watched, Favorites)
        if (hasTrackingSection) {
            if (config.showWatchlist) {
                ExpressiveMenuItem(
                    title = if (effectiveInWatchlist) {
                        stringResource(R.string.remove_from_watchlist)
                    } else {
                        stringResource(R.string.add_to_watchlist)
                    },
                    icon = if (effectiveInWatchlist) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isActive = effectiveInWatchlist,
                    onClick = {
                        isMenuExpanded = false
                        if (onToggleWatchlist != null) {
                            onToggleWatchlist()
                        } else {
                            val wasInWatchlist = effectiveInWatchlist
                            viewModel.toggleWatchlist(
                                mediaId = mediaId,
                                mediaType = mediaType,
                                title = title,
                                posterImageUrl = posterImageUrl,
                                backdropImageUrl = backdropImageUrl,
                                voteAvg = voteAvg,
                                releaseDate = releaseDate
                            )
                            val feedbackMsg = if (wasInWatchlist) {
                                context.getString(R.string.media_menu_removed_from_watchlist)
                            } else {
                                context.getString(R.string.media_menu_added_to_watchlist)
                            }
                            val destination = if (wasInWatchlist) null else LibraryHomeNavKey(
                                initialTab = LibraryTabDestination.Watchlist,
                                initialMediaType = mediaTypeStr
                            )
                            onShowFeedback?.invoke(
                                feedbackMsg,
                                if (wasInWatchlist) null else viewInLibraryText,
                                destination
                            )
                        }
                    }
                )
            }

            if (config.showWatched) {
                ExpressiveMenuItem(
                    title = if (effectiveIsWatched) {
                        stringResource(R.string.media_card_action_mark_unwatched)
                    } else {
                        stringResource(R.string.media_card_action_mark_watched)
                    },
                    icon = if (effectiveIsWatched) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    isActive = effectiveIsWatched,
                    onClick = {
                        isMenuExpanded = false
                        if (onToggleWatched != null) {
                            onToggleWatched()
                        } else {
                            val wasWatched = effectiveIsWatched
                            viewModel.toggleWatched(
                                mediaId = mediaId,
                                mediaType = mediaType,
                                title = title,
                                posterImageUrl = posterImageUrl,
                                voteAvg = voteAvg
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
                )
            }

            if (config.showFavorite) {
                ExpressiveMenuItem(
                    title = if (effectiveIsFavorite) {
                        stringResource(R.string.remove_from_favorite)
                    } else {
                        stringResource(R.string.add_to_favorite)
                    },
                    icon = if (effectiveIsFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    iconTint = MaterialTheme.colorScheme.error,
                    isActive = effectiveIsFavorite,
                    onClick = {
                        isMenuExpanded = false
                        if (onToggleFavorite != null) {
                            onToggleFavorite()
                        } else {
                            val wasFavorite = effectiveIsFavorite
                            viewModel.toggleFavorite(
                                mediaId = mediaId,
                                mediaType = mediaType,
                                title = title,
                                posterImageUrl = posterImageUrl,
                                backdropImageUrl = backdropImageUrl,
                                voteAvg = voteAvg,
                                releaseDate = releaseDate
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
                )
            }
        }

        // Section Divider between Tracking and Custom Collections
        if (hasTrackingSection && hasCustomListSection) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            )
        }

        // Group 3: Cinephile Custom Lists / Collections
        if (hasCustomListSection) {
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

    if (showLogDialog) {
        LogAndRateDialog(
            mediaId = mediaId,
            mediaType = mediaType,
            title = title,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl,
            releaseDate = releaseDate,
            tmdbRating = voteAvg,
            onDismiss = { showLogDialog = false },
            onSave = { entry ->
                showLogDialog = false
                viewModel.saveDiaryEntry(entry)
                val feedbackMsg = context.getString(R.string.media_menu_diary_logged_success, title)
                val viewInDiaryText = context.getString(R.string.media_menu_view_in_diary)
                val destination = LibraryHomeNavKey(initialTab = LibraryTabDestination.History)
                onShowFeedback?.invoke(feedbackMsg, viewInDiaryText, destination)
            }
        )
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
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)?,
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

    val navigator = LocalNavigator.current
    val canHandleNewCollection = onCustomListClick != null || navigator != null
    val mediaTypeStr = if (mediaType == MediaType.Movie) "movie" else "tv"

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
                    val destination = if (option.isContained) null else LibraryHomeNavKey(
                        initialTab = LibraryTabDestination.CustomLists,
                        targetCustomListId = option.listId
                    )
                    onShowFeedback?.invoke(
                        feedbackMessage,
                        if (option.isContained) null else viewInLibraryText,
                        destination
                    )
                }
            )
        }

        val targetNavKey = StandaloneLibraryNavKey(
            initialTab = LibraryTabDestination.CustomLists,
            initialMediaType = mediaTypeStr,
            openCreateCustomList = true,
            attachMediaId = mediaId,
            attachMediaType = mediaTypeStr,
            attachMediaTitle = title,
            attachMediaPosterUrl = posterImageUrl
        )

        if (canHandleNewCollection) {
            ExpressiveMenuItem(
                title = stringResource(R.string.media_menu_new_collection),
                icon = Icons.Rounded.CreateNewFolder,
                iconTint = MaterialTheme.colorScheme.secondary,
                isActive = false,
                onClick = {
                    onDismissMenu()
                    if (onCustomListClick != null) {
                        onCustomListClick()
                    } else if (navigator != null) {
                        navigator.navigate(targetNavKey)
                    }
                }
            )
        }
    } else if (canHandleNewCollection) {
        val targetNavKey = StandaloneLibraryNavKey(
            initialTab = LibraryTabDestination.CustomLists,
            initialMediaType = mediaTypeStr,
            openCreateCustomList = true,
            attachMediaId = mediaId,
            attachMediaType = mediaTypeStr,
            attachMediaTitle = title,
            attachMediaPosterUrl = posterImageUrl
        )
        ExpressiveMenuItem(
            title = stringResource(R.string.media_menu_add_to_collection),
            icon = Icons.Rounded.FolderSpecial,
            iconTint = MaterialTheme.colorScheme.secondary,
            isActive = false,
            onClick = {
                onDismissMenu()
                if (onCustomListClick != null) {
                    onCustomListClick()
                } else if (navigator != null) {
                    navigator.navigate(targetNavKey)
                }
            }
        )
    }
}
