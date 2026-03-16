package com.ssverma.feature.tv.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.ui.home.content.TvShowHomeContent
import com.ssverma.shared.domain.model.ProviderInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowScreen(
    openTvShowList: (listingArgs: TvShowListingArgs) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    viewModel: HomeTvShowViewModel = hiltViewModel()
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
        )
    }
}
