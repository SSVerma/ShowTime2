package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.repository.CommentQuotaManager

class FakeCommentQuotaManager : CommentQuotaManager {
    var dailyCommentsCount: Int = 0
    var extraCommentSlots: Int = 0
    var freeDailyLimit: Int = 3
    var grantCommentPassCallCount: Int = 0
    var consumeCommentPassCallCount: Int = 0
    var recordCommentPostedCallCount: Int = 0

    override suspend fun canPostComment(isProActive: Boolean): Boolean {
        if (isProActive) return true
        if (dailyCommentsCount < freeDailyLimit) return true
        return extraCommentSlots > 0
    }

    override suspend fun recordCommentPosted() {
        recordCommentPostedCallCount++
        if (dailyCommentsCount < freeDailyLimit) {
            dailyCommentsCount++
        } else if (extraCommentSlots > 0) {
            extraCommentSlots--
        }
    }

    override suspend fun grantCommentPass() {
        grantCommentPassCallCount++
        extraCommentSlots += 3
    }

    override suspend fun consumeCommentPass(): Boolean {
        consumeCommentPassCallCount++
        if (extraCommentSlots > 0) {
            extraCommentSlots--
            return true
        }
        return false
    }
}
