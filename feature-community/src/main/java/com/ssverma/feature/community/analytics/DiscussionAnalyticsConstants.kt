package com.ssverma.feature.community.analytics

object DiscussionAnalyticsKeys {
    const val TARGET_TYPE = "target_type"
    const val TARGET_ID = "target_id"
    const val IS_SPOILER = "is_spoiler"
    const val IS_REPLY = "is_reply"
    const val COMMENT_ID = "comment_id"
    const val REASON = "reason"
    const val FILTER = "filter"
}

object DiscussionAnalyticsEventName {
    const val COMMENT_POSTED = "discussion_comment_posted"
    const val COMMENT_UPVOTED = "discussion_comment_upvoted"
    const val COMMENT_REPORTED = "discussion_comment_reported"
    const val FILTER_SELECTED = "discussion_filter_selected"
}
