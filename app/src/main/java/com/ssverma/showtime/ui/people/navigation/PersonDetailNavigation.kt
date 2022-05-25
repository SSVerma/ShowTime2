package com.ssverma.showtime.ui.people.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import com.ssverma.core.navigation.*
import com.ssverma.showtime.ui.movie.navigation.MovieDetailDestination
import com.ssverma.showtime.ui.people.PersonDetailsScreen
import com.ssverma.showtime.ui.tv.navigation.TvShowDetailDestination

object PersonDetailDestination : DependentDestination<Int>("person") {
    const val PersonId = "personId"

    override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
        return builder
            .mandatoryArg(PersonId, navType = NavType.IntType)
            .build()
    }

    override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
        return builder
            .mandatoryArg(PersonId, input)
            .build()
    }
}

fun NavGraphBuilder.personDetailGraph(
    navController: NavController
) = composable(destination = PersonDetailDestination) {
    PersonDetailsScreen(
        viewModel = hiltViewModel(it),
        onBackPress = { navController.popBackStack() },
        openMovieDetails = { movieId ->
            navController.navigateTo(MovieDetailDestination.actualRoute(movieId))
        },
        openTvShowDetails = { tvShowId ->
            navController.navigateTo(TvShowDetailDestination.actualRoute(tvShowId))
        },
        openPersonAllImages = { personId ->
            navController.navigateTo(PersonImageShotsDestination.actualRoute(personId))
        }
    )
}