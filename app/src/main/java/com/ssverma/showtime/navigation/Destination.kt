package com.ssverma.showtime.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.movie.MovieListLaunchable
import com.ssverma.showtime.ui.movie.MovieListingType
import com.ssverma.showtime.ui.tv.TvEpisodeLaunchable
import com.ssverma.showtime.ui.tv.TvSeasonLaunchable
import com.ssverma.showtime.ui.tv.TvShowListLaunchable
import com.ssverma.showtime.ui.tv.TvShowListingType

interface Destination {
    val placeholderRoute: PlaceholderRoute
    val arguments: List<NamedNavArgument>
}

abstract class StandaloneDestination(private val identifier: String) : Destination {
    override val placeholderRoute: PlaceholderRoute
        get() = Route.placeholderOf(identifier).build()

    override val arguments: List<NamedNavArgument>
        get() = placeholderRoute.navArguments

    val actualRoute: ActualRoute
        get() = Route.actualOf(identifier).build()
}

abstract class DependentDestination<T>(private val identifier: String) : Destination {
    override val placeholderRoute: PlaceholderRoute
        get() = placeholderRoute(Route.placeholderOf(identifier))

    override val arguments: List<NamedNavArgument>
        get() = placeholderRoute.navArguments

    abstract fun placeholderRoute(
        builder: PlaceholderRoute.PlaceHolderRouteBuilder
    ): PlaceholderRoute

    abstract fun actualRoute(
        input: T,
        builder: ActualRoute.ActualRouteBuilder = Route.actualOf(identifier)
    ): ActualRoute
}

abstract class GraphDestination(identifier: String) : StandaloneDestination(identifier)

sealed class AppDestination : Destination {
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
            return builder
                .mandatoryArg(
                    name = ArgListingType,
                    navType = NavType.EnumType(MovieListingType::class.java)
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
            input: MovieListLaunchable,
            builder: ActualRoute.ActualRouteBuilder,
        ): ActualRoute {
            return builder.mandatoryArg(ArgListingType, input.listingType.name)
                .optionalArg(ArgTitleRes, input.titleRes)
                .optionalArg(ArgTitle, input.title)
                .optionalArg(ArgGenreId, input.genre?.id ?: 0)
                .optionalArg(ArgKeywordId, input.keyword?.id ?: 0)
                .build()
        }
    }

    object MovieDetails : DependentDestination<Int>("movie") {
        const val ArgMovieId = "movieId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(ArgMovieId, navType = NavType.IntType)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(ArgMovieId, input)
                .build()
        }
    }

    object MovieDetailsGraph : GraphDestination("movieInfo")

    object MovieReviews : DependentDestination<Int>("movie/reviews") {
        const val MovieId = "movieId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(MovieId, navType = NavType.IntType)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(MovieId, input)
                .build()
        }
    }

    object MovieImageShots : StandaloneDestination("movie/shots/image")

    object MovieImagePager : DependentDestination<Int>("movie/imagePager") {
        const val PageIndex = "pageIndex"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(PageIndex, navType = NavType.IntType)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(PageIndex, input)
                .build()
        }
    }

    object PersonDetails : DependentDestination<Int>("person") {
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

    object PersonImages : DependentDestination<Int>("person/images") {
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

    object TvShowDetails : DependentDestination<Int>("tvShow") {
        const val ArgTvShowId = "tvShowId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(ArgTvShowId, navType = NavType.IntType)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(ArgTvShowId, input)
                .build()
        }
    }

    object TvShowImageShots : StandaloneDestination("tvShow/shots/image")

    object TvShowReviews : DependentDestination<Int>("tvShow/reviews") {
        const val TvShowId = "tvShowId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(TvShowId, navType = NavType.IntType)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(TvShowId, input)
                .build()
        }
    }

    object TvImagePager : DependentDestination<Int>("tv/imagePager") {
        const val PageIndex = "pageIndex"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(PageIndex, navType = NavType.IntType)
                .build()
        }

        override fun actualRoute(input: Int, builder: ActualRoute.ActualRouteBuilder): ActualRoute {
            return builder
                .mandatoryArg(PageIndex, input)
                .build()
        }
    }

    object TvSeasonDetails : DependentDestination<TvSeasonLaunchable>("tvShow/season") {
        const val ArgTvShowId = "tvShowId"
        const val ArgTvSeasonNumber = "seasonNumber"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(ArgTvShowId, navType = NavType.IntType)
                .mandatoryArg(ArgTvSeasonNumber, navType = NavType.IntType)
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
    }

    object TvEpisodeDetails : DependentDestination<TvEpisodeLaunchable>("tvShow/season/episode") {
        const val ArgTvShowId = "tvShowId"
        const val ArgSeasonNumber = "seasonNumber"
        const val ArgEpisodeNumber = "episodeNumber"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(ArgTvShowId, navType = NavType.IntType)
                .mandatoryArg(ArgSeasonNumber, navType = NavType.IntType)
                .mandatoryArg(ArgEpisodeNumber, navType = NavType.IntType)
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
    }

    object TvShowList : DependentDestination<TvShowListLaunchable>("tvShows") {
        const val ArgListingType = "type"
        const val ArgTitleRes = "titleRes"
        const val ArgTitle = "title"
        const val ArgGenreId = "genreId"
        const val ArgKeywordId = "keywordId"

        override fun placeholderRoute(builder: PlaceholderRoute.PlaceHolderRouteBuilder): PlaceholderRoute {
            return builder
                .mandatoryArg(
                    name = ArgListingType,
                    navType = NavType.EnumType(TvShowListingType::class.java)
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
            input: TvShowListLaunchable,
            builder: ActualRoute.ActualRouteBuilder,
        ): ActualRoute {
            return builder.mandatoryArg(ArgListingType, input.listingType.name)
                .optionalArg(ArgTitleRes, input.titleRes)
                .optionalArg(ArgTitle, input.title)
                .optionalArg(ArgGenreId, input.genre?.id ?: 0)
                .optionalArg(ArgKeywordId, input.keyword?.id ?: 0)
                .build()
        }
    }
}
