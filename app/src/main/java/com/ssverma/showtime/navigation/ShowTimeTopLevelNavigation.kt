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
    object Movie : ShowTimeTopLevelNavItem(
        navKey = MovieHomeNavKey,
        titleResId = R.string.movie,
        iconResId = R.drawable.ic_movie
    )

    object Tv : ShowTimeTopLevelNavItem(
        navKey = TvShowHomeNavKey,
        titleResId = R.string.tv,
        iconResId = R.drawable.ic_tv
    )

    object Person : ShowTimeTopLevelNavItem(
        navKey = PersonHomeNavKey,
        titleResId = R.string.people,
        iconResId = R.drawable.ic_people
    )

    object Library : ShowTimeTopLevelNavItem(
        navKey = LibraryHomeNavKey,
        titleResId = R.string.library,
        iconResId = R.drawable.ic_library
    )
}

val ShowTimeTopLevelNavItems = listOf(
    ShowTimeTopLevelNavItem.Movie,
    ShowTimeTopLevelNavItem.Tv,
    ShowTimeTopLevelNavItem.Person,
)
