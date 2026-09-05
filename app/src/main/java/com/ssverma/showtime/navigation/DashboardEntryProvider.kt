package com.ssverma.showtime.navigation

import android.net.Uri
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.movie.navigation.MovieDiscussionsNavKey
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingRoute
import com.ssverma.feature.tv.navigation.TvEpisodeDiscussionsNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey
import com.ssverma.feature.tv.navigation.TvShowDiscussionsNavKey
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.showtime.R
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubNavKey
import com.ssverma.showtime.ui.dashboard.DashboardScreen

fun EntryProviderScope<NavKey>.dashboardEntries(
    navigator: Navigator,
    openLibraryPage: (LibraryHomeNavKey) -> Unit
) {
    showTimeEntry<DashboardHomeNavKey> {
        DashboardScreen(
            openMovieDetails = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            openTvShowDetails = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            openDiscussions = { args ->
                val season = args.seasonNumber
                val episode = args.episodeNumber
                if (args.mediaType == MediaType.Movie) {
                    navigator.navigate(
                        MovieDiscussionsNavKey(
                            movieId = args.mediaId,
                            movieTitle = args.title,
                            posterImageUrl = args.posterImageUrl,
                            backdropImageUrl = args.backdropImageUrl
                        )
                    )
                } else if (season != null && episode != null) {
                    navigator.navigate(
                        TvEpisodeDiscussionsNavKey(
                            tvShowId = args.mediaId,
                            seasonNumber = season,
                            episodeNumber = episode,
                            episodeTitle = args.title,
                            posterImageUrl = args.posterImageUrl,
                            backdropImageUrl = args.backdropImageUrl
                        )
                    )
                } else {
                    navigator.navigate(
                        TvShowDiscussionsNavKey(
                            tvShowId = args.mediaId,
                            tvShowTitle = args.title,
                            posterImageUrl = args.posterImageUrl,
                            backdropImageUrl = args.backdropImageUrl
                        )
                    )
                }
            },
            openCinemaGame = {
                navigator.navigate(CinemaGameNavKey)
            },
            openWatchProviderHub = { provider, isMovie ->
                navigator.navigate(
                    WatchProviderHubNavKey(
                        providerId = provider.providerId,
                        providerName = provider.providerName,
                        logoPath = Uri.encode(provider.logoPath),
                        isMovie = isMovie,
                        source = "dashboard"
                    )
                )
            },
            openStudioPortal = { portal ->
                if (portal.isNetwork) {
                    val tvConfig = TvDiscoverConfig.builder()
                        .with(DiscoverOption.Network(networkId = portal.id))
                        .build()
                    navigator.navigate(
                        TvShowListingRoute(
                            TvShowListingArgs.Discovery(
                                initialConfig = tvConfig,
                                titleRes = portal.nameRes
                            )
                        )
                    )
                } else {
                    val movieConfig = MovieDiscoverConfig.builder()
                        .with(DiscoverOption.Company(companyId = portal.id))
                        .build()
                    navigator.navigate(
                        MovieListingRoute(
                            MovieListingArgs.Discovery(
                                initialConfig = movieConfig,
                                titleRes = portal.nameRes
                            )
                        )
                    )
                }
            },
            openMovieListing = {
                navigator.navigate(
                    MovieListingRoute(
                        MovieListingArgs.Popular(titleRes = R.string.popuplar)
                    )
                )
            },
            openTvListing = {
                navigator.navigate(
                    TvShowListingRoute(
                        TvShowListingArgs.Popular(titleRes = R.string.popuplar)
                    )
                )
            },
            openLibraryPage = openLibraryPage,
            openUniversalDiscovery = { navKey ->
                navigator.navigate(navKey)
            }
        )
    }
}
