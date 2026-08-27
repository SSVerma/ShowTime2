package com.ssverma.feature.person.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.person.ui.details.PersonDetailsScreen
import com.ssverma.feature.person.ui.details.PersonDetailsViewModel
import com.ssverma.feature.person.ui.home.PersonScreen
import com.ssverma.feature.person.ui.shots.PersonImageShotsScreen
import com.ssverma.feature.person.ui.shots.PersonImagesViewModel
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

fun EntryProviderScope<NavKey>.personEntries(
    navigator: Navigator,
    openLibraryPage: (LibraryHomeNavKey) -> Unit
) {
    showTimeEntry<PersonHomeNavKey> {
        PersonScreen(
            onBackPressed = { navigator.goBack() },
            openPersonDetailsScreen = { person ->
                navigator.navigate(
                    PersonDetailNavKey(
                        personId = person.id,
                        personName = person.name,
                        personImageUrl = person.imageUrl,
                        source = "person_list"
                    )
                )
            },
            openMovieDetailsScreen = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            openTvShowDetailsScreen = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            }
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
            },
            source = key.source
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
