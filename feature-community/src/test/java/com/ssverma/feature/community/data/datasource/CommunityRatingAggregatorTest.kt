package com.ssverma.feature.community.data.datasource

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import org.junit.Test

class CommunityRatingAggregatorTest {

    @Test
    fun `applySubmit on empty aggregate increments count, sum, average, and bucket`() {
        val result = CommunityRatingAggregator.applySubmit(
            current = AggregateRatingSnapshot(),
            previousRating = null,
            newRating = 4.5f
        )

        assertThat(result.ratingCount).isEqualTo(1)
        assertThat(result.ratingSum).isWithin(0.001).of(4.5)
        assertThat(result.averageRating).isWithin(0.001f).of(4.5f)
        assertThat(result.histogram[4.5f]).isEqualTo(1)
        assertThat(result.histogram.values.sum()).isEqualTo(1)
    }

    @Test
    fun `applySubmit updating existing rating preserves count and shifts bucket`() {
        val initial = AggregateRatingSnapshot(
            ratingCount = 2,
            ratingSum = 7.5,
            averageRating = 3.75f,
            histogram = AggregateRatingSnapshot.emptyHistogram() + mapOf(
                3.5f to 1,
                4.0f to 1
            )
        )

        val updated = CommunityRatingAggregator.applySubmit(
            current = initial,
            previousRating = 3.5f,
            newRating = 5.0f
        )

        assertThat(updated.ratingCount).isEqualTo(2)
        assertThat(updated.ratingSum).isWithin(0.001).of(9.0)
        assertThat(updated.averageRating).isWithin(0.001f).of(4.5f)
        assertThat(updated.histogram[3.5f]).isEqualTo(0)
        assertThat(updated.histogram[4.0f]).isEqualTo(1)
        assertThat(updated.histogram[5.0f]).isEqualTo(1)
    }

    @Test
    fun `applyRemove decrements count, sum, and bucket cleanly to zero`() {
        val initial = AggregateRatingSnapshot(
            ratingCount = 1,
            ratingSum = 4.0,
            averageRating = 4.0f,
            histogram = AggregateRatingSnapshot.emptyHistogram() + mapOf(4.0f to 1)
        )

        val removed = CommunityRatingAggregator.applyRemove(
            current = initial,
            previousRating = 4.0f
        )

        assertThat(removed.ratingCount).isEqualTo(0)
        assertThat(removed.ratingSum).isEqualTo(0.0)
        assertThat(removed.averageRating).isEqualTo(0f)
        assertThat(removed.histogram[4.0f]).isEqualTo(0)
    }

    @Test
    fun `resolveDisplayState on optimistic removal does not snap back to stale remote rating`() {
        val remoteAggregate = AggregateRatingSnapshot(
            ratingCount = 5,
            ratingSum = 20.5,
            averageRating = 4.1f,
            histogram = AggregateRatingSnapshot.emptyHistogram() + mapOf(
                4.0f to 4,
                4.5f to 1
            )
        )

        val resolved = CommunityRatingAggregator.resolveDisplayState(
            mediaType = MediaType.Movie,
            mediaId = 550,
            remoteAggregate = remoteAggregate,
            localUserRating = null,
            remoteUserRating = 4.5f, // Stale remote snapshot before delete commits
            optimisticAction = OptimisticRatingAction.Removed,
            sufficientThreshold = 5
        )

        assertThat(resolved.userRating).isNull()
        assertThat(resolved.ratingCount).isEqualTo(4)
        assertThat(resolved.ratingSum).isWithin(0.001).of(16.0)
        assertThat(resolved.averageRating).isWithin(0.001f).of(4.0f)
        assertThat(resolved.getBucketCount(4.5f)).isEqualTo(0)
        assertThat(resolved.hasSufficientRatings()).isFalse()
    }

    @Test
    fun `resolveDisplayState on optimistic submit updates aggregate immediately before remote catches up`() {
        val remoteAggregate = AggregateRatingSnapshot(
            ratingCount = 4,
            ratingSum = 16.0,
            averageRating = 4.0f,
            histogram = AggregateRatingSnapshot.emptyHistogram() + mapOf(4.0f to 4)
        )

        val resolved = CommunityRatingAggregator.resolveDisplayState(
            mediaType = MediaType.Movie,
            mediaId = 550,
            remoteAggregate = remoteAggregate,
            localUserRating = 5.0f,
            remoteUserRating = null, // Remote hasn't committed yet
            optimisticAction = OptimisticRatingAction.Submitted(rating = 5.0f),
            sufficientThreshold = 5
        )

        assertThat(resolved.userRating).isEqualTo(5.0f)
        assertThat(resolved.ratingCount).isEqualTo(5)
        assertThat(resolved.ratingSum).isWithin(0.001).of(21.0)
        assertThat(resolved.averageRating).isWithin(0.001f).of(4.2f)
        assertThat(resolved.getBucketCount(5.0f)).isEqualTo(1)
        assertThat(resolved.hasSufficientRatings()).isTrue()
    }

    @Test
    fun `resolveDisplayState does not double-count once remote catches up with optimistic submit`() {
        val remoteAggregate = AggregateRatingSnapshot(
            ratingCount = 5,
            ratingSum = 21.0,
            averageRating = 4.2f,
            histogram = AggregateRatingSnapshot.emptyHistogram() + mapOf(
                4.0f to 4,
                5.0f to 1
            )
        )

        val resolved = CommunityRatingAggregator.resolveDisplayState(
            mediaType = MediaType.Movie,
            mediaId = 550,
            remoteAggregate = remoteAggregate,
            localUserRating = 5.0f,
            remoteUserRating = 5.0f, // Remote already has 5.0
            optimisticAction = OptimisticRatingAction.Submitted(rating = 5.0f),
            sufficientThreshold = 5
        )

        assertThat(resolved.ratingCount).isEqualTo(5)
        assertThat(resolved.getBucketCount(5.0f)).isEqualTo(1)
        assertThat(resolved.userRating).isEqualTo(5.0f)
    }

    @Test
    fun `parseHistogram and toFirestoreHistogram cover all 10 valid steps and clamp negatives`() {
        val parsed = CommunityRatingAggregator.parseHistogram(
            mapOf(
                "0.5" to 2L,
                "3.5" to -4L,
                "5.0" to 7,
                "invalid" to 99
            )
        )

        assertThat(parsed).hasSize(CommunityRating.VALID_RATING_STEPS.size)
        assertThat(parsed[0.5f]).isEqualTo(2)
        assertThat(parsed[3.5f]).isEqualTo(0)
        assertThat(parsed[5.0f]).isEqualTo(7)

        val firestoreMap = CommunityRatingAggregator.toFirestoreHistogram(parsed)
        assertThat(firestoreMap.keys).containsExactly(
            "0.5", "1.0", "1.5", "2.0", "2.5",
            "3.0", "3.5", "4.0", "4.5", "5.0"
        )
        assertThat(firestoreMap["0.5"]).isEqualTo(2)
        assertThat(firestoreMap["5.0"]).isEqualTo(7)
    }
}
