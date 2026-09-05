package com.ssverma.feature.filter.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.filter.ui.discovery.UniversalDiscoveryScreen
import com.ssverma.feature.filter.ui.hub.WatchProviderHubScreen
import com.ssverma.feature.filter.ui.hub.WatchProviderHubViewModel
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingRoute
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubNavKey

fun EntryProviderScope<NavKey>.filterEntries(
    navigator: Navigator
) {
    showTimeEntry<WatchProviderHubNavKey> { key ->
        WatchProviderHubScreen(
            viewModel = hiltViewModel<WatchProviderHubViewModel, WatchProviderHubViewModel.Factory> { factory ->
                factory.create(
                    providerId = key.providerId,
                    providerName = key.providerName,
                    logoPath = key.logoPath,
                    isMovie = key.isMovie
                )
            },
            source = key.source,
            onMovieClick = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            onTvShowClick = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            onBackClick = {
                navigator.goBack()
            },
            onGenreClick = { genre, isMovie ->
                if (isMovie) {
                    navigator.navigate(
                        MovieListingRoute(
                            MovieListingArgs.ByGenre(
                                genreId = genre.id,
                                title = genre.name
                            )
                        )
                    )
                } else {
                    navigator.navigate(
                        TvShowListingRoute(
                            TvShowListingArgs.ByGenre(
                                genreId = genre.id,
                                title = genre.name
                            )
                        )
                    )
                }
            },
            onMovieSeeAllClick = { providerInfo, discoverConfig ->
                navigator.navigate(
                    MovieListingRoute(
                        MovieListingArgs.Discovery(
                            initialConfig = discoverConfig,
                            title = providerInfo.providerName,
                        )
                    )
                )
            },
            onTvSeeAllClick = { provider, discoverConfig ->
                navigator.navigate(
                    TvShowListingRoute(
                        TvShowListingArgs.Discovery(
                            initialConfig = discoverConfig,
                            title = provider.providerName
                        )
                    )
                )
            },
            openLibraryPage = { libraryNavKey ->
                navigator.navigate(libraryNavKey)
            }
        )
    }

    showTimeEntry<UniversalDiscoveryNavKey> { key ->
        UniversalDiscoveryScreen(
            navKey = key,
            onBackClick = { navigator.goBack() },
            onOpenMovieDetails = { movieId -> navigator.navigate(MovieDetailNavKey(movieId)) },
            onOpenTvShowDetails = { tvShowId -> navigator.navigate(TvShowDetailNavKey(tvShowId)) },
            openLibraryPage = { libraryNavKey -> navigator.navigate(libraryNavKey) },
            onOpenCinemaDiary = { navigator.navigate(CinemaDiaryNavKey) }
        )
    }
}
