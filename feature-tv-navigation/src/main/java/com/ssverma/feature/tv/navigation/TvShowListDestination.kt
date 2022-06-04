package com.ssverma.feature.tv.navigation

import androidx.navigation.NavType
import com.ssverma.core.navigation.ActualRoute
import com.ssverma.core.navigation.DependentDestination
import com.ssverma.core.navigation.PlaceholderRoute
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs

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