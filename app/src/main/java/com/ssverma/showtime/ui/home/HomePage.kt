package com.ssverma.showtime.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.ssverma.showtime.AppDestinations
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.common.AppIcons
import com.ssverma.showtime.ui.common.Chip
import com.ssverma.showtime.ui.library.LibraryScreen
import com.ssverma.showtime.ui.movie.MovieScreen
import com.ssverma.showtime.ui.people.PeopleScreen
import com.ssverma.showtime.ui.tv.TvShowScreen
import dev.chrisbanes.accompanist.insets.navigationBarsHeight
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

sealed class BottomNavScreen(
    val route: String,
    @StringRes val tabTitleStringRes: Int,
    val tabIcon: ImageVector
) {
    object Home : BottomNavScreen(
        route = AppDestinations.BottomNavDestinations.MOVIE_ROUTE,
        tabTitleStringRes = R.string.movie,
        tabIcon = AppIcons.Home
    )

    object Explore : BottomNavScreen(
        route = AppDestinations.BottomNavDestinations.TV_SHOW_ROUTE,
        tabTitleStringRes = R.string.tv_show,
        tabIcon = AppIcons.Face
    )

    object People : BottomNavScreen(
        route = AppDestinations.BottomNavDestinations.PEOPLE_ROUTE,
        tabTitleStringRes = R.string.people,
        tabIcon = AppIcons.Person
    )

    object Library : BottomNavScreen(
        route = AppDestinations.BottomNavDestinations.LIBRARY_ROUTE,
        tabTitleStringRes = R.string.library,
        tabIcon = AppIcons.Star
    )
}

private val bottomTabs = listOf(
    BottomNavScreen.Home,
    BottomNavScreen.Explore,
    BottomNavScreen.People,
    BottomNavScreen.Library,
)

@Composable
fun HomePage(viewModel: HomeViewModel) {
    val navController = rememberNavController()

    Scaffold(
        backgroundColor = MaterialTheme.colors.surface,
        bottomBar = {
            BottomNavigation(
                modifier = Modifier.navigationBarsHeight(additional = 56.dp),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.arguments?.getString(KEY_ROUTE)
                bottomTabs.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                imageVector = screen.tabIcon,
                                contentDescription = stringResource(id = screen.tabTitleStringRes)
                            )
                        },
                        label = { Text(text = stringResource(id = screen.tabTitleStringRes)) },
                        selected = currentRoute == screen.route,
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = LocalContentColor.current,
                        modifier = Modifier.navigationBarsPadding(),
                        onClick = {
                            navController.navigate(screen.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

    ) {
        NavHost(
            navController = navController,
            startDestination = AppDestinations.BottomNavDestinations.MOVIE_ROUTE
        ) {
            composable(AppDestinations.BottomNavDestinations.MOVIE_ROUTE) {
                MovieScreen(viewModel)
            }
            composable(AppDestinations.BottomNavDestinations.TV_SHOW_ROUTE) {
                TvShowScreen()
            }
            composable(AppDestinations.BottomNavDestinations.PEOPLE_ROUTE) {
                PeopleScreen()
            }
            composable(AppDestinations.BottomNavDestinations.LIBRARY_ROUTE) {
                LibraryScreen()
            }
        }
    }
}

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

@Composable
fun HomeCategories(modifier: Modifier = Modifier, viewModel: HomeViewModel) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = {
            items(viewModel.movieCategories) { category ->
                Chip(
                    text = stringResource(id = category.nameRes),
                    onClick = {
                        viewModel.onCategorySelected(movieCategory = category)
                    }
                )
            }
        }
    )
}