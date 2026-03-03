package com.ssverma.feature.movie.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.ui.home.content.MovieHomeContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieScreen(
    viewModel: HomeMovieViewModel,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit
) {
    AppPage(
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    ) {
        MovieHomeContent(
            viewModel = viewModel,
            openMovieList = openMovieList,
            openMovieDetails = openMovieDetails,
            openSearchPage = openSearchPage,
            openAccountPage = openAccountPage,
        )
    }
}
