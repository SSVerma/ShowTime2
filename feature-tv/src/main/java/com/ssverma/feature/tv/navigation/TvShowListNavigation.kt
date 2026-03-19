package com.ssverma.feature.tv.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ssverma.core.navigation.navigateTo
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.ui.list.TvShowListScreen
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubDestination
import com.ssverma.showtime.feature.filter.navigation.WatchProviderNavArgs

fun NavGraphBuilder.tvShowListGraph(
    navController: NavController
) = composable<TvShowListingRoute>(
    typeMap = TvShowListingArgs.TypeMap
) {
    TvShowListScreen(
        onBackPressed = { navController.popBackStack() },
        openTvShowDetails = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        openWatchHub = { provider ->
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
