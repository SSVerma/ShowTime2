package com.ssverma.feature.tv.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.ui.home.content.TvShowHomeContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowScreen(
    viewModel: HomeTvShowViewModel,
    openTvShowList: (listingArgs: TvShowListingArgs) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit
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
        )
    }
}
