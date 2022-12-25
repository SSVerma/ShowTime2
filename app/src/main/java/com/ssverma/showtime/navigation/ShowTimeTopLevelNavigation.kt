package com.ssverma.showtime.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.ssverma.core.navigation.Destination
import com.ssverma.core.navigation.GraphDestination
import com.ssverma.core.navigation.navigation
import com.ssverma.feature.library.navigation.LibraryHomeDestination
import com.ssverma.feature.library.navigation.libraryHomeGraph
import com.ssverma.feature.movie.navigation.MovieHomeDestination
import com.ssverma.feature.movie.navigation.movieHomeGraph
import com.ssverma.feature.person.navigation.PersonHomeDestination
import com.ssverma.feature.person.navigation.personHomeGraph
import com.ssverma.feature.tv.navigation.TvShowHomeDestination
import com.ssverma.feature.tv.navigation.tvShowHomeGraph
import com.ssverma.showtime.R

sealed class ShowTimeTopLevelNavItem(
    val destination: Destination,
    @StringRes val titleResId: Int,
    @DrawableRes val iconResId: Int
) {
    object Movie : ShowTimeTopLevelNavItem(
        destination = MovieHomeDestination,
        titleResId = R.string.movie,
        iconResId = R.drawable.ic_movie
    )

    object Tv : ShowTimeTopLevelNavItem(
        destination = TvShowHomeDestination,
        titleResId = R.string.tv_show,
        iconResId = R.drawable.ic_tv
    )

    object Person : ShowTimeTopLevelNavItem(
        destination = PersonHomeDestination,
        titleResId = R.string.people,
        iconResId = R.drawable.ic_people
    )

    object Library : ShowTimeTopLevelNavItem(
        destination = LibraryHomeDestination,
        titleResId = R.string.library,
        iconResId = R.drawable.ic_library
    )
}

val ShowTimeTopLevelNavItems = listOf(
    ShowTimeTopLevelNavItem.Movie,
    ShowTimeTopLevelNavItem.Tv,
    ShowTimeTopLevelNavItem.Person,
    ShowTimeTopLevelNavItem.Library,
)

object ShowTimeTopLevelDestination : GraphDestination("home")

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.topLevelNavGraph(
    navController: NavController
) = navigation(
    graphDestination = ShowTimeTopLevelDestination,
    startDestination = MovieHomeDestination
) {
    movieHomeGraph(navController)
    tvShowHomeGraph(navController)
    personHomeGraph(navController)
    libraryHomeGraph(navController)
}