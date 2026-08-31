package com.ssverma.shared.data.repository

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.TrendingDiscussion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscussionsTest {

    @Test
    fun comment_defaultValues_areCorrect() {
        val comment = Comment(
            id = "c1",
            authorId = "u1",
            authorName = "NolanFan",
            content = "The ending was mind-blowing!",
            isSpoiler = false,
            upvotesCount = 12,
            isUpvotedByMe = true
        )

        assertEquals("c1", comment.id)
        assertEquals("NolanFan", comment.authorName)
        assertFalse(comment.isSpoiler)
        assertEquals(12, comment.upvotesCount)
        assertTrue(comment.isUpvotedByMe)
    }

    @Test
    fun comment_spoilerFlag_identifiesSpoilers() {
        val spoilerComment = Comment(
            id = "c2",
            authorId = "u2",
            authorName = "MysteryWatcher",
            content = "The protagonist was actually dead the whole time.",
            isSpoiler = true
        )

        assertTrue(spoilerComment.isSpoiler)
    }

    @Test
    fun discussions_filterSpoilerFree_filtersCorrectly() {
        val comments = listOf(
            Comment(
                id = "1",
                authorId = "u1",
                authorName = "A",
                content = "Great visual effects!",
                isSpoiler = false
            ),
            Comment(
                id = "2",
                authorId = "u2",
                authorName = "B",
                content = "He dies at the end!",
                isSpoiler = true
            ),
            Comment(
                id = "3",
                authorId = "u3",
                authorName = "C",
                content = "Loved the soundtrack",
                isSpoiler = false
            )
        )

        val spoilerFree = comments.filter { !it.isSpoiler }
        assertEquals(2, spoilerFree.size)
        assertEquals("1", spoilerFree[0].id)
        assertEquals("3", spoilerFree[1].id)
    }

    @Test
    fun discussions_sortByTopUpvoted_sortsCorrectly() {
        val comments = listOf(
            Comment(
                id = "1",
                authorId = "u1",
                authorName = "A",
                content = "Good",
                upvotesCount = 5
            ),
            Comment(
                id = "2",
                authorId = "u2",
                authorName = "B",
                content = "Masterpiece",
                upvotesCount = 42
            ),
            Comment(id = "3", authorId = "u3", authorName = "C", content = "Okay", upvotesCount = 1)
        )

        val topUpvoted = comments.sortedByDescending { it.upvotesCount }
        assertEquals("2", topUpvoted[0].id)
        assertEquals(42, topUpvoted[0].upvotesCount)
        assertEquals("1", topUpvoted[1].id)
        assertEquals("3", topUpvoted[2].id)
    }

    @Test
    fun trendingDiscussion_properties_areAccurate() {
        val trending = TrendingDiscussion(
            mediaId = 693134,
            mediaType = MediaType.Movie,
            title = "Dune: Part Two",
            backdropImageUrl = "https://image.tmdb.org/t/p/w780/dune.jpg",
            posterImageUrl = "https://image.tmdb.org/t/p/w500/dune.jpg",
            discussionCount = 56,
            latestCommentSnippet = "The IMAX worm ride was legendary!"
        )

        assertEquals(693134, trending.mediaId)
        assertEquals(MediaType.Movie, trending.mediaType)
        assertEquals("Dune: Part Two", trending.title)
        assertEquals(56, trending.discussionCount)
    }

    @Test
    fun discussions_threadedReplies_areStructuredCorrectly() {
        val root = Comment(
            id = "root_1",
            authorId = "u1",
            authorName = "NolanFan",
            content = "The ending was unexpected!",
            repliesCount = 2,
            replies = listOf(
                Comment(
                    id = "rep_1",
                    authorId = "u2",
                    authorName = "CineBuff",
                    content = "Totally agree!",
                    parentId = "root_1",
                    replyToAuthorName = "NolanFan"
                ),
                Comment(
                    id = "rep_2",
                    authorId = "u3",
                    authorName = "MovieCritic",
                    content = "I saw it coming from scene 1.",
                    parentId = "root_1",
                    replyToAuthorName = "NolanFan"
                )
            )
        )

        assertEquals("root_1", root.id)
        assertEquals(2, root.replies.size)
        assertEquals("rep_1", root.replies[0].id)
        assertEquals("root_1", root.replies[0].parentId)
        assertEquals("NolanFan", root.replies[0].replyToAuthorName)
    }

    @Test
    fun upvoteToggle_unvotedToUpvoted_incrementsCountAndSetsFlag() {
        val initialComment = Comment(
            id = "c1",
            authorId = "u1",
            authorName = "A",
            content = "Hello",
            upvotesCount = 5,
            isUpvotedByMe = false
        )

        val newIsUpvoted = !initialComment.isUpvotedByMe
        val newCount =
            if (newIsUpvoted) initialComment.upvotesCount + 1 else (initialComment.upvotesCount - 1).coerceAtLeast(
                0
            )
        val updated = initialComment.copy(upvotesCount = newCount, isUpvotedByMe = newIsUpvoted)

        assertTrue(updated.isUpvotedByMe)
        assertEquals(6, updated.upvotesCount)
    }

    @Test
    fun upvoteToggle_upvotedToUnvoted_decrementsCountAndClearsFlag() {
        val initialComment = Comment(
            id = "c1",
            authorId = "u1",
            authorName = "A",
            content = "Hello",
            upvotesCount = 1,
            isUpvotedByMe = true
        )

        val newIsUpvoted = !initialComment.isUpvotedByMe
        val newCount =
            if (newIsUpvoted) initialComment.upvotesCount + 1 else (initialComment.upvotesCount - 1).coerceAtLeast(
                0
            )
        val updated = initialComment.copy(upvotesCount = newCount, isUpvotedByMe = newIsUpvoted)

        assertFalse(updated.isUpvotedByMe)
        assertEquals(0, updated.upvotesCount)
    }

    @Test
    fun upvoteToggle_zeroCount_doesNotGoNegative() {
        val initialComment = Comment(
            id = "c1",
            authorId = "u1",
            authorName = "A",
            content = "Hello",
            upvotesCount = 0,
            isUpvotedByMe = true
        )

        val newIsUpvoted = !initialComment.isUpvotedByMe
        val newCount =
            if (newIsUpvoted) initialComment.upvotesCount + 1 else (initialComment.upvotesCount - 1).coerceAtLeast(
                0
            )
        val updated = initialComment.copy(upvotesCount = newCount, isUpvotedByMe = newIsUpvoted)

        assertFalse(updated.isUpvotedByMe)
        assertEquals(0, updated.upvotesCount)
    }

    @Test
    fun nestedReplies_smartSorting_topUpvoted_sortsRepliesByLikes() {
        val replies = listOf(
            Comment(
                id = "r1",
                authorId = "u1",
                authorName = "A",
                content = "First reply",
                upvotesCount = 2,
                createdAtEpochMs = 1000L
            ),
            Comment(
                id = "r2",
                authorId = "u2",
                authorName = "B",
                content = "Top witty reply",
                upvotesCount = 15,
                createdAtEpochMs = 2000L
            ),
            Comment(
                id = "r3",
                authorId = "u3",
                authorName = "C",
                content = "Latest reply",
                upvotesCount = 8,
                createdAtEpochMs = 3000L
            )
        )

        val sortedReplies = replies.sortedWith(
            compareByDescending<Comment> { it.upvotesCount }.thenByDescending { it.createdAtEpochMs }
        )

        assertEquals("r2", sortedReplies[0].id)
        assertEquals(15, sortedReplies[0].upvotesCount)
        assertEquals("r3", sortedReplies[1].id)
        assertEquals(8, sortedReplies[1].upvotesCount)
        assertEquals("r1", sortedReplies[2].id)
        assertEquals(2, sortedReplies[2].upvotesCount)
    }

    @Test
    fun nestedReplies_smartSorting_chronologicalAsc_sortsForConversationFlow() {
        val replies = listOf(
            Comment(
                id = "r3",
                authorId = "u3",
                authorName = "C",
                content = "Latest reply",
                createdAtEpochMs = 3000L
            ),
            Comment(
                id = "r1",
                authorId = "u1",
                authorName = "A",
                content = "First reply",
                createdAtEpochMs = 1000L
            ),
            Comment(
                id = "r2",
                authorId = "u2",
                authorName = "B",
                content = "Middle reply",
                createdAtEpochMs = 2000L
            )
        )

        val sortedReplies = replies.sortedBy { it.createdAtEpochMs }

        assertEquals("r1", sortedReplies[0].id)
        assertEquals("r2", sortedReplies[1].id)
        assertEquals("r3", sortedReplies[2].id)
    }

    @Test
    fun trendingDiscussion_episodeDiscussion_carriesSeasonAndEpisode() {
        val episodeDiscussion = TrendingDiscussion(
            mediaId = 1399,
            mediaType = MediaType.Tv,
            title = "Ozymandias",
            backdropImageUrl = null,
            posterImageUrl = null,
            discussionCount = 120,
            seasonNumber = 5,
            episodeNumber = 14
        )

        assertEquals(1399, episodeDiscussion.mediaId)
        assertEquals(MediaType.Tv, episodeDiscussion.mediaType)
        assertEquals(5, episodeDiscussion.seasonNumber)
        assertEquals(14, episodeDiscussion.episodeNumber)
    }

    @Test
    fun discussionTarget_factories_createExpectedTargets() {
        val movieTarget = com.ssverma.shared.domain.model.community.DiscussionTarget.movie(693134)
        assertEquals(MediaType.Movie, movieTarget.mediaType)
        assertEquals(693134, movieTarget.mediaId)
        org.junit.Assert.assertNull(movieTarget.seasonNumber)
        org.junit.Assert.assertNull(movieTarget.episodeNumber)

        val tvTarget = com.ssverma.shared.domain.model.community.DiscussionTarget.tvShow(1399)
        assertEquals(MediaType.Tv, tvTarget.mediaType)
        assertEquals(1399, tvTarget.mediaId)
        org.junit.Assert.assertNull(tvTarget.seasonNumber)
        org.junit.Assert.assertNull(tvTarget.episodeNumber)

        val episodeTarget =
            com.ssverma.shared.domain.model.community.DiscussionTarget.tvEpisode(1399, 5, 14)
        assertEquals(MediaType.Tv, episodeTarget.mediaType)
        assertEquals(1399, episodeTarget.mediaId)
        assertEquals(5, episodeTarget.seasonNumber)
        assertEquals(14, episodeTarget.episodeNumber)
    }

    @Test
    fun discussionParams_dataClasses_encapsulatePayloadsSafely() {
        val target = com.ssverma.shared.domain.model.community.DiscussionTarget.movie(101)
        val postParams = com.ssverma.shared.domain.model.community.PostCommentParams(
            target = target,
            content = "Great cinematography!",
            isSpoiler = false,
            mediaTitle = "Inception"
        )
        assertEquals(target, postParams.target)
        assertEquals("Great cinematography!", postParams.content)
        assertFalse(postParams.isSpoiler)
        assertEquals("Inception", postParams.mediaTitle)

        val upvoteParams = com.ssverma.shared.domain.model.community.ToggleCommentUpvoteParams(
            target = target,
            commentId = "c100"
        )
        assertEquals(target, upvoteParams.target)
        assertEquals("c100", upvoteParams.commentId)
    }

    @Test
    fun discussions_remoteKillSwitch_constantsDefined() {
        assertEquals(
            "remote_discussions_enabled",
            CommunityRepositoryImpl.REMOTE_KEY_COMMUNITY_DISCUSSIONS_ENABLED
        )
        assertEquals(
            "remote_reactions_enabled",
            CommunityRepositoryImpl.REMOTE_KEY_COMMUNITY_REACTIONS_ENABLED
        )
        assertEquals(
            "remote_daily_polls_enabled",
            CommunityRepositoryImpl.REMOTE_KEY_DAILY_POLLS_ENABLED
        )
        assertEquals(
            "remote_community_lists_enabled",
            CommunityRepositoryImpl.REMOTE_KEY_COMMUNITY_LISTS_ENABLED
        )
    }
}
