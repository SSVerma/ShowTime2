package com.ssverma.feature.person.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.account.navigation.ProfileNavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.person.ui.details.PersonDetailsScreen
import com.ssverma.feature.person.ui.details.PersonDetailsViewModel
import com.ssverma.feature.person.ui.home.PersonScreen
import com.ssverma.feature.person.ui.shots.PersonImageShotsScreen
import com.ssverma.feature.person.ui.shots.PersonImagesViewModel
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

fun EntryProviderScope<NavKey>.personEntries(
    navigator: Navigator,
    openLibraryPage: () -> Unit
) {
    showTimeEntry<PersonHomeNavKey> {
        PersonScreen(
            openPersonDetailsScreen = { person ->
                navigator.navigate(
                    PersonDetailNavKey(
                        personId = person.id,
                        personName = person.name,
                        personImageUrl = person.imageUrl
                    )
                )
            },
            openMovieDetailsScreen = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            openTvShowDetailsScreen = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            openSearchPage = {
                navigator.navigate(SearchNavKey)
            },
            openAccountPage = {
                navigator.navigate(ProfileNavKey)
            },
            openLibraryPage = openLibraryPage
        )
    }

    showTimeEntry<PersonDetailNavKey> { key ->
        PersonDetailsScreen(
            personId = key.personId,
            initialName = key.personName,
            initialImageUrl = key.personImageUrl,
            viewModel = hiltViewModel<PersonDetailsViewModel, PersonDetailsViewModel.Factory> { factory ->
                factory.create(key.personId)
            },
            onBackPress = { navigator.goBack() },
            openMovieDetails = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            openTvShowDetails = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            openPersonAllImages = { personId ->
                navigator.navigate(PersonImageShotsNavKey(personId))
            }
        )
    }

    showTimeEntry<PersonImageShotsNavKey> { key ->
        PersonImageShotsScreen(
            viewModel = hiltViewModel<PersonImagesViewModel, PersonImagesViewModel.Factory> { factory ->
                factory.create(key.personId)
            },
            onBackPressed = { navigator.goBack() }
        )
    }
}
