package com.ssverma.feature.tv.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ssverma.shared.ui.component.community.DiscussionsScreenContent

@Composable
fun TvShowDiscussionsScreen(
    viewModel: TvShowDiscussionsViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
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

@Composable
fun TvEpisodeDiscussionsScreen(
    viewModel: TvEpisodeDiscussionsViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
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
