package com.ssverma.feature.person.ui.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.feature.person.R
import com.ssverma.feature.person.analytics.PersonAnalyticsScreenName
import com.ssverma.feature.person.ui.home.content.PersonHomeContent
import com.ssverma.shared.domain.model.person.Person

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonScreen(
    viewModel: PersonHomeViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    openPersonDetailsScreen: (Person) -> Unit,
    openMovieDetailsScreen: (movieId: Int) -> Unit,
    openTvShowDetailsScreen: (tvShowId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    TrackScreenView(screenName = PersonAnalyticsScreenName.PERSON_LISTING)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Screen(
        title = stringResource(id = R.string.popular_people),
        onBackPressed = onBackPressed,
        scrollBehavior = scrollBehavior,
        modifier = modifier
    ) { innerPadding ->
        val pagedPersons = viewModel.popularPersons.collectAsLazyPagingItems()

        PagedContent(pagedPersons) { persons ->
            PersonHomeContent(
                pagedPersons = persons,
                contentPadding = innerPadding,
                openPersonDetailsScreen = openPersonDetailsScreen,
                openMovieDetailsScreen = openMovieDetailsScreen,
                openTvShowDetailsScreen = openTvShowDetailsScreen
            )
        }
    }
}
