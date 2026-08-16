package com.ssverma.feature.movie.navigation

import android.net.Uri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.account.navigation.ProfileNavKey
import com.ssverma.feature.movie.navigation.args.MovieListingRoute
import com.ssverma.feature.movie.ui.details.MovieDetailsScreen
import com.ssverma.feature.movie.ui.details.MovieDetailsViewModel
import com.ssverma.feature.movie.ui.details.MovieImagePagerScreen
import com.ssverma.feature.movie.ui.details.MovieImageShotsScreen
import com.ssverma.feature.movie.ui.details.MovieReviewsScreen
import com.ssverma.feature.movie.ui.details.MovieReviewsViewModel
import com.ssverma.feature.movie.ui.home.MovieScreen
import com.ssverma.feature.movie.ui.list.MovieListScreen
import com.ssverma.feature.movie.ui.list.MovieListViewModel
import com.ssverma.feature.person.navigation.PersonDetailNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubNavKey

fun EntryProviderScope<NavKey>.movieEntries(
    navigator: Navigator,
    openLibraryPage: () -> Unit
) {
    showTimeEntry<MovieListingRoute> { key ->
        MovieListScreen(
            viewModel = hiltViewModel<MovieListViewModel, MovieListViewModel.Factory> { factory ->
                factory.create(key.args)
            },
            onBackPressed = { navigator.goBack() },
            openMovieDetails = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            openWatchHub = { provider ->
                navigator.navigate(
                    WatchProviderHubNavKey(
                        providerId = provider.providerId,
                        providerName = provider.providerName,
                        logoPath = Uri.encode(provider.logoPath),
                        isMovie = true
                    )
                )
            }
        )
    }

    showTimeEntry<MovieHomeNavKey> {
        MovieScreen(
            openMovieList = { listingArgs ->
                navigator.navigate(MovieListingRoute(listingArgs))
            },
            openMovieDetails = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            openSearchPage = {
                navigator.navigate(SearchNavKey)
            },
            openAccountPage = {
                navigator.navigate(ProfileNavKey)
            },
            openWatchProviderHub = { provider ->
                navigator.navigate(
                    WatchProviderHubNavKey(
                        providerId = provider.providerId,
                        providerName = provider.providerName,
                        logoPath = Uri.encode(provider.logoPath),
                        isMovie = true
                    )
                )
            },
            openLibraryPage = openLibraryPage
        )
    }

    showTimeEntry<MovieDetailNavKey> { key ->
        MovieDetailsScreen(
            viewModel = hiltViewModel<MovieDetailsViewModel, MovieDetailsViewModel.Factory> { factory ->
                factory.create(key.movieId)
            },
            onBackPressed = { navigator.goBack() },
            openMovieDetails = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            openImageShotsList = {
                navigator.navigate(MovieImageShotsNavKey(key.movieId))
            },
            openImageShot = { index ->
                navigator.navigate(MovieImagePagerNavKey(key.movieId, index))
            },
            openReviewsList = { movieId ->
                navigator.navigate(MovieReviewsNavKey(movieId))
            },
            openPersonDetails = { cast ->
                navigator.navigate(
                    PersonDetailNavKey(
                        personId = cast.id,
                        personName = cast.name,
                        personImageUrl = cast.avatarImageUrl
                    )
                )
            },
            openMovieList = { listingArgs ->
                navigator.navigate(MovieListingRoute(listingArgs))
            },
            openWatchHub = { provider ->
                navigator.navigate(
                    WatchProviderHubNavKey(
                        providerId = provider.providerId,
                        providerName = provider.providerName,
                        logoPath = Uri.encode(provider.logoPath),
                        isMovie = true
                    )
                )
            }
        )
    }

    showTimeEntry<MovieReviewsNavKey> { key ->
        MovieReviewsScreen(
            viewModel = hiltViewModel<MovieReviewsViewModel, MovieReviewsViewModel.Factory> { factory ->
                factory.create(key.movieId)
            },
            onBackPress = { navigator.goBack() }
        )
    }

    showTimeEntry<MovieImageShotsNavKey> { key ->
        MovieImageShotsScreen(
            onBackPressed = { navigator.goBack() },
            openImagePager = { index ->
                navigator.navigate(MovieImagePagerNavKey(key.movieId, index))
            },
            viewModel = hiltViewModel<MovieDetailsViewModel, MovieDetailsViewModel.Factory> { factory ->
                factory.create(key.movieId)
            }
        )
    }

    showTimeEntry<MovieImagePagerNavKey> { key ->
        MovieImagePagerScreen(
            defaultPageIndex = key.initialPageIndex,
            onBackPressed = { navigator.goBack() },
            viewModel = hiltViewModel<MovieDetailsViewModel, MovieDetailsViewModel.Factory> { factory ->
                factory.create(key.movieId)
            }
        )
    }
}
