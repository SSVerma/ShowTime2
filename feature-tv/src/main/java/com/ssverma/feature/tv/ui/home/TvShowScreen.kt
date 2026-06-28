package com.ssverma.feature.tv.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.ui.home.content.TvShowHomeContent
import com.ssverma.shared.domain.model.ProviderInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowScreen(
    openTvShowList: (listingRoute: TvShowListingRoute) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    openLibraryPage: () -> Unit,
    viewModel: HomeTvShowViewModel = hiltViewModel()
) {
    TrackScreenView(screenName = TvAnalyticsScreenName.TV_HOME)

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        AppPage(
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        ) {
            TvShowHomeContent(
                viewModel = viewModel,
                openTvShowList = openTvShowList,
                openTvShowDetails = openTvShowDetails,
                openSearchPage = openSearchPage,
                openAccountPage = openAccountPage,
                openWatchProviderHub = openWatchProviderHub,
                openLibraryPage = openLibraryPage
            )
        }
    }
}
