package com.ssverma.shared.data.worker

import com.google.common.truth.Truth.assertThat
import com.ssverma.api.service.tmdb.response.RemoteProviderInfo
import org.junit.Test

class ReleaseRadarWorkerTest {

    @Test
    fun `buildMovieDeepLink creates correct movie deep link`() {
        val link = ReleaseRadarWorker.buildMovieDeepLink(mediaId = 693134)
        assertThat(link).isEqualTo("showtime://showtime.ssverma.in/movie/693134")
    }

    @Test
    fun `radar architecture constants enforce lightweight TMDB footprint`() {
        // Max 3 TMDB watch provider queries per day
        assertThat(ReleaseRadarWorker.MAX_STREAMING_CALLS_PER_DAY).isEqualTo(3)

        // 30 to 120 day temporal window
        assertThat(ReleaseRadarWorker.STREAMING_WINDOW_MIN_DAYS).isEqualTo(30L)
        assertThat(ReleaseRadarWorker.STREAMING_WINDOW_MAX_DAYS).isEqualTo(120L)

        // 7-day recheck cooldown per movie
        assertThat(ReleaseRadarWorker.STREAMING_RECHECK_COOLDOWN_DAYS).isEqualTo(7L)
    }

    @Test
    fun `filterMatchedProviders matches subscribed streaming services`() {
        val netflix = RemoteProviderInfo(
            logoPath = null,
            providerId = 8,
            providerName = "Netflix",
            displayPriority = 1
        )
        val prime = RemoteProviderInfo(
            logoPath = null,
            providerId = 119,
            providerName = "Amazon Prime Video",
            displayPriority = 2
        )
        val appleTv = RemoteProviderInfo(
            logoPath = null,
            providerId = 350,
            providerName = "Apple TV+",
            displayPriority = 3
        )

        val available = listOf(netflix, prime, appleTv)
        val userSubscriptions = setOf(8, 350) // User subscribes to Netflix & Apple TV+

        val matched = ReleaseRadarWorker.filterMatchedProviders(available, userSubscriptions)
        assertThat(matched).containsExactly(netflix, appleTv).inOrder()
    }

    @Test
    fun `filterMatchedProviders returns all providers when user has not set subscriptions`() {
        val netflix = RemoteProviderInfo(
            logoPath = null,
            providerId = 8,
            providerName = "Netflix",
            displayPriority = 1
        )
        val prime = RemoteProviderInfo(
            logoPath = null,
            providerId = 119,
            providerName = "Amazon Prime Video",
            displayPriority = 2
        )

        val available = listOf(netflix, prime)
        val matched = ReleaseRadarWorker.filterMatchedProviders(available, emptySet())

        assertThat(matched).containsExactly(netflix, prime).inOrder()
    }

    @Test
    fun `filterMatchedProviders returns empty when user subscriptions do not overlap`() {
        val netflix = RemoteProviderInfo(
            logoPath = null,
            providerId = 8,
            providerName = "Netflix",
            displayPriority = 1
        )
        val available = listOf(netflix)
        val userSubscriptions = setOf(337) // Disney+ only

        val matched = ReleaseRadarWorker.filterMatchedProviders(available, userSubscriptions)
        assertThat(matched).isEmpty()
    }

    @Test
    fun `formatStreamingProviderNames limits to top 3 providers`() {
        val p1 = RemoteProviderInfo(
            logoPath = null,
            providerId = 1,
            providerName = "Netflix",
            displayPriority = 1
        )
        val p2 = RemoteProviderInfo(
            logoPath = null,
            providerId = 2,
            providerName = "Prime Video",
            displayPriority = 2
        )
        val p3 = RemoteProviderInfo(
            logoPath = null,
            providerId = 3,
            providerName = "Max",
            displayPriority = 3
        )
        val p4 = RemoteProviderInfo(
            logoPath = null,
            providerId = 4,
            providerName = "Hulu",
            displayPriority = 4
        )

        val formatted = ReleaseRadarWorker.formatStreamingProviderNames(listOf(p1, p2, p3, p4))
        assertThat(formatted).isEqualTo("Netflix, Prime Video, Max")
    }
}
