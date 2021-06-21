package com.ssverma.showtime.navigation

import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.movie.MovieListLaunchable
import com.ssverma.showtime.ui.movie.MovieListingType

interface Destination {
    val identifier: String
}

abstract class StandaloneDestination(override val identifier: String) : Destination {
    val placeholderRoute: PlaceholderRoute
        get() = Route.placeholderOf(identifier).build()

    val actualRoute: ActualRoute
        get() = Route.actualOf(identifier).build()
}

abstract class DependentDestination<T>(override val identifier: String) : Destination {
    val placeholderRoute: PlaceholderRoute
        get() = placeholderRoute(Route.placeholderOf(identifier))

    abstract fun placeholderRoute(
        builder: PlaceholderRoute.PlaceHolderRouteBuilder
    ): PlaceholderRoute

    abstract fun actualRoute(
        input: T,
        builder: ActualRoute.ActualRouteBuilder = Route.actualOf(identifier)
    ): ActualRoute

    abstract fun arguments(): List<NamedNavArgument>
}

sealed class AppDestination(
    override val identifier: String
) : Destination {
    object Home : StandaloneDestination("home")

    sealed class HomeBottomNavDestination(identifier: String) : StandaloneDestination(identifier) {
        object Movie : HomeBottomNavDestination("home/movie")

        object Tv : HomeBottomNavDestination("home/tv")

        object People : HomeBottomNavDestination("home/people")

        object Library : HomeBottomNavDestination("home/library")
    }

    object MovieList : DependentDestination<MovieListLaunchable>("movie-list") {
        const val ArgListingType = "type"
        const val ArgTitleRes = "titleRes"
        const val ArgTitle = "title"
        const val ArgGenreId = "genreId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder.mandatoryArg(ArgListingType)
                .optionalArg(ArgTitleRes)
                .optionalArg(ArgTitle)
                .optionalArg(ArgGenreId)
                .build()
        }

        override fun actualRoute(
            input: MovieListLaunchable,
            builder: ActualRoute.ActualRouteBuilder,
        ): ActualRoute {
            return builder.mandatoryArg(ArgListingType, input.listingType.name)
                .optionalArg(ArgTitleRes, input.titleRes.toString())
                .optionalArg(ArgTitle, input.title)
                .optionalArg(ArgGenreId, input.genre?.id ?: 0)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(ArgListingType) {
                    type = NavType.EnumType(MovieListingType::class.java)
                },
                navArgument(ArgTitleRes) {
                    defaultValue = R.string.movies
                },
                navArgument(ArgTitle) {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument(ArgGenreId) {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        }
    }

    object MovieDetails : DependentDestination<Int>("movie") {
        const val ArgMovieId = "movieId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(ArgMovieId)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(ArgMovieId, input)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(ArgMovieId) {
                    type = NavType.IntType
                },
            )
        }
    }
}
