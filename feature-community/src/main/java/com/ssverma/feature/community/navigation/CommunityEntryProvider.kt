package com.ssverma.feature.community.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.community.ui.discussions.DiscussionsScreen
import com.ssverma.feature.community.ui.discussions.DiscussionsViewModel
import com.ssverma.feature.movie.navigation.MovieDetailNavKey
import com.ssverma.feature.tv.navigation.TvEpisodeDetailNavKey
import com.ssverma.feature.tv.navigation.TvShowDetailNavKey

fun EntryProviderScope<NavKey>.communityEntries(
    navigator: Navigator,
) {
    showTimeEntry<CommunityDiscussionsNavKey> { key ->
        DiscussionsScreen(
            viewModel = hiltViewModel<DiscussionsViewModel, DiscussionsViewModel.Factory> { factory ->
                factory.create(
                    discussionTarget = key.toDiscussionTarget(),
                    mediaTitle = key.mediaTitle,
                    posterImageUrl = key.posterImageUrl,
                    backdropImageUrl = key.backdropImageUrl
                )
            },
            onBackPressed = { navigator.goBack() },
            onMediaClick = {
                val targetNavKey: NavKey = when (val target = key.target) {
                    is DiscussionNavTarget.Movie -> {
                        MovieDetailNavKey(movieId = target.movieId)
                    }

                    is DiscussionNavTarget.TvShow -> {
                        TvShowDetailNavKey(tvShowId = target.tvShowId)
                    }

                    is DiscussionNavTarget.TvEpisode -> {
                        TvEpisodeDetailNavKey(
                            tvShowId = target.tvShowId,
                            seasonNumber = target.seasonNumber,
                            episodeNumber = target.episodeNumber,
                            tvShowTitle = key.mediaTitle,
                            tvShowPosterPath = key.posterImageUrl,
                            tvShowBackdropPath = key.backdropImageUrl,
                        )
                    }
                }

                val currentStack = navigator.state.backStacks[navigator.state.topLevelRoute]
                val previousKey = currentStack?.getOrNull(currentStack.size - 2)

                if (previousKey == targetNavKey) {
                    navigator.goBack()
                } else {
                    navigator.navigate(targetNavKey)
                }
            }
        )
    }
}
