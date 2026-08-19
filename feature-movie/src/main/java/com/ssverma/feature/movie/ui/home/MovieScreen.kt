package com.ssverma.feature.movie.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.analytics.MovieAnalyticsScreenName
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.ui.home.content.MovieHomeContent
import com.ssverma.shared.domain.model.ProviderInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit,
    viewModel: HomeMovieViewModel = hiltViewModel()
) {
    TrackScreenView(screenName = MovieAnalyticsScreenName.MOVIE_HOME)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        AppPage(
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            snackbarHost = {
                ShowTimeSnackbarHost(
                    hostState = snackbarHostState,
                    floatingBottomBar = true
                )
            }
        ) {
            MovieHomeContent(
                viewModel = viewModel,
                openMovieList = openMovieList,
                openMovieDetails = openMovieDetails,
                openSearchPage = openSearchPage,
                openAccountPage = openAccountPage,
                openWatchProviderHub = openWatchProviderHub,
                openLibraryPage = openLibraryPage,
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
                }
            )
        }
    }
}
