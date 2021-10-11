package com.ssverma.showtime.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.movie.MovieListLaunchable
import com.ssverma.showtime.ui.movie.MovieListingType
import com.ssverma.showtime.ui.tv.TvEpisodeLaunchable
import com.ssverma.showtime.ui.tv.TvSeasonLaunchable
import com.ssverma.showtime.ui.tv.TvShowListLaunchable
import com.ssverma.showtime.ui.tv.TvShowListingType

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

abstract class GraphDestination(override val identifier: String) : StandaloneDestination(identifier)

sealed class AppDestination(
    override val identifier: String
) : Destination {
    object Home : GraphDestination("home")

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
        const val ArgKeywordId = "keywordId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder.mandatoryArg(ArgListingType)
                .optionalArg(ArgTitleRes)
                .optionalArg(ArgTitle)
                .optionalArg(ArgGenreId)
                .optionalArg(ArgKeywordId)
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
                .optionalArg(ArgKeywordId, input.keyword?.id ?: 0)
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
                    defaultValue = null
                },
                navArgument(ArgGenreId) {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument(ArgKeywordId) {
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

    object MovieDetailsGraph : GraphDestination("movieInfo")

    object MovieReviews : DependentDestination<Int>("movie/reviews") {
        const val MovieId = "movieId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(MovieId)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(MovieId, input)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(MovieId) {
                    type = NavType.IntType
                },
            )
        }
    }

    object MovieImageShots : StandaloneDestination("movie/shots/image")

    object MovieImagePager : DependentDestination<Int>("movie/imagePager") {
        const val PageIndex = "pageIndex"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(PageIndex)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(PageIndex, input)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(PageIndex) {
                    type = NavType.IntType
                },
            )
        }
    }

    object PersonDetails : DependentDestination<Int>("person") {
        const val PersonId = "personId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(PersonId)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(PersonId, input)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(PersonId) {
                    type = NavType.IntType
                },
            )
        }
    }

    object PersonImages : DependentDestination<Int>("person/images") {
        const val PersonId = "personId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(PersonId)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(PersonId, input)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(PersonId) {
                    type = NavType.IntType
                },
            )
        }
    }

    object TvShowDetails : DependentDestination<Int>("tvShow") {
        const val ArgTvShowId = "tvShowId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(ArgTvShowId)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(ArgTvShowId, input)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(ArgTvShowId) {
                    type = NavType.IntType
                },
            )
        }
    }

    object TvShowImageShots : StandaloneDestination("tvShow/shots/image")

    object TvShowReviews : DependentDestination<Int>("tvShow/reviews") {
        const val TvShowId = "tvShowId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(TvShowId)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(TvShowId, input)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(TvShowId) {
                    type = NavType.IntType
                },
            )
        }
    }

    object TvImagePager : DependentDestination<Int>("tv/imagePager") {
        const val PageIndex = "pageIndex"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(PageIndex)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(PageIndex, input)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(PageIndex) {
                    type = NavType.IntType
                },
            )
        }
    }

    object TvSeasonDetails : DependentDestination<TvSeasonLaunchable>("tvShow/season") {
        const val ArgTvShowId = "tvShowId"
        const val ArgTvSeasonNumber = "seasonNumber"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(ArgTvShowId)
                .mandatoryArg(ArgTvSeasonNumber)
                .build()
        }

        override fun actualRoute(
            input: TvSeasonLaunchable,
            builder: ActualRoute.ActualRouteBuilder
        ): ActualRoute {
            return builder
                .mandatoryArg(ArgTvShowId, input.tvShowId)
                .mandatoryArg(ArgTvSeasonNumber, input.seasonNumber)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(ArgTvShowId) {
                    type = NavType.IntType
                },
                navArgument(ArgTvSeasonNumber) {
                    type = NavType.IntType
                },
            )
        }
    }

    object TvEpisodeDetails : DependentDestination<TvEpisodeLaunchable>("tvShow/season/episode") {
        const val ArgTvShowId = "tvShowId"
        const val ArgSeasonNumber = "seasonNumber"
        const val ArgEpisodeNumber = "episodeNumber"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(ArgTvShowId)
                .mandatoryArg(ArgSeasonNumber)
                .mandatoryArg(ArgEpisodeNumber)
                .build()
        }

        override fun actualRoute(
            input: TvEpisodeLaunchable,
            builder: ActualRoute.ActualRouteBuilder
        ): ActualRoute {
            return builder
                .mandatoryArg(ArgTvShowId, input.tvShowId)
                .mandatoryArg(ArgSeasonNumber, input.seasonNumber)
                .mandatoryArg(ArgEpisodeNumber, input.episodeNumber)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(ArgTvShowId) {
                    type = NavType.IntType
                },
                navArgument(ArgSeasonNumber) {
                    type = NavType.IntType
                },
                navArgument(ArgEpisodeNumber) {
                    type = NavType.IntType
                },
            )
        }
    }

    object TvShowList : DependentDestination<TvShowListLaunchable>("tvShows") {
        const val ArgListingType = "type"
        const val ArgTitleRes = "titleRes"
        const val ArgTitle = "title"
        const val ArgGenreId = "genreId"
        const val ArgKeywordId = "keywordId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder.mandatoryArg(ArgListingType)
                .optionalArg(ArgTitleRes)
                .optionalArg(ArgTitle)
                .optionalArg(ArgGenreId)
                .optionalArg(ArgKeywordId)
                .build()
        }

        override fun actualRoute(
            input: TvShowListLaunchable,
            builder: ActualRoute.ActualRouteBuilder,
        ): ActualRoute {
            return builder.mandatoryArg(ArgListingType, input.listingType.name)
                .optionalArg(ArgTitleRes, input.titleRes.toString())
                .optionalArg(ArgTitle, input.title)
                .optionalArg(ArgGenreId, input.genre?.id ?: 0)
                .optionalArg(ArgKeywordId, input.keyword?.id ?: 0)
                .build()
        }

        override fun arguments(): List<NamedNavArgument> {
            return listOf(
                navArgument(ArgListingType) {
                    type = NavType.EnumType(TvShowListingType::class.java)
                },
                navArgument(ArgTitleRes) {
                    type = NavType.ReferenceType
                    defaultValue = R.string.movies
                },
                navArgument(ArgTitle) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument(ArgGenreId) {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument(ArgKeywordId) {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        }
    }
}
