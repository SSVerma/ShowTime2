package com.ssverma.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.adaptiveListPaneMetadata
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.person.navigation.PersonDetailNavKey
import com.ssverma.feature.search.ui.suggestion.SearchSuggestionScreen
import com.ssverma.feature.search.ui.suggestion.component.SearchDetailPlaceholder
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

fun EntryProviderScope<NavKey>.searchEntries(
    navigator: Navigator
) {
    showTimeEntry<SearchNavKey>(
        metadata = {
            adaptiveListPaneMetadata(
                alwaysShowSplit = true,
                detailPlaceholder = { SearchDetailPlaceholder() }
            )
        }
    ) {
        SearchSuggestionScreen(
            onMovieClick = { movieId ->
                navigator.navigateFromListToDetail(MovieDetailNavKey(movieId))
            },
            onTvShowClick = { tvShowId ->
                navigator.navigateFromListToDetail(TvShowDetailNavKey(tvShowId))
            },
            onPersonClick = { personId, personName, personImageUrl ->
                navigator.navigateFromListToDetail(
                    PersonDetailNavKey(
                        personId = personId,
                        personName = personName,
                        personImageUrl = personImageUrl,
                        source = "search"
                    )
                )
            },
            onBackPressed = {
                navigator.navigateUpFromListPane()
            }
        )
    }
}
