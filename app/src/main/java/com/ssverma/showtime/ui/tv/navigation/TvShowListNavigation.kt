package com.ssverma.showtime.ui.tv.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.tv.TvShowListScreen
import com.ssverma.showtime.ui.tv.TvShowListingArgs

object TvShowListDestination : DependentDestination<TvShowListingArgs>("tvShows") {
    const val ArgListingType = "type"
    const val ArgTitleRes = "titleRes"
    const val ArgTitle = "title"
    const val ArgGenreId = "genreId"
    const val ArgKeywordId = "keywordId"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(
                name = ArgListingType,
                navType = NavType.IntType
            )
            .optionalArg(
                name = ArgTitleRes,
                navType = NavType.ReferenceType,
                defaultVal = R.string.tv_show
            )
            .optionalArg(
                name = ArgTitle,
                navType = NavType.StringType,
                isNullable = true,
                defaultVal = null
            )
            .optionalArg(name = ArgGenreId, navType = NavType.IntType, defaultVal = 0)
            .optionalArg(name = ArgKeywordId, navType = NavType.IntType, defaultVal = 0)
            .build()
    }

    override fun actualRoute(
        input: TvShowListingArgs,
        builder: ActualRoute.ActualRouteBuilder,
    ): ActualRoute {
        return builder.mandatoryArg(ArgListingType, input.listingType)
            .optionalArg(ArgTitleRes, input.titleRes)
            .optionalArg(ArgTitle, input.title)
            .optionalArg(ArgGenreId, input.genreId)
            .optionalArg(ArgKeywordId, input.keywordId)
            .build()
    }
}

fun NavGraphBuilder.tvShowListGraph(
    navController: NavController
) = composable(destination = TvShowListDestination) {
    TvShowListScreen(
        viewModel = hiltViewModel(it),
        onBackPressed = { navController.popBackStack() },
        openTvShowDetails = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        }
    )
}