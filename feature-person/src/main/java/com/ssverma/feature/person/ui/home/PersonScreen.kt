package com.ssverma.feature.person.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.feature.person.ui.home.content.PersonHomeContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
    viewModel: PersonHomeViewModel = hiltViewModel(),
    openPersonDetailsScreen: (personId: Int) -> Unit,
    openMovieDetailsScreen: (movieId: Int) -> Unit,
    openTvShowDetailsScreen: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit
) {
    val pagedPersons = viewModel.popularPersons.collectAsLazyPagingItems()

    PagedContent(pagedPersons) { persons ->
        PersonHomeContent(
            pagedPersons = persons,
            openPersonDetailsScreen = openPersonDetailsScreen,
            openMovieDetailsScreen = openMovieDetailsScreen,
            openTvShowDetailsScreen = openTvShowDetailsScreen,
            openSearchPage = openSearchPage,
            openAccountPage = openAccountPage
        )
    }
}
