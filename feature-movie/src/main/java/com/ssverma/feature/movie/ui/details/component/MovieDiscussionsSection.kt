package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.common.ui.community.MediaDiscussionsSection
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.EditCommentArgs
import com.ssverma.shared.domain.model.community.PostCommentArgs
import com.ssverma.shared.domain.model.community.ReportCommentArgs

fun LazyListScope.movieDiscussionsSection(
    discussions: List<Comment>,
    onDiscussionsViewAllClick: () -> Unit,
    onPostComment: (PostCommentArgs) -> Unit,
    onEditComment: (EditCommentArgs) -> Unit,
    onReportComment: (ReportCommentArgs) -> Unit,
    onToggleUpvote: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    item(key = "movie_discussions", contentType = "discussions") {
        MediaDiscussionsSection(
            discussions = discussions,
            onDiscussionsViewAllClick = onDiscussionsViewAllClick,
            onPostComment = onPostComment,
            onEditComment = onEditComment,
            onReportComment = onReportComment,
            onToggleUpvote = onToggleUpvote,
            onDeleteComment = onDeleteComment,
            modifier = modifier
        )
    }
}
