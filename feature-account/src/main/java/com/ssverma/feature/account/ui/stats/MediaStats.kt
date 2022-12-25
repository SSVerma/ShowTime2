package com.ssverma.feature.account.ui.stats

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.core.ui.layout.Popup
import com.ssverma.feature.account.R
import com.ssverma.feature.auth.ui.AuthScreenContainer
import com.ssverma.shared.domain.model.MediaType

@Composable
fun MediaStatsAction(
    mediaType: MediaType,
    mediaId: Int,
    modifier: Modifier = Modifier,
    viewModel: MediaStatsViewModel = hiltViewModel()
) {
    val mediaStatsUiState by viewModel.mediaStats.collectAsState()
    val popupExpansionState = remember { mutableStateOf(false) }

    Popup(
        anchorContent = {
            FloatingActionButton(
                onClick = {
                    popupExpansionState.value = true
                    viewModel.fetchMediaStats(
                        mediaType = mediaType,
                        mediaId = mediaId
                    )
                },
                backgroundColor = MaterialTheme.colors.surface,
                modifier = modifier.size(com.ssverma.shared.ui.component.ActionSize)
            ) {
                Icon(imageVector = AppIcons.Add, contentDescription = null)
            }
        },
        expandState = popupExpansionState,
    ) {
        when (mediaStatsUiState) {
            is MediaStatsUiState.Error -> {
                DropdownMenuItem(onClick = { popupExpansionState.value = false }) {
                    Text(
                        text = stringResource(
                            id = com.ssverma.core.ui.R.string.something_went_wrong
                        )
                    )
                }
            }
            MediaStatsUiState.Unauthorized -> {
                DropdownMenuItem(onClick = {}) {
                    AuthScreenContainer(
                        onBackPressed = { popupExpansionState.value = false }
                    ) {
                        LaunchedEffect(true) {
                            viewModel.fetchMediaStats(mediaType = mediaType, mediaId = mediaId)
                        }
                    }
                }
            }
            MediaStatsUiState.Loading -> {
                DropdownMenuItem(onClick = {}) {
                    ShowTimeLoadingIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(id = com.ssverma.core.ui.R.string.loading))
                }
            }
            is MediaStatsUiState.Success -> {
                val mediaStats = (mediaStatsUiState as MediaStatsUiState.Success).mediaStats
                FavoriteMenuItem(
                    favorite = mediaStats.favorite,
                    onClick = {
                        viewModel.toggleMediaFavoriteStatus(
                            mediaType = mediaType,
                            mediaId = mediaId,
                            isCurrentFavorite = mediaStats.favorite
                        )
                    }
                )

                WatchlistMenuItem(
                    inWatchlist = mediaStats.inWatchlist,
                    onClick = {
                        viewModel.toggleMediaWatchlistStatus(
                            mediaType = mediaType,
                            mediaId = mediaId,
                            isCurrentInWatchlist = mediaStats.inWatchlist
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteMenuItem(
    favorite: Boolean,
    onClick: () -> Unit
) {
    val favoriteTitle = if (favorite) {
        stringResource(id = R.string.remove_from_favorite)
    } else {
        stringResource(id = R.string.add_to_favorite)
    }

    val favoriteIcon = if (favorite) {
        AppIcons.Delete
    } else {
        AppIcons.Add
    }

    MediaStatsItem(title = favoriteTitle, icon = favoriteIcon, onClick = onClick)
}

@Composable
private fun WatchlistMenuItem(
    inWatchlist: Boolean,
    onClick: () -> Unit
) {
    val watchlistTitle = if (inWatchlist) {
        stringResource(id = R.string.remove_from_watchlist)
    } else {
        stringResource(id = R.string.add_to_watchlist)
    }

    val watchlistIcon = if (inWatchlist) {
        AppIcons.Delete
    } else {
        AppIcons.Add
    }

    MediaStatsItem(title = watchlistTitle, icon = watchlistIcon, onClick = onClick)
}

@Composable
private fun MediaStatsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    DropdownMenuItem(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title)
    }
}

