package com.ssverma.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.person.navigation.PersonDetailNavKey
import com.ssverma.feature.search.ui.suggestion.SearchSuggestionScreen
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

fun EntryProviderScope<NavKey>.searchEntries(
    navigator: Navigator
) {
    showTimeEntry<SearchNavKey> {
        SearchSuggestionScreen(
            onMovieClick = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            onTvShowClick = { tvShowId ->
                navigator.navigate(TvShowDetailNavKey(tvShowId))
            },
            onPersonClick = { personId, personName, personImageUrl ->
                navigator.navigate(
                    PersonDetailNavKey(
                        personId = personId,
                        personName = personName,
                        personImageUrl = personImageUrl
                    )
                )
            },
            onBackPressed = {
                navigator.goBack()
            }
        )
    }
}
