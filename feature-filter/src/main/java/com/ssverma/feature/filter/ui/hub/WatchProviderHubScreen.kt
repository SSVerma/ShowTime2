package com.ssverma.feature.filter.ui.hub

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.DefaultCoreErrorIndicator
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.feature.filter.ui.hub.component.WatchProviderHubContent
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import kotlinx.coroutines.launch

@Composable
fun WatchProviderHubScreen(
    onMovieClick: (Int) -> Unit,
    onTvShowClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onGenreClick: (Genre, Boolean) -> Unit,
    onMovieSeeAllClick: (providerInfo: ProviderInfo, discoverConfig: MovieDiscoverConfig) -> Unit,
    onTvSeeAllClick: (providerInfo: ProviderInfo, discoverConfig: TvDiscoverConfig) -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit,
    viewModel: WatchProviderHubViewModel,
    source: String = "default"
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            ShowTimeSnackbarHost(
                hostState = snackbarHostState,
                floatingBottomBar = false
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val hubContentState = uiState.hubContentState
            val currentContent = (hubContentState as? UiState.Success)?.data
            val isLoading = hubContentState is UiState.Loading

            if (hubContentState is UiState.Error && currentContent == null) {
                DefaultCoreErrorIndicator(
                    failure = hubContentState.failure as Failure.CoreFailure,
                    onRetry = { viewModel.fetchHubContent() }
                )
            } else {
                WatchProviderHubContent(
                    provider = uiState.provider,
                    heroItems = currentContent?.heroItems.orEmpty(),
                    newItems = currentContent?.newItems.orEmpty(),
                    upcomingItems = currentContent?.upcomingItems.orEmpty(),
                    topRatedItems = currentContent?.topRatedItems.orEmpty(),
                    genres = currentContent?.genres.orEmpty(),
                    isMovieMode = uiState.isMovieMode,
                    onToggleMode = viewModel::toggleMode,
                    onMovieClick = { movie -> onMovieClick(movie.id) },
                    onTvShowClick = { tvShow -> onTvShowClick(tvShow.id) },
                    onGenreClick = { genre -> onGenreClick(genre, uiState.isMovieMode) },
                    onBackClick = onBackClick,
                    onMovieSeeAllClick = { discoverConfig ->
                        onMovieSeeAllClick(uiState.provider, discoverConfig)
                    },
                    onTvSeeAllClick = { discoverConfig ->
                        onTvSeeAllClick(uiState.provider, discoverConfig)
                    },
                    onAdLoaded = viewModel::onCarouselNativeAdLoaded,
                    source = source,
                    onShowFeedback = { message, actionLabel, destination ->
                        coroutineScope.launch {
                            val result = snackbarHostState.showImmediateSnackbar(
                                message = message,
                                actionLabel = actionLabel,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                openLibraryPage(destination ?: LibraryHomeNavKey.Default)
                            }
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}
