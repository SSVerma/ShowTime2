package com.ssverma.feature.movie.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.feature.movie.analytics.MovieAnalyticsScreenName
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.ui.home.content.MovieHomeContent
import com.ssverma.shared.domain.model.ProviderInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    openLibraryPage: () -> Unit,
    viewModel: HomeMovieViewModel = hiltViewModel()
) {
    TrackScreenView(screenName = MovieAnalyticsScreenName.MOVIE_HOME)

    AppPage(
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    ) {
        MovieHomeContent(
            viewModel = viewModel,
            openMovieList = openMovieList,
            openMovieDetails = openMovieDetails,
            openSearchPage = openSearchPage,
            openAccountPage = openAccountPage,
            openWatchProviderHub = openWatchProviderHub,
            openLibraryPage = openLibraryPage
        )
    }
}
