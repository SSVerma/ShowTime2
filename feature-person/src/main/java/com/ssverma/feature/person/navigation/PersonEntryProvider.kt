package com.ssverma.feature.person.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.adaptiveDetailPaneMetadata
import com.ssverma.core.navigation.nav3.adaptiveListPaneMetadata
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.shared.ui.component.illustration.DetailPaneEmptyState
import com.ssverma.core.ui.adaptive.LocalAdaptiveLayoutInfo
import com.ssverma.core.ui.adaptive.NavigationChromeType
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.person.ui.details.PersonDetailsScreen
import com.ssverma.feature.person.ui.details.PersonDetailsViewModel
import com.ssverma.feature.person.ui.home.PersonScreen
import com.ssverma.feature.person.ui.shots.PersonImageShotsScreen
import com.ssverma.feature.person.ui.shots.PersonImagesViewModel
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

fun EntryProviderScope<NavKey>.personEntries(
    navigator: Navigator,
    openLibraryPage: (NavKey) -> Unit
) {
    showTimeEntry<PersonHomeNavKey>(
        metadata = {
            adaptiveListPaneMetadata(
                detailPlaceholder = { DetailPaneEmptyState() }
            )
        }
    ) {
        val isTopLevel =
            LocalAdaptiveLayoutInfo.current.navigationChromeType != NavigationChromeType.FloatingBottomBar
        PersonScreen(
            isTopLevel = isTopLevel,
            onBackPressed = { navigator.navigateUpFromListPane() },
            openPersonDetailsScreen = { person ->
                navigator.navigateFromListToDetail(
                    PersonDetailNavKey(
                        personId = person.id,
                        personName = person.name,
                        personImageUrl = person.imageUrl,
                        source = "person_list"
                    )
                )
            },
            openMovieDetailsScreen = { movieId ->
                navigator.navigateFromListToDetail(MovieDetailNavKey(movieId))
            },
            openTvShowDetailsScreen = { tvShowId ->
                navigator.navigateFromListToDetail(TvShowDetailNavKey(tvShowId))
            }
        )
    }

    showTimeEntry<PersonDetailNavKey>(
        metadata = { adaptiveDetailPaneMetadata() }
    ) { key ->
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
            source = key.source,
            isSideBySideRootDetail = navigator.isSideBySideDetailRoot(key)
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
