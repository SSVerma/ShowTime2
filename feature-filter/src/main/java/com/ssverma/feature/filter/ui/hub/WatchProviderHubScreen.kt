package com.ssverma.feature.filter.ui.hub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.StatefulContent
import com.ssverma.feature.filter.ui.hub.component.WatchProviderHubContent
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.tv.TvShowPreview

@Composable
fun WatchProviderHubScreen(
    onMovieClick: (Int) -> Unit,
    onTvShowClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onGenreClick: (Genre, Boolean) -> Unit,
    onMovieSeeAllClick: (providerInfo: ProviderInfo, discoverConfig: MovieDiscoverConfig) -> Unit,
    onTvSeeAllClick: (providerInfo: ProviderInfo, discoverConfig: TvDiscoverConfig) -> Unit,
    viewModel: WatchProviderHubViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatefulContent(
        state = uiState.hubContentState,
        onRetry = { viewModel.fetchHubContent() },
        loading = {
            WatchProviderHubContent(
                provider = uiState.provider,
                heroItems = emptyList(),
                newItems = emptyList(),
                todayItems = emptyList(),
                topRatedItems = emptyList(),
                genres = emptyList(),
                isMovieMode = uiState.isMovieMode,
                onMediaClick = {},
                onGenreClick = {},
                onBackClick = onBackClick,
                onMovieSeeAllClick = {},
                onTvSeeAllClick = {},
                isLoading = true
            )
        }
    ) { hubContent ->
        WatchProviderHubContent(
            provider = uiState.provider,
            heroItems = hubContent.heroItems,
            newItems = hubContent.newItems,
            todayItems = hubContent.upcomingItems,
            topRatedItems = hubContent.topRatedItems,
            genres = hubContent.genres,
            isMovieMode = uiState.isMovieMode,
            onMediaClick = { media ->
                when (media) {
                    is MoviePreview -> onMovieClick(media.id)
                    is TvShowPreview -> onTvShowClick(media.id)
                }
            },
            onGenreClick = { genre -> onGenreClick(genre, uiState.isMovieMode) },
            onBackClick = onBackClick,
            onMovieSeeAllClick = { discoverConfig ->
                onMovieSeeAllClick(uiState.provider, discoverConfig)
            },
            onTvSeeAllClick = { discoverConfig ->
                onTvSeeAllClick(uiState.provider, discoverConfig)
            }
        )
    }
}
