package com.ssverma.feature.movie.navigation

import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute
import com.ssverma.feature.movie.navigation.args.MovieListingArgs

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