package com.ssverma.feature.match.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.match.ui.MovieMatchRoomScreen
import com.ssverma.feature.match.ui.MovieMatchRoomViewModel
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.payment.navigation.ProPaywallNavKey

fun EntryProviderScope<NavKey>.matchEntries(navigator: Navigator) {
    showTimeEntry<MatchRoomNavKey> { key ->
        val viewModel = hiltViewModel<MovieMatchRoomViewModel>()
        LaunchedEffect(key) {
            viewModel.initFromNavKey(key.roomCode, key.initialMode)
        }
        MovieMatchRoomScreen(
            viewModel = viewModel,
            onBackPressed = { navigator.goBack() },
            openMovieDetails = { movieId ->
                navigator.navigate(MovieDetailNavKey(movieId))
            },
            openProPaywall = {
                navigator.navigate(ProPaywallNavKey)
            }
        )
    }
}
