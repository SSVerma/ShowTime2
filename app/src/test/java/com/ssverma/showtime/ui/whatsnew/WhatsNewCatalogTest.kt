package com.ssverma.showtime.ui.whatsnew

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.model.feature.CinephileFeature
import org.junit.Test

class WhatsNewCatalogTest {

    @Test
    fun `whats new catalog contains all headline features`() {
        val features = WhatsNewCatalog.allFeatures
        assertThat(features).hasSize(10)
    }

    @Test
    fun `whats new catalog features have valid resources and unique destinations`() {
        val features = WhatsNewCatalog.allFeatures
        val seenFeatures = mutableSetOf<CinephileFeature>()

        features.forEach { feature ->
            assertThat(feature.titleRes).isNotEqualTo(0)
            assertThat(feature.descriptionRes).isNotEqualTo(0)
            assertThat(feature.destinationNavKey).isNotNull()
            assertThat(seenFeatures.add(feature.feature)).isTrue()
        }

        assertThat(seenFeatures).containsExactly(
            CinephileFeature.CINEMA_DIARY,
            CinephileFeature.CINEMA_RECEIPT,
            CinephileFeature.BACKLOG_CHALLENGES,
            CinephileFeature.HOME_SCREEN_WIDGETS,
            CinephileFeature.DISCOVERY,
            CinephileFeature.MOVIE_MATCH,
            CinephileFeature.TASTE_PROFILE,
            CinephileFeature.DAILY_GAME,
            CinephileFeature.MY_LISTS,
            CinephileFeature.COMMUNITY_LISTS
        )
    }

    @Test
    fun `filterActiveFeatures returns all features when filter is blank`() {
        val features = WhatsNewCatalog.filterActiveFeatures("")
        assertThat(features).hasSize(WhatsNewCatalog.allFeatures.size)
    }

    @Test
    fun `filterActiveFeatures filters down to specified feature ids`() {
        val filter = "${CinephileFeature.MY_LISTS.id}, ${CinephileFeature.DISCOVERY.id}"
        val filtered = WhatsNewCatalog.filterActiveFeatures(filter)

        assertThat(filtered).hasSize(2)
        assertThat(filtered.map { it.feature }).containsExactly(
            CinephileFeature.MY_LISTS,
            CinephileFeature.DISCOVERY
        )
    }

    @Test
    fun `filterActiveFeatures falls back to all features if filter matches nothing`() {
        val filtered = WhatsNewCatalog.filterActiveFeatures("unknown_feature_xyz")
        assertThat(filtered).hasSize(WhatsNewCatalog.allFeatures.size)
    }
}
