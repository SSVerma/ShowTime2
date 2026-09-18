package com.ssverma.shared.domain.repository

interface CommentQuotaManager {
    suspend fun canPostComment(isProActive: Boolean): Boolean
    suspend fun recordCommentPosted()
    suspend fun grantCommentPass()
    suspend fun consumeCommentPass(): Boolean
}
