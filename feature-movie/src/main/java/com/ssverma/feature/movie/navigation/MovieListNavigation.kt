package com.ssverma.feature.movie.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.ui.MovieListScreen
import com.ssverma.feature.movie.ui.MovieListingArgs

object MovieListDestination : DependentDestination<MovieListingArgs>("movies") {
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
                ArgTitleRes,
                navType = NavType.ReferenceType,
                defaultVal = R.string.movies
            )
            .optionalArg(
                ArgTitle,
                navType = NavType.StringType,
                isNullable = true,
                defaultVal = null
            )
            .optionalArg(ArgGenreId, navType = NavType.IntType, defaultVal = 0)
            .optionalArg(ArgKeywordId, navType = NavType.IntType, defaultVal = 0)
            .build()
    }

    override fun actualRoute(
        input: MovieListingArgs,
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

fun NavGraphBuilder.movieListGraph(
    navController: NavController
) = composable(MovieListDestination) {
    MovieListScreen(
        viewModel = hiltViewModel(it),
        onBackPressed = { navController.popBackStack() },
        openMovieDetails = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        }
    )
}