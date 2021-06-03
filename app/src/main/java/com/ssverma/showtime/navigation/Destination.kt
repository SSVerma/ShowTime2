package com.ssverma.showtime.navigation

import androidx.navigation.NavType
import androidx.navigation.compose.NamedNavArgument
import androidx.navigation.compose.navArgument
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.movie.MovieListLaunchable
import com.ssverma.showtime.ui.movie.MovieListingType

interface Destination<T> {
    val placeholderRoute: PlaceholderRoute
    fun actualRoute(input: T): ActualRoute
    fun arguments(): List<NamedNavArgument> = emptyList()
}

sealed class AppDestination<T>(
    internal val identifier: String
) : Destination<T> {
    object Home : AppDestination<Nothing?>("home") {
        override val placeholderRoute: PlaceholderRoute
            get() = Route.placeholderOf(identifier).build()

        override fun actualRoute(input: Nothing?): ActualRoute {
            return Route.actualOf(identifier).build()
        }
    }

    sealed class HomeBottomNavDestination(identifier: String) :
        AppDestination<Nothing?>(identifier) {
        object Movie : HomeBottomNavDestination("home/movie") {
            override val placeholderRoute: PlaceholderRoute
                get() = Route.placeholderOf(identifier).build()

            override fun actualRoute(input: Nothing?): ActualRoute {
                return Route.actualOf(identifier).build()
            }

        }

        object Tv : HomeBottomNavDestination("home/tv") {
            override val placeholderRoute: PlaceholderRoute
                get() = Route.placeholderOf(identifier).build()

            override fun actualRoute(input: Nothing?): ActualRoute {
                return Route.actualOf(identifier).build()
            }

        }

        object People : HomeBottomNavDestination("home/people") {
            override val placeholderRoute: PlaceholderRoute
                get() = Route.placeholderOf(identifier).build()

            override fun actualRoute(input: Nothing?): ActualRoute {
                return Route.actualOf(identifier).build()
            }

        }

        object Library : HomeBottomNavDestination("home/library") {
            override val placeholderRoute: PlaceholderRoute
                get() = Route.placeholderOf(identifier).build()

            override fun actualRoute(input: Nothing?): ActualRoute {
                return Route.actualOf(identifier).build()
            }

        }
    }

    object MovieList : AppDestination<MovieListLaunchable>("movie-list") {
        const val ArgListingType = "type"
        const val ArgTitleRes = "titleRes"
        const val ArgTitle = "title"
        const val ArgGenreId = "genreId"

        override val placeholderRoute: PlaceholderRoute
            get() = Route.placeholderOf(identifier)
                .mandatoryArg(ArgListingType)
                .optionalArg(ArgTitleRes)
                .optionalArg(ArgTitle)
                .optionalArg(ArgGenreId)
                .build()

        override fun actualRoute(input: MovieListLaunchable): ActualRoute {
            return Route.actualOf(identifier)
                .mandatoryArg(ArgListingType, input.listingType.name)
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
}
