package com.ssverma.feature.account.ui.stats

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.Popup
import com.ssverma.feature.account.R
import com.ssverma.feature.auth.ui.auth.AuthScreenContainer
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
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = modifier.size(com.ssverma.shared.ui.component.ActionSize)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        },
        expandState = popupExpansionState,
    ) {
        when (mediaStatsUiState) {
            is MediaStatsUiState.Error -> {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(
                                id = com.ssverma.core.ui.R.string.something_went_wrong
                            )
                        )
                    },
                    onClick = { popupExpansionState.value = false }
                )
            }
            MediaStatsUiState.Unauthorized -> {
                DropdownMenuItem(
                    text = {
                        AuthScreenContainer(
                            onBackPressed = { popupExpansionState.value = false }
                        ) {
                            LaunchedEffect(Unit) {
                                viewModel.fetchMediaStats(mediaType = mediaType, mediaId = mediaId)
                            }
                        }
                    },
                    onClick = {}
                )
            }
            MediaStatsUiState.Loading -> {
                DropdownMenuItem(
                    text = {
                        ShowTimeLoadingIndicator()
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(id = com.ssverma.core.ui.R.string.loading))
                    },
                    onClick = {}
                )
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
        Icons.Default.Delete
    } else {
        Icons.Default.Add
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
        Icons.Default.Delete
    } else {
        Icons.Default.Add
    }

    MediaStatsItem(title = watchlistTitle, icon = watchlistIcon, onClick = onClick)
}

@Composable
private fun MediaStatsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text = title) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        onClick = onClick
    )
}
