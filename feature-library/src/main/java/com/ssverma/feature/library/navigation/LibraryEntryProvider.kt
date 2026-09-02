package com.ssverma.feature.library.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.account.navigation.BackupSyncNavKey
import com.ssverma.feature.library.ui.diary.CinemaDiaryScreen
import com.ssverma.feature.library.ui.home.LibraryScreen
import com.ssverma.feature.library.ui.receipt.CinemaReceiptScreen
import com.ssverma.feature.library.ui.taste.TasteProfileScreen
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.payment.navigation.ProPaywallNavKey
import com.ssverma.feature.search.navigation.SearchNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

fun EntryProviderScope<NavKey>.libraryEntries(
    navigator: Navigator
) {
    showTimeEntry<LibraryHomeNavKey> { navKey ->
        LibraryScreen(
            onBackPressed = null,
            isTopLevel = true,
            onMovieClicked = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            onTvShowClicked = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            openSearchPage = {
                navigator.navigate(SearchNavKey)
            },
            onNavigateToProPaywall = {
                navigator.navigate(ProPaywallNavKey)
            },
            onOpenBackup = {
                navigator.navigate(BackupSyncNavKey)
            },
            onOpenDiary = {
                navigator.navigate(CinemaDiaryNavKey)
            },
            onOpenTasteProfile = {
                navigator.navigate(TasteProfileNavKey)
            },
            initialTab = navKey.initialTab,
            initialMediaType = navKey.initialMediaType,
            targetCustomListId = navKey.targetCustomListId
        )
    }

    showTimeEntry<CinemaReceiptNavKey> {
        CinemaReceiptScreen(
            onBackPressed = {
                navigator.goBack()
            }
        )
    }

    showTimeEntry<CinemaDiaryNavKey> {
        CinemaDiaryScreen(
            onBackClick = {
                navigator.goBack()
            },
            onOpenMovieDetails = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            onOpenTvShowDetails = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            onOpenTasteProfile = {
                navigator.navigate(TasteProfileNavKey)
            }
        )
    }

    showTimeEntry<TasteProfileNavKey> {
        TasteProfileScreen(
            onBackClick = {
                navigator.goBack()
            },
            onOpenMovieDetails = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            onOpenTvShowDetails = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            }
        )
    }
}
