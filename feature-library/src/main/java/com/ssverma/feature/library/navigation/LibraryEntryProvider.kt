package com.ssverma.feature.library.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.account.navigation.ProfileNavKey
import com.ssverma.feature.library.ui.home.LibraryScreen
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

fun EntryProviderScope<NavKey>.libraryEntries(
    navigator: Navigator
) {
    showTimeEntry<LibraryHomeNavKey> { navKey ->
        val isTopLevel = navKey == LibraryHomeNavKey.Default
        LibraryScreen(
            onBackPressed = if (isTopLevel) null else {
                { navigator.goBack() }
            },
            isTopLevel = isTopLevel,
            onMovieClicked = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            onTvShowClicked = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            openSearchPage = {
                navigator.navigate(SearchNavKey)
            },
            openAccountPage = {
                navigator.navigate(ProfileNavKey)
            },
            initialTab = navKey.initialTab,
            initialMediaType = navKey.initialMediaType,
            targetCustomListId = navKey.targetCustomListId
        )
    }

    showTimeEntry<CinemaReceiptNavKey> {
        com.ssverma.feature.library.ui.receipt.CinemaReceiptScreen(
            onBackPressed = {
                navigator.goBack()
            }
        )
    }
}
