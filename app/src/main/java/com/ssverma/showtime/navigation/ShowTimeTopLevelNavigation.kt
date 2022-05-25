package com.ssverma.showtime.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ssverma.core.navigation.Destination
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.library.navigation.LibraryHomeDestination
import com.ssverma.showtime.ui.movie.navigation.MovieHomeDestination
import com.ssverma.showtime.ui.people.navigation.PersonHomeDestination
import com.ssverma.showtime.ui.tv.navigation.TvShowHomeDestination

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