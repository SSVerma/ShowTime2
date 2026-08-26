package com.ssverma.feature.tv.ui.home

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
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.ui.home.content.TvShowHomeContent
import com.ssverma.shared.domain.model.ProviderInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowScreen(
    openTvShowList: (listingRoute: TvShowListingRoute) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit,
    openTvSeasonDetails: (showTmdbId: Int, seasonNumber: Int) -> Unit = { id, _ ->
        openTvShowDetails(
            id
        )
    },
    viewModel: HomeTvShowViewModel = hiltViewModel()
) {
    TrackScreenView(screenName = TvAnalyticsScreenName.TV_HOME)

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
            TvShowHomeContent(
                viewModel = viewModel,
                openTvShowList = openTvShowList,
                openTvShowDetails = openTvShowDetails,
                openSearchPage = openSearchPage,
                openAccountPage = openAccountPage,
                openWatchProviderHub = openWatchProviderHub,
                openLibraryPage = openLibraryPage,
                openTvSeasonDetails = openTvSeasonDetails,
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
