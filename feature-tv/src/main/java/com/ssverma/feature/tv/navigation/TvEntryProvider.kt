package com.ssverma.feature.tv.navigation

import android.net.Uri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.account.navigation.ProfileNavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.person.navigation.PersonDetailNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.ui.details.TvEpisodeDetailsScreen
import com.ssverma.feature.tv.ui.details.TvEpisodeDetailsViewModel
import com.ssverma.feature.tv.ui.details.TvSeasonDetailsScreen
import com.ssverma.feature.tv.ui.details.TvSeasonDetailsViewModel
import com.ssverma.feature.tv.ui.details.TvShowDetailsScreen
import com.ssverma.feature.tv.ui.details.TvShowDetailsViewModel
import com.ssverma.feature.tv.ui.details.TvShowImagePagerScreen
import com.ssverma.feature.tv.ui.details.TvShowImageShotsScreen
import com.ssverma.feature.tv.ui.details.TvShowReviewsScreen
import com.ssverma.feature.tv.ui.details.TvShowReviewsViewModel
import com.ssverma.feature.tv.ui.home.TvShowScreen
import com.ssverma.feature.tv.ui.list.TvShowListScreen
import com.ssverma.feature.tv.ui.list.TvShowListViewModel
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubNavKey

fun EntryProviderScope<NavKey>.tvEntries(
    navigator: Navigator,
    openLibraryPage: (LibraryHomeNavKey) -> Unit
) {
    showTimeEntry<TvShowListingRoute> { key ->
        TvShowListScreen(
            viewModel = hiltViewModel<TvShowListViewModel, TvShowListViewModel.Factory> { factory ->
                factory.create(key.args)
            },
            onBackPressed = { navigator.goBack() },
            openTvShowDetails = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            openWatchHub = { provider ->
                navigator.navigate(
                    WatchProviderHubNavKey(
                        providerId = provider.providerId,
                        providerName = provider.providerName,
                        logoPath = Uri.encode(provider.logoPath),
                        isMovie = false
                    )
                )
            },
            openLibraryPage = openLibraryPage
        )
    }

    showTimeEntry<TvShowHomeNavKey> {
        TvShowScreen(
            openTvShowDetails = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            openTvShowList = { listingRoute ->
                navigator.navigate(listingRoute)
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
                        isMovie = false
                    )
                )
            },
            openLibraryPage = openLibraryPage
        )
    }

    showTimeEntry<TvShowDetailNavKey> { key ->
        TvShowDetailsScreen(
            viewModel = hiltViewModel<TvShowDetailsViewModel, TvShowDetailsViewModel.Factory> { factory ->
                factory.create(key.tvShowId)
            },
            onBackPressed = { navigator.goBack() },
            openTvShowDetails = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            openImageShotsList = {
                navigator.navigate(TvShowImageShotsNavKey(key.tvShowId))
            },
            openImageShot = { index ->
                navigator.navigate(TvShowImagePagerNavKey(key.tvShowId, index))
            },
            openReviewsList = { tvShowId ->
                navigator.navigate(TvShowReviewsNavKey(tvShowId))
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
            openTvShowList = { listingRoute ->
                navigator.navigate(listingRoute)
            },
            openTvSeasonDetails = { seasonLaunchable ->
                navigator.navigate(
                    TvSeasonDetailNavKey(
                        tvShowId = seasonLaunchable.tvShowId,
                        seasonNumber = seasonLaunchable.seasonNumber
                    )
                )
            },
            openWatchHub = { provider ->
                navigator.navigate(
                    WatchProviderHubNavKey(
                        providerId = provider.providerId,
                        providerName = provider.providerName,
                        logoPath = Uri.encode(provider.logoPath),
                        isMovie = false
                    )
                )
            },
            openLibraryPage = openLibraryPage
        )
    }

    showTimeEntry<TvShowReviewsNavKey> { key ->
        TvShowReviewsScreen(
            viewModel = hiltViewModel<TvShowReviewsViewModel, TvShowReviewsViewModel.Factory> { factory ->
                factory.create(key.tvShowId)
            },
            onBackPress = { navigator.goBack() }
        )
    }

    showTimeEntry<TvShowImageShotsNavKey> { key ->
        TvShowImageShotsScreen(
            onBackPressed = { navigator.goBack() },
            openImagePager = { index ->
                navigator.navigate(TvShowImagePagerNavKey(key.tvShowId, index))
            },
            viewModel = hiltViewModel<TvShowDetailsViewModel, TvShowDetailsViewModel.Factory> { factory ->
                factory.create(key.tvShowId)
            }
        )
    }

    showTimeEntry<TvShowImagePagerNavKey> { key ->
        TvShowImagePagerScreen(
            defaultPageIndex = key.initialPageIndex,
            onBackPressed = { navigator.goBack() },
            viewModel = hiltViewModel<TvShowDetailsViewModel, TvShowDetailsViewModel.Factory> { factory ->
                factory.create(key.tvShowId)
            }
        )
    }

    showTimeEntry<TvSeasonDetailNavKey> { key ->
        TvSeasonDetailsScreen(
            viewModel = hiltViewModel<TvSeasonDetailsViewModel, TvSeasonDetailsViewModel.Factory> { factory ->
                factory.create(tvShowId = key.tvShowId, seasonNumber = key.seasonNumber)
            },
            onBackPress = { navigator.goBack() },
            openEpisodeDetails = { episodeLaunchable ->
                navigator.navigate(
                    TvEpisodeDetailNavKey(
                        tvShowId = episodeLaunchable.tvShowId,
                        seasonNumber = episodeLaunchable.seasonNumber,
                        episodeNumber = episodeLaunchable.episodeNumber
                    )
                )
            },
            openPersonDetails = { cast ->
                navigator.navigate(
                    PersonDetailNavKey(
                        personId = cast.id,
                        personName = cast.name,
                        personImageUrl = cast.avatarImageUrl
                    )
                )
            }
        )
    }

    showTimeEntry<TvEpisodeDetailNavKey> { key ->
        TvEpisodeDetailsScreen(
            viewModel = hiltViewModel<TvEpisodeDetailsViewModel, TvEpisodeDetailsViewModel.Factory> { factory ->
                factory.create(
                    tvShowId = key.tvShowId,
                    seasonNumber = key.seasonNumber,
                    episodeNumber = key.episodeNumber
                )
            },
            onBackPress = { navigator.goBack() },
            openPersonDetails = { cast ->
                navigator.navigate(
                    PersonDetailNavKey(
                        personId = cast.id,
                        personName = cast.name,
                        personImageUrl = cast.avatarImageUrl
                    )
                )
            }
        )
    }
}
