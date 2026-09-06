package com.ssverma.shared.ui.component.community

import android.content.Context
import androidx.compose.runtime.Immutable
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.CommunityModerationConfig

@Immutable
data class CommentUiModel(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val avatarInitial: String,
    val displayRelativeTime: String,
    val content: String,
    val isSpoiler: Boolean,
    val isFlagged: Boolean = false,
    val upvotesCount: Int,
    val displayUpvotesCount: String,
    val isUpvotedByMe: Boolean,
    val isOwner: Boolean,
    val isEdited: Boolean,
    val parentId: String?,
    val replyToAuthorName: String?,
    val repliesCount: Int,
    val replies: List<CommentUiModel>,
    val createdAtEpochMs: Long
)

fun Comment.toUiModel(
    context: Context,
    currentUserId: String? = null,
    flagThreshold: Int = CommunityModerationConfig.DEFAULT_FLAG_REPORT_THRESHOLD
): CommentUiModel {
    return toUiModel(
        stringResolver = { resId, args ->
            if (args.isEmpty()) context.getString(resId)
            else context.getString(resId, *args)
        },
        currentUserId = currentUserId,
        flagThreshold = flagThreshold
    )
}

fun Comment.toUiModel(
    stringResolver: (resId: Int, args: Array<out Any>) -> String,
    currentUserId: String? = null,
    flagThreshold: Int = CommunityModerationConfig.DEFAULT_FLAG_REPORT_THRESHOLD
): CommentUiModel {
    val initial = authorName.firstOrNull()?.uppercase() ?: "?"
    val formattedTime = RelativeTimeFormatter.format(
        stringResolver = stringResolver,
        epochMs = createdAtEpochMs
    )
    val upvotesStr = if (upvotesCount > 0) upvotesCount.toString() else ""
    val isMyComment = isOwner || (currentUserId != null && authorId == currentUserId)

    // Natural conversation flow (YouTube & Twitter style): nested replies ordered chronologically from oldest to newest
    val sortedReplies = replies.sortedBy { it.createdAtEpochMs }

    return CommentUiModel(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        avatarInitial = initial,
        displayRelativeTime = formattedTime,
        content = content,
        isSpoiler = isSpoiler,
        isFlagged = reportCount >= flagThreshold,
        upvotesCount = upvotesCount,
        displayUpvotesCount = upvotesStr,
        isUpvotedByMe = isUpvotedByMe,
        isOwner = isMyComment,
        isEdited = isEdited,
        parentId = parentId,
        replyToAuthorName = replyToAuthorName,
        repliesCount = if (repliesCount > 0) repliesCount else replies.size,
        replies = sortedReplies.map {
            it.toUiModel(
                stringResolver = stringResolver,
                currentUserId = currentUserId,
                flagThreshold = flagThreshold
            )
        },
        createdAtEpochMs = createdAtEpochMs
    )
}
