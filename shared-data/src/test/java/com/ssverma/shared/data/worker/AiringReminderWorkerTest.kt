package com.ssverma.shared.data.worker

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AiringReminderWorkerTest {

    @Test
    fun `buildNotificationTitle formats TV episode with season and episode numbers`() {
        val title = AiringReminderWorker.buildNotificationTitle(
            mediaType = "tv",
            mediaTitle = "Severance",
            seasonNumber = 2,
            episodeNumber = 4
        )
        assertThat(title).isEqualTo("Severance S2E4 airs today!")
    }

    @Test
    fun `buildNotificationTitle formats TV show without season-episode gracefully`() {
        val title = AiringReminderWorker.buildNotificationTitle(
            mediaType = "tv",
            mediaTitle = "Severance",
            seasonNumber = null,
            episodeNumber = null
        )
        assertThat(title).isEqualTo("Severance airs today!")
    }

    @Test
    fun `buildNotificationTitle formats movie release title`() {
        val title = AiringReminderWorker.buildNotificationTitle(
            mediaType = "movie",
            mediaTitle = "Dune: Part Three",
            seasonNumber = null,
            episodeNumber = null
        )
        assertThat(title).isEqualTo("Dune: Part Three releases today!")
    }

    @Test
    fun `buildNotificationMessage includes episode title and provider`() {
        val message = AiringReminderWorker.buildNotificationMessage(
            mediaType = "tv",
            episodeTitle = "The Way We Were",
            providerName = "Apple TV+"
        )
        assertThat(message).isEqualTo("\"The Way We Were\" · Available on Apple TV+")
    }

    @Test
    fun `buildNotificationMessage falls back to default text when metadata absent`() {
        val tvMessage = AiringReminderWorker.buildNotificationMessage(
            mediaType = "tv",
            episodeTitle = null,
            providerName = null
        )
        assertThat(tvMessage).contains("new episode is airing today")

        val movieMessage = AiringReminderWorker.buildNotificationMessage(
            mediaType = "movie",
            episodeTitle = null,
            providerName = null
        )
        assertThat(movieMessage).contains("available in cinemas")
    }

    @Test
    fun `buildDeepLink builds correct showtime deep links`() {
        val tvDeepLink = AiringReminderWorker.buildDeepLink(mediaType = "tv", mediaId = 12345)
        assertThat(tvDeepLink).isEqualTo("showtime://showtime.ssverma.in/tv/12345")

        val movieDeepLink = AiringReminderWorker.buildDeepLink(mediaType = "movie", mediaId = 67890)
        assertThat(movieDeepLink).isEqualTo("showtime://showtime.ssverma.in/movie/67890")
    }
}
