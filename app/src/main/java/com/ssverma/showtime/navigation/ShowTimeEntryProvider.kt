package com.ssverma.showtime.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.feature.account.navigation.accountEntries
import com.ssverma.feature.auth.navigation.authEntries
import com.ssverma.feature.filter.navigation.filterEntries
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.library.navigation.libraryEntries
import com.ssverma.feature.movie.navigation.movieEntries
import com.ssverma.feature.payment.navigation.paymentEntries
import com.ssverma.feature.person.navigation.personEntries
import com.ssverma.feature.search.navigation.searchEntries
import com.ssverma.feature.tv.navigation.tvEntries

@Composable
fun rememberShowTimeEntryProvider(
    navigator: Navigator,
    openLibraryPage: (LibraryHomeNavKey) -> Unit
): (NavKey) -> NavEntry<NavKey> {
    return remember(navigator, openLibraryPage) {
        entryProvider {
            dashboardEntries(navigator, openLibraryPage)
            movieEntries(navigator, openLibraryPage)
            tvEntries(navigator, openLibraryPage)
            personEntries(navigator, openLibraryPage)
            libraryEntries(navigator)
            searchEntries(navigator)
            authEntries(navigator)
            accountEntries(navigator)
            paymentEntries(navigator)
            filterEntries(navigator)
        }
    }
}
