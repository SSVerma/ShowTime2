package com.ssverma.feature.tv.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.account.navigation.ProfileDestination
import com.ssverma.feature.search.navigation.SearchDestination
import com.ssverma.feature.tv.ui.home.TvShowScreen
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubDestination
import com.ssverma.showtime.feature.filter.navigation.WatchProviderNavArgs

fun NavGraphBuilder.tvShowHomeGraph(
    navController: NavController
) = composable(destination = TvShowHomeDestination) {
    TvShowScreen(
        openTvShowDetails = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        openTvShowList = { listingArgs ->
            navController.navigateTo(
                TvShowListDestination.actualRoute(listingArgs)
            )
        },
        openSearchPage = {
            navController.navigateTo(SearchDestination.actualRoute)
        },
        openAccountPage = {
            navController.navigateTo(ProfileDestination.actualRoute)
        },
        openWatchProviderHub = { provider ->
            navController.navigateTo(
                WatchProviderHubDestination.actualRoute(
                    WatchProviderNavArgs(
                        providerId = provider.providerId,
                        providerName = provider.providerName,
                        logoPath = Uri.encode(provider.logoPath),
                        isMovie = false
                    )
                )
            )
        }
    )
}
