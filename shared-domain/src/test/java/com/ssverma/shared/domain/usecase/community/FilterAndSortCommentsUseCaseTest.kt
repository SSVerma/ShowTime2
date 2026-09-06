package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.ThreadFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class FilterAndSortCommentsUseCaseTest {

    private lateinit var useCase: FilterAndSortCommentsUseCase

    @Before
    fun setUp() {
        useCase = FilterAndSortCommentsUseCase()
    }

    private fun createSampleComment(
        id: String,
        isSpoiler: Boolean = false,
        upvotesCount: Int = 0,
        createdAtEpochMs: Long = 1000L,
        replies: List<Comment> = emptyList()
    ): Comment {
        return Comment(
            id = id,
            authorId = "author_$id",
            authorName = "User $id",
            content = "Comment content $id",
            isSpoiler = isSpoiler,
            upvotesCount = upvotesCount,
            createdAtEpochMs = createdAtEpochMs,
            replies = replies
        )
    }

    @Test
    fun `invoke with ALL filter orders roots newest-first and replies oldest-first`() {
        val replyOld = createSampleComment(id = "r_old", createdAtEpochMs = 100L)
        val replyNew = createSampleComment(id = "r_new", createdAtEpochMs = 200L)
        val rootOlder = createSampleComment(
            id = "root_older",
            createdAtEpochMs = 500L,
            replies = listOf(replyNew, replyOld)
        )
        val rootNewer = createSampleComment(id = "root_newer", createdAtEpochMs = 1500L)

        val result = useCase(
            comments = listOf(rootOlder, rootNewer),
            filter = ThreadFilter.ALL
        )

        assertEquals(2, result.size)
        assertEquals("root_newer", result[0].id)
        assertEquals("root_older", result[1].id)

        // Nested replies follow natural conversation order (oldest to newest)
        assertEquals("r_old", result[1].replies[0].id)
        assertEquals("r_new", result[1].replies[1].id)
    }

    @Test
    fun `invoke with SPOILER_FREE filter excludes spoiler roots`() {
        val normalComment = createSampleComment(id = "c1", isSpoiler = false)
        val spoilerComment = createSampleComment(id = "c2", isSpoiler = true)

        val result = useCase(
            comments = listOf(normalComment, spoilerComment),
            filter = ThreadFilter.SPOILER_FREE
        )

        assertEquals(1, result.size)
        assertEquals("c1", result[0].id)
        assertFalse(result[0].isSpoiler)
    }

    @Test
    fun `invoke with TOP_UPVOTED filter orders roots and replies by upvotes descending`() {
        val replyLow = createSampleComment(id = "r_low", upvotesCount = 1, createdAtEpochMs = 200L)
        val replyHigh =
            createSampleComment(id = "r_high", upvotesCount = 15, createdAtEpochMs = 100L)
        val rootLowVotes = createSampleComment(
            id = "c_low",
            upvotesCount = 2,
            replies = listOf(replyLow, replyHigh)
        )
        val rootHighVotes = createSampleComment(id = "c_high", upvotesCount = 20)

        val result = useCase(
            comments = listOf(rootLowVotes, rootHighVotes),
            filter = ThreadFilter.TOP_UPVOTED
        )

        assertEquals(2, result.size)
        assertEquals("c_high", result[0].id)
        assertEquals("c_low", result[1].id)

        // Replies also sorted by top upvotes
        assertEquals("r_high", result[1].replies[0].id)
        assertEquals("r_low", result[1].replies[1].id)
    }

    @Test
    fun `invoke with excludedCommentIds removes reported roots and replies`() {
        val replyReported = createSampleComment(id = "r_reported")
        val replyGood = createSampleComment(id = "r_good")
        val rootReported = createSampleComment(id = "root_reported")
        val rootGood =
            createSampleComment(id = "root_good", replies = listOf(replyReported, replyGood))

        val result = useCase(
            comments = listOf(rootReported, rootGood),
            filter = ThreadFilter.ALL,
            excludedCommentIds = setOf("root_reported", "r_reported")
        )

        assertEquals(1, result.size)
        assertEquals("root_good", result[0].id)
        assertEquals(1, result[0].replies.size)
        assertEquals("r_good", result[0].replies[0].id)
    }
}
