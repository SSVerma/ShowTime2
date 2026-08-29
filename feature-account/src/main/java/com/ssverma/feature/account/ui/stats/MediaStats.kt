package com.ssverma.feature.account.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.Popup
import com.ssverma.feature.account.R
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.LibraryTabDestination
import com.ssverma.shared.domain.model.MediaType
import kotlinx.coroutines.launch
import com.ssverma.core.ui.R as CoreUiR

@Composable
fun MediaStatsAction(
    mediaType: MediaType,
    mediaId: Int,
    title: String = "",
    posterImageUrl: String = "",
    backdropImageUrl: String = "",
    voteAvg: Float = 0f,
    releaseDate: String = "",
    modifier: Modifier = Modifier,
    triggerIcon: ImageVector = Icons.Rounded.FavoriteBorder,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onShowFeedback: ((message: String, actionLabel: String?, destination: LibraryHomeNavKey?) -> Unit)? = null,
    onClick: () -> Unit = {},
    viewModel: MediaStatsViewModel = hiltViewModel()
) {
    val mediaStatsUiState by viewModel.mediaStats.collectAsState()
    val isActionActive by remember(mediaId) {
        viewModel.isMediaActionActiveFlow(mediaId)
    }.collectAsState(initial = false)
    val popupExpansionState = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val iconScale = remember { Animatable(1f) }
    var burstTrigger by remember { mutableIntStateOf(0) }

    Popup(
        anchorContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
            ) {
                Surface(
                    onClick = {
                        onClick()
                        coroutineScope.launch {
                            iconScale.animateTo(0.92f, animationSpec = tween(50))
                            iconScale.animateTo(1f, animationSpec = tween(120))
                        }
                        popupExpansionState.value = true
                        viewModel.fetchMediaStats(
                            mediaType = mediaType,
                            mediaId = mediaId
                        )
                    },
                    shape = CircleShape,
                    color = containerColor,
                    tonalElevation = 2.dp,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = iconScale.value
                            scaleY = iconScale.value
                        }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = triggerIcon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Active Indicator Dot when any library action is taken
                androidx.compose.animation.AnimatedVisibility(
                    visible = isActionActive,
                    enter = androidx.compose.animation.scaleIn(
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 0.7f,
                            stiffness = 400f
                        )
                    ) + androidx.compose.animation.fadeIn(animationSpec = tween(150)),
                    exit = androidx.compose.animation.scaleOut(
                        animationSpec = tween(150)
                    ) + androidx.compose.animation.fadeOut(animationSpec = tween(150)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 1.dp, end = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.2.dp,
                                color = containerColor,
                                shape = CircleShape
                            )
                    )
                }

                // Floating minor hearts rising gracefully in the air above card boundaries
                FloatingHeartsBurst(trigger = burstTrigger)
            }
        },
        expandState = popupExpansionState,
        properties = PopupProperties(
            focusable = true,
            clippingEnabled = false
        )
    ) {
        when (mediaStatsUiState) {
            is MediaStatsUiState.Error -> {
                DropdownMenuItem(
                    modifier = Modifier.widthIn(min = 220.dp),
                    text = {
                        Text(
                            text = stringResource(
                                id = CoreUiR.string.something_went_wrong
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = { popupExpansionState.value = false }
                )
            }

            MediaStatsUiState.Unauthorized,
            MediaStatsUiState.Loading -> {
                DropdownMenuItem(
                    modifier = Modifier.widthIn(min = 220.dp),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            ShowTimeLoadingIndicator()
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(id = CoreUiR.string.loading),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    onClick = {}
                )
            }

            is MediaStatsUiState.Success -> {
                val mediaStats = (mediaStatsUiState as MediaStatsUiState.Success).mediaStats

                val mediaTypeStr = when (mediaType) {
                    MediaType.Movie -> "movie"
                    MediaType.Tv -> "tv"
                    else -> null
                }

                val removedFromFavoritesText = stringResource(R.string.removed_from_favorites)
                val addedToFavoritesText = stringResource(R.string.added_to_favorites)
                val removedFromWatchlistText = stringResource(R.string.removed_from_watchlist)
                val addedToWatchlistText = stringResource(R.string.added_to_watchlist)
                val removedFromWatchedText = stringResource(R.string.removed_from_watched)
                val markedAsWatchedText = stringResource(R.string.marked_as_watched)
                val viewInLibraryText = stringResource(R.string.view_in_library)

                // Favorite Action Item
                ExpressiveMenuItem(
                    title = if (mediaStats.favorite) stringResource(R.string.remove_from_favorite) else stringResource(
                        R.string.add_to_favorite
                    ),
                    icon = if (mediaStats.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    iconTint = if (mediaStats.favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    isActive = mediaStats.favorite,
                    onClick = {
                        popupExpansionState.value = false
                        val wasFavorite = mediaStats.favorite
                        if (wasFavorite) {
                            onShowFeedback?.invoke(
                                removedFromFavoritesText,
                                null,
                                null
                            )
                        } else {
                            burstTrigger++
                            onShowFeedback?.invoke(
                                addedToFavoritesText,
                                viewInLibraryText,
                                LibraryHomeNavKey(
                                    initialTab = LibraryTabDestination.Favorites,
                                    initialMediaType = mediaTypeStr
                                )
                            )
                        }
                        viewModel.toggleMediaFavoriteStatus(
                            mediaType = mediaType,
                            mediaId = mediaId,
                            title = title,
                            posterImageUrl = posterImageUrl,
                            backdropImageUrl = backdropImageUrl,
                            voteAvg = voteAvg,
                            releaseDate = releaseDate
                        )
                    }
                )

                // Watchlist Action Item
                ExpressiveMenuItem(
                    title = if (mediaStats.inWatchlist) stringResource(R.string.remove_from_watchlist) else stringResource(
                        R.string.add_to_watchlist
                    ),
                    icon = if (mediaStats.inWatchlist) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    iconTint = if (mediaStats.inWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    isActive = mediaStats.inWatchlist,
                    onClick = {
                        popupExpansionState.value = false
                        val wasInWatchlist = mediaStats.inWatchlist
                        if (wasInWatchlist) {
                            onShowFeedback?.invoke(
                                removedFromWatchlistText,
                                null,
                                null
                            )
                        } else {
                            burstTrigger++
                            onShowFeedback?.invoke(
                                addedToWatchlistText,
                                viewInLibraryText,
                                LibraryHomeNavKey(
                                    initialTab = LibraryTabDestination.Watchlist,
                                    initialMediaType = mediaTypeStr
                                )
                            )
                        }
                        viewModel.toggleMediaWatchlistStatus(
                            mediaType = mediaType,
                            mediaId = mediaId,
                            title = title,
                            posterImageUrl = posterImageUrl,
                            backdropImageUrl = backdropImageUrl,
                            voteAvg = voteAvg,
                            releaseDate = releaseDate
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                // Mark as Watched Item
                ExpressiveMenuItem(
                    title = if (mediaStats.isWatched) stringResource(R.string.remove_from_watched) else stringResource(
                        R.string.mark_as_watched
                    ),
                    icon = Icons.Rounded.Visibility,
                    iconTint = if (mediaStats.isWatched) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                    isActive = mediaStats.isWatched,
                    onClick = {
                        popupExpansionState.value = false
                        val wasWatched = mediaStats.isWatched
                        if (wasWatched) {
                            onShowFeedback?.invoke(
                                removedFromWatchedText,
                                null,
                                null
                            )
                        } else {
                            burstTrigger++
                            onShowFeedback?.invoke(
                                markedAsWatchedText,
                                viewInLibraryText,
                                LibraryHomeNavKey(
                                    initialTab = LibraryTabDestination.History,
                                    initialMediaType = mediaTypeStr
                                )
                            )
                        }
                        viewModel.toggleWatchHistoryStatus(
                            mediaType = mediaType,
                            mediaId = mediaId,
                            title = title,
                            posterImageUrl = posterImageUrl,
                            voteAvg = voteAvg
                        )
                    }
                )

                val customLists by viewModel.customLists.collectAsState(initial = emptyList())
                val mediaCustomListIds by viewModel.getCustomListIdsForMedia(mediaId)
                    .collectAsState(initial = emptyList())

                if (customLists.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    customLists.forEach { customList ->
                        val isContained = mediaCustomListIds.contains(customList.listId)
                        ExpressiveMenuItem(
                            title = customList.title,
                            icon = Icons.Rounded.FolderSpecial,
                            iconTint = if (isContained) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            isActive = isContained,
                            onClick = {
                                viewModel.toggleMediaCustomList(
                                    listId = customList.listId,
                                    mediaId = mediaId,
                                    mediaType = mediaType,
                                    title = title,
                                    posterImageUrl = posterImageUrl,
                                    backdropImageUrl = backdropImageUrl,
                                    voteAvg = voteAvg,
                                    isCurrentlyInList = isContained
                                )
                                onShowFeedback?.invoke(
                                    if (isContained) "Removed from ${customList.title}" else "Added to ${customList.title}",
                                    if (isContained) null else viewInLibraryText,
                                    if (isContained) null else LibraryHomeNavKey(
                                        initialTab = LibraryTabDestination.CustomLists,
                                        targetCustomListId = customList.listId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpressiveMenuItem(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        leadingIcon = {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        trailingIcon = if (isActive) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FloatingHeartsBurst(
    trigger: Int,
    modifier: Modifier = Modifier
) {
    if (trigger == 0) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error

    val particles = remember(trigger, primaryColor, secondaryColor, tertiaryColor, errorColor) {
        listOf(
            FloatingHeartData(
                offsetX = -6f,
                driftX = -28f,
                targetY = -120f,
                size = 15.dp,
                color = primaryColor,
                delay = 0,
                rotation = -14f
            ),
            FloatingHeartData(
                offsetX = 6f,
                driftX = 24f,
                targetY = -160f,
                size = 20.dp,
                color = tertiaryColor,
                delay = 70,
                rotation = 16f
            ),
            FloatingHeartData(
                offsetX = -2f,
                driftX = -12f,
                targetY = -140f,
                size = 13.dp,
                color = secondaryColor,
                delay = 140,
                rotation = -8f
            ),
            FloatingHeartData(
                offsetX = 10f,
                driftX = 35f,
                targetY = -110f,
                size = 16.dp,
                color = errorColor,
                delay = 210,
                rotation = 20f
            ),
            FloatingHeartData(
                offsetX = -10f,
                driftX = -36f,
                targetY = -175f,
                size = 14.dp,
                color = primaryColor.copy(alpha = 0.85f),
                delay = 280,
                rotation = -16f
            ),
            FloatingHeartData(
                offsetX = 0f,
                driftX = 12f,
                targetY = -190f,
                size = 22.dp,
                color = tertiaryColor.copy(alpha = 0.85f),
                delay = 100,
                rotation = 10f
            ),
            FloatingHeartData(
                offsetX = -4f,
                driftX = -20f,
                targetY = -150f,
                size = 18.dp,
                color = secondaryColor.copy(alpha = 0.85f),
                delay = 350,
                rotation = -10f
            ),
            FloatingHeartData(
                offsetX = 8f,
                driftX = 18f,
                targetY = -180f,
                size = 16.dp,
                color = errorColor.copy(alpha = 0.85f),
                delay = 420,
                rotation = 12f
            )
        )
    }

    val animProgress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    if (animProgress.value < 1f) {
        androidx.compose.ui.window.Popup(
            alignment = Alignment.BottomCenter,
            properties = androidx.compose.ui.window.PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                clippingEnabled = false
            )
        ) {
            Box(
                modifier = Modifier.size(width = 160.dp, height = 240.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                particles.forEach { particle ->
                    val totalDuration = 1500f
                    val adjustedProgress =
                        ((animProgress.value * totalDuration - particle.delay) / (totalDuration - particle.delay)).coerceIn(
                            0f,
                            1f
                        )
                    val alpha = if (adjustedProgress < 0.15f) {
                        adjustedProgress / 0.15f
                    } else if (adjustedProgress < 0.65f) {
                        1f
                    } else {
                        (1f - (adjustedProgress - 0.65f) / 0.35f).coerceIn(0f, 1f)
                    }
                    val scale = if (adjustedProgress < 0.2f) {
                        (adjustedProgress / 0.2f) * 1.15f
                    } else {
                        1.15f - (adjustedProgress - 0.2f) * 0.35f
                    }
                    val currentX =
                        particle.offsetX + (particle.driftX - particle.offsetX) * adjustedProgress
                    val currentY = particle.targetY * adjustedProgress

                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = particle.color,
                        modifier = Modifier
                            .size(particle.size)
                            .graphicsLayer {
                                translationX = currentX.dp.toPx()
                                translationY = currentY.dp.toPx()
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                rotationZ = particle.rotation
                            }
                    )
                }
            }
        }
    }
}

private data class FloatingHeartData(
    val offsetX: Float,
    val driftX: Float,
    val targetY: Float,
    val size: Dp,
    val color: Color,
    val delay: Int,
    val rotation: Float
)
