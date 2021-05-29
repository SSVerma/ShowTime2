package com.ssverma.showtime.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.AppDestinations
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.common.AppIcons

sealed class BottomNavScreen(
    val route: String,
    @StringRes val tabTitleStringRes: Int,
    val tabIcon: ImageVector
) {
    object Movie : BottomNavScreen(
        route = AppDestinations.HomeBottomNavDestinations.MovieRoute,
        tabTitleStringRes = R.string.movie,
        tabIcon = AppIcons.Home
    )

    object Tv : BottomNavScreen(
        route = AppDestinations.HomeBottomNavDestinations.TvShowRoute,
        tabTitleStringRes = R.string.tv_show,
        tabIcon = AppIcons.Face
    )

    object People : BottomNavScreen(
        route = AppDestinations.HomeBottomNavDestinations.PeopleRoute,
        tabTitleStringRes = R.string.people,
        tabIcon = AppIcons.Person
    )

    object Library : BottomNavScreen(
        route = AppDestinations.HomeBottomNavDestinations.LibraryRoute,
        tabTitleStringRes = R.string.library,
        tabIcon = AppIcons.Star
    )
}

val homeBottomNavScreens = listOf(
    BottomNavScreen.Movie,
    BottomNavScreen.Tv,
    BottomNavScreen.People,
    BottomNavScreen.Library,
)

@Composable
fun HomePageAppBar(modifier: Modifier = Modifier) {
    TopAppBar(
        elevation = 0.dp,
        backgroundColor = Color.Transparent,
        modifier = modifier.height(56.dp),
        contentColor = Color.White //Always blackish tint on background
    ) {
        Image(
            modifier = Modifier
                .padding(8.dp)
                .size(32.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null
        )
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = { /* todo */ }
        ) {
            Icon(
                imageVector = AppIcons.Search,
                contentDescription = stringResource(R.string.search)
            )
        }
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = { /* todo */ }
        ) {
            Icon(
                imageVector = AppIcons.AccountCircle,
                contentDescription = stringResource(R.string.account)
            )
        }
    }
}