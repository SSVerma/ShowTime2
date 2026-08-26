package com.ssverma.showtime.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.navigation.MovieHomeNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.feature.tv.navigation.TvShowHomeNavKey
import com.ssverma.showtime.R

sealed class ShowTimeTopLevelNavItem(
    val navKey: NavKey,
    @param:StringRes val titleResId: Int,
    @param:DrawableRes val iconResId: Int
) {
    object Home : ShowTimeTopLevelNavItem(
        navKey = DashboardHomeNavKey,
        titleResId = R.string.home,
        iconResId = R.drawable.ic_home
    )

    object Movie : ShowTimeTopLevelNavItem(
        navKey = MovieHomeNavKey,
        titleResId = R.string.movies,
        iconResId = R.drawable.ic_movie
    )

    object Tv : ShowTimeTopLevelNavItem(
        navKey = TvShowHomeNavKey,
        titleResId = R.string.tv,
        iconResId = R.drawable.ic_tv
    )

    object Library : ShowTimeTopLevelNavItem(
        navKey = LibraryHomeNavKey(),
        titleResId = R.string.library,
        iconResId = R.drawable.ic_library
    )

    object Person : ShowTimeTopLevelNavItem(
        navKey = PersonHomeNavKey,
        titleResId = R.string.people,
        iconResId = R.drawable.ic_people
    )
}

val ShowTimeTopLevelNavItems = listOf(
    ShowTimeTopLevelNavItem.Home,
    ShowTimeTopLevelNavItem.Movie,
    ShowTimeTopLevelNavItem.Tv,
    ShowTimeTopLevelNavItem.Library,
)

val ShowTimeRailNavItems = listOf(
    ShowTimeTopLevelNavItem.Home,
    ShowTimeTopLevelNavItem.Movie,
    ShowTimeTopLevelNavItem.Tv,
    ShowTimeTopLevelNavItem.Library,
    ShowTimeTopLevelNavItem.Person,
)
