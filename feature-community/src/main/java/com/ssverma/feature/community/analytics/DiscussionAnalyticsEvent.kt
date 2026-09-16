package com.ssverma.feature.community.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.analytics.SharedAnalyticsKeys

sealed class DiscussionAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap()
) : AnalyticsEvent {

    data class CommentPosted(
        val targetType: String,
        val targetId: Int,
        val isSpoiler: Boolean,
        val isReply: Boolean,
        val sourceScreen: String = CommunityAnalyticsScreenName.COMMUNITY_DISCUSSIONS
    ) : DiscussionAnalyticsEvent(
        eventName = DiscussionAnalyticsEventName.COMMENT_POSTED,
        params = mapOf(
            DiscussionAnalyticsKeys.TARGET_TYPE to targetType,
            DiscussionAnalyticsKeys.TARGET_ID to targetId,
            DiscussionAnalyticsKeys.IS_SPOILER to isSpoiler,
            DiscussionAnalyticsKeys.IS_REPLY to isReply,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class CommentUpvoted(
        val targetType: String,
        val targetId: Int,
        val commentId: String,
        val sourceScreen: String = CommunityAnalyticsScreenName.COMMUNITY_DISCUSSIONS
    ) : DiscussionAnalyticsEvent(
        eventName = DiscussionAnalyticsEventName.COMMENT_UPVOTED,
        params = mapOf(
            DiscussionAnalyticsKeys.TARGET_TYPE to targetType,
            DiscussionAnalyticsKeys.TARGET_ID to targetId,
            DiscussionAnalyticsKeys.COMMENT_ID to commentId,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class CommentReported(
        val targetType: String,
        val targetId: Int,
        val commentId: String,
        val reason: String,
        val sourceScreen: String = CommunityAnalyticsScreenName.COMMUNITY_DISCUSSIONS
    ) : DiscussionAnalyticsEvent(
        eventName = DiscussionAnalyticsEventName.COMMENT_REPORTED,
        params = mapOf(
            DiscussionAnalyticsKeys.TARGET_TYPE to targetType,
            DiscussionAnalyticsKeys.TARGET_ID to targetId,
            DiscussionAnalyticsKeys.COMMENT_ID to commentId,
            DiscussionAnalyticsKeys.REASON to reason,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )

    data class FilterSelected(
        val filter: String,
        val sourceScreen: String = CommunityAnalyticsScreenName.COMMUNITY_DISCUSSIONS
    ) : DiscussionAnalyticsEvent(
        eventName = DiscussionAnalyticsEventName.FILTER_SELECTED,
        params = mapOf(
            DiscussionAnalyticsKeys.FILTER to filter,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen
        )
    )
}
