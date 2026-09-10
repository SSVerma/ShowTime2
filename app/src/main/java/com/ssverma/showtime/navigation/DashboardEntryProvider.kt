package com.ssverma.showtime.navigation

import android.net.Uri
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.library.navigation.BacklogChallengeNavKey
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.library.navigation.CinemaReceiptNavKey
import com.ssverma.feature.library.navigation.CinephileWrappedNavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.TasteProfileNavKey
import com.ssverma.feature.community.navigation.CommunityDiscussionsNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.match.navigation.MatchRoomNavKey
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingRoute
import com.ssverma.feature.payment.navigation.ProPaywallNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey
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
                val navKey = if (args.mediaType == MediaType.Movie) {
                    CommunityDiscussionsNavKey.movie(
                        movieId = args.mediaId,
                        movieTitle = args.title,
                        posterImageUrl = args.posterImageUrl,
                        backdropImageUrl = args.backdropImageUrl
                    )
                } else if (season != null && episode != null) {
                    CommunityDiscussionsNavKey.tvEpisode(
                        tvShowId = args.mediaId,
                        seasonNumber = season,
                        episodeNumber = episode,
                        episodeTitle = args.title,
                        posterImageUrl = args.posterImageUrl,
                        backdropImageUrl = args.backdropImageUrl
                    )
                } else {
                    CommunityDiscussionsNavKey.tvShow(
                        tvShowId = args.mediaId,
                        tvShowTitle = args.title,
                        posterImageUrl = args.posterImageUrl,
                        backdropImageUrl = args.backdropImageUrl
                    )
                }
                navigator.navigate(navKey)
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
            },
            openCinemaDiary = {
                navigator.navigate(CinemaDiaryNavKey)
            },
            openTasteProfile = {
                navigator.navigate(TasteProfileNavKey)
            },
            openWrapped = {
                navigator.navigate(CinephileWrappedNavKey)
            },
            openBacklogChallenges = {
                navigator.navigate(BacklogChallengeNavKey)
            },
            openReceipt = {
                navigator.navigate(CinemaReceiptNavKey)
            },
            openPeople = {
                navigator.navigate(PersonHomeNavKey)
            },
            openMovieMatch = {
                navigator.navigate(MatchRoomNavKey())
            },
            openMovieGenreListing = { genre ->
                navigator.navigate(
                    MovieListingRoute(
                        MovieListingArgs.ByGenre(
                            genreId = genre.id,
                            title = genre.name
                        )
                    )
                )
            },
            openTvGenreListing = { genre ->
                navigator.navigate(
                    TvShowListingRoute(
                        TvShowListingArgs.ByGenre(
                            genreId = genre.id,
                            title = genre.name
                        )
                    )
                )
            },
            openProPaywall = {
                navigator.navigate(ProPaywallNavKey)
            }
        )
    }
}
