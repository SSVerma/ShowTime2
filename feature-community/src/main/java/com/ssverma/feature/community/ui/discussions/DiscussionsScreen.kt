package com.ssverma.feature.community.ui.discussions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ssverma.common.ui.community.DiscussionsScreenContent
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.feature.community.analytics.CommunityAnalyticsScreenName

@Composable
fun DiscussionsScreen(
    viewModel: DiscussionsViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrackScreenView(screenName = CommunityAnalyticsScreenName.COMMUNITY_DISCUSSIONS)

    val comments by viewModel.uiComments.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    DiscussionsScreenContent(
        comments = comments,
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::onFilterSelected,
        mediaTitle = viewModel.title,
        posterImageUrl = viewModel.poster,
        onBackPressed = onBackPressed,
        onPostComment = viewModel::postComment,
        onToggleUpvote = viewModel::toggleCommentUpvote,
        onEditComment = viewModel::editComment,
        onReportComment = viewModel::reportComment,
        onDeleteComment = viewModel::deleteComment,
        modifier = modifier
    )
}
