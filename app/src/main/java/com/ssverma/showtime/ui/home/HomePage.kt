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
import com.ssverma.showtime.R
import com.ssverma.showtime.navigation.AppDestination
import com.ssverma.showtime.navigation.StandaloneDestination
import com.ssverma.showtime.ui.common.AppIcons

sealed class HomeBottomNavItem(
    val linkedDestination: StandaloneDestination,
    @StringRes val tabTitleRes: Int,
    val tabIcon: ImageVector
) {
    object Movie : HomeBottomNavItem(
        linkedDestination = AppDestination.HomeBottomNavDestination.Movie,
        tabTitleRes = R.string.movie,
        tabIcon = AppIcons.Home
    )

    object Tv : HomeBottomNavItem(
        linkedDestination = AppDestination.HomeBottomNavDestination.Tv,
        tabTitleRes = R.string.tv_show,
        tabIcon = AppIcons.Face
    )

    object People : HomeBottomNavItem(
        linkedDestination = AppDestination.HomeBottomNavDestination.People,
        tabTitleRes = R.string.people,
        tabIcon = AppIcons.Person
    )

    object Library : HomeBottomNavItem(
        linkedDestination = AppDestination.HomeBottomNavDestination.Library,
        tabTitleRes = R.string.library,
        tabIcon = AppIcons.Star
    )
}

val homeBottomNavItems = listOf(
    HomeBottomNavItem.Movie,
    HomeBottomNavItem.Tv,
    HomeBottomNavItem.People,
    HomeBottomNavItem.Library,
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