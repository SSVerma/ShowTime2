package com.ssverma.feature.community.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.navigation.nav3.Navigator
import com.ssverma.core.navigation.nav3.showTimeEntry
import com.ssverma.feature.community.ui.discussions.DiscussionsScreen
import com.ssverma.feature.community.ui.discussions.DiscussionsViewModel

fun EntryProviderScope<NavKey>.communityEntries(
    navigator: Navigator
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
            onBackPressed = { navigator.goBack() }
        )
    }
}
