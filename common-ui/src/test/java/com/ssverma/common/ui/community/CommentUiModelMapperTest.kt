package com.ssverma.common.ui.community

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.ui.R
import org.junit.Test
import java.util.concurrent.TimeUnit

class CommentUiModelMapperTest {

    private val testStringResolver: (Int, Array<out Any>) -> String = { resId, args ->
        when (resId) {
            R.string.time_just_now -> "Just now"
            R.string.time_minutes_ago -> "${args[0]}m"
            R.string.time_hours_ago -> "${args[0]}h"
            R.string.time_days_ago -> "${args[0]}d"
            R.string.time_weeks_ago -> "${args[0]}w"
            else -> ""
        }
    }

    @Test
    fun toUiModel_mapsAuthorInitial_ownership_andUpvoteStringsCorrectly() {
        val rootComment = Comment(
            id = "comment_1",
            authorId = "user_42",
            authorName = "Cinephile Alex",
            content = "Incredible direction and cinematography!",
            isSpoiler = false,
            upvotesCount = 12,
            isUpvotedByMe = true,
            isOwner = false,
            repliesCount = 1,
            replies = listOf(
                Comment(
                    id = "reply_1",
                    authorId = "user_99",
                    authorName = "Nolan Fan",
                    content = "Agreed, especially the IMAX sequence.",
                    parentId = "comment_1",
                    replyToAuthorName = "Cinephile Alex",
                    upvotesCount = 0
                )
            ),
            createdAtEpochMs = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)
        )

        val uiModel =
            rootComment.toUiModel(stringResolver = testStringResolver, currentUserId = "user_42")

        assertThat(uiModel.avatarInitial).isEqualTo("C")
        assertThat(uiModel.isOwner).isTrue()
        assertThat(uiModel.displayUpvotesCount).isEqualTo("12")
        assertThat(uiModel.isUpvotedByMe).isTrue()
        assertThat(uiModel.replies).hasSize(1)

        val replyUi = uiModel.replies.first()
        assertThat(replyUi.avatarInitial).isEqualTo("N")
        assertThat(replyUi.isOwner).isFalse()
        assertThat(replyUi.displayUpvotesCount).isEmpty()
        assertThat(replyUi.replyToAuthorName).isEqualTo("Cinephile Alex")
    }

    @Test
    fun relativeTimeFormatting_resolvesTimeBoundariesAccurately() {
        val now = 1000000000000L

        // Just now (< 1 min)
        assertThat(
            RelativeTimeFormatter.format(
                stringResolver = testStringResolver,
                epochMs = now - TimeUnit.SECONDS.toMillis(30),
                nowMs = now
            )
        ).isEqualTo("Just now")

        // 15 minutes ago
        assertThat(
            RelativeTimeFormatter.format(
                stringResolver = testStringResolver,
                epochMs = now - TimeUnit.MINUTES.toMillis(15),
                nowMs = now
            )
        ).isEqualTo("15m")

        // 4 hours ago
        assertThat(
            RelativeTimeFormatter.format(
                stringResolver = testStringResolver,
                epochMs = now - TimeUnit.HOURS.toMillis(4),
                nowMs = now
            )
        ).isEqualTo("4h")

        // 3 days ago
        assertThat(
            RelativeTimeFormatter.format(
                stringResolver = testStringResolver,
                epochMs = now - TimeUnit.DAYS.toMillis(3),
                nowMs = now
            )
        ).isEqualTo("3d")

        // 2 weeks ago
        assertThat(
            RelativeTimeFormatter.format(
                stringResolver = testStringResolver,
                epochMs = now - TimeUnit.DAYS.toMillis(14),
                nowMs = now
            )
        ).isEqualTo("2w")
    }

    @Test
    fun toUiModel_sortsRepliesChronologicallyFromOldestToNewest() {
        val baseTime = 1000000000000L
        val rootComment = Comment(
            id = "root_1",
            authorId = "user_1",
            authorName = "Alex",
            content = "Root thought",
            replies = listOf(
                Comment(
                    id = "reply_latest",
                    authorId = "user_3",
                    authorName = "Charlie",
                    content = "Posted 5m ago",
                    parentId = "root_1",
                    createdAtEpochMs = baseTime + 5000L
                ),
                Comment(
                    id = "reply_earliest",
                    authorId = "user_2",
                    authorName = "Bob",
                    content = "Posted 10m ago",
                    parentId = "root_1",
                    createdAtEpochMs = baseTime + 1000L
                )
            ),
            createdAtEpochMs = baseTime
        )

        val uiModel = rootComment.toUiModel(stringResolver = testStringResolver)

        assertThat(uiModel.replies).hasSize(2)
        // Natural flow: earliest reply at top, latest reply appended at bottom
        assertThat(uiModel.replies[0].id).isEqualTo("reply_earliest")
        assertThat(uiModel.replies[1].id).isEqualTo("reply_latest")
    }

    @Test
    fun toUiModel_whenReportCountMeetsThreshold_flagsComment() {
        val normalComment = Comment(
            id = "normal_1",
            authorId = "user_1",
            authorName = "Alex",
            content = "Great movie!",
            reportCount = 2
        )
        val flaggedComment = Comment(
            id = "flagged_1",
            authorId = "user_2",
            authorName = "Spammer",
            content = "Spam content",
            reportCount = 3
        )

        val normalUi = normalComment.toUiModel(stringResolver = testStringResolver)
        val flaggedUi = flaggedComment.toUiModel(stringResolver = testStringResolver)

        assertThat(normalUi.isFlagged).isFalse()
        assertThat(flaggedUi.isFlagged).isTrue()
    }
}
