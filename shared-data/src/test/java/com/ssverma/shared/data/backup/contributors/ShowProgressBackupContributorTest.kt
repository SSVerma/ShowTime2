package com.ssverma.shared.data.backup.contributors

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.data.local.db.dao.EpisodeWatchHistoryDao
import com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao
import com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ShowProgressBackupContributorTest {

    private val mockShowWatchProgressDao: ShowWatchProgressDao = mockk(relaxed = true)
    private val mockEpisodeWatchHistoryDao: EpisodeWatchHistoryDao = mockk(relaxed = true)
    private val gson = Gson()

    private lateinit var contributor: ShowProgressBackupContributor

    @Before
    fun setUp() {
        contributor = ShowProgressBackupContributor(
            showWatchProgressDao = mockShowWatchProgressDao,
            episodeWatchHistoryDao = mockEpisodeWatchHistoryDao
        )
    }

    @Test
    fun `exportData returns json with both progress and episode history`() = runTest {
        val progressList = listOf(
            ShowWatchProgressEntity(
                showId = 101,
                showTitle = "Breaking Bad",
                showPosterPath = "/poster.jpg",
                seasonNumber = 1,
                episodeNumber = 2,
                episodeTitle = "Cat's in the Bag...",
                seasonCompleted = 1,
                seasonTotalAired = 7,
                totalCompleted = 1,
                totalAired = 62,
                lastWatchedAt = 1000L
            )
        )
        val historyList = listOf(
            EpisodeWatchHistoryEntity(
                showId = 101,
                seasonNumber = 1,
                episodeNumber = 1,
                watchedAt = 1000L
            )
        )

        coEvery { mockShowWatchProgressDao.getAllProgress() } returns progressList
        coEvery { mockEpisodeWatchHistoryDao.getAllHistory() } returns historyList

        val json = contributor.exportData().asJsonObject

        assertThat(json.has(BackupMetadata.KEY_SHOW_PROGRESS)).isTrue()
        assertThat(json.has(BackupMetadata.KEY_EPISODE_HISTORY)).isTrue()
    }

    @Test
    fun `importData preserves local show progress when local progress is ahead of cloud backup`() =
        runTest {
            // Cloud backup has S01E02 (1 episode completed)
            val cloudProgress = listOf(
                ShowWatchProgressEntity(
                    showId = 101,
                    showTitle = "Breaking Bad",
                    showPosterPath = "/poster.jpg",
                    seasonNumber = 1,
                    episodeNumber = 2,
                    episodeTitle = "Cloud Episode 2",
                    seasonCompleted = 1,
                    seasonTotalAired = 7,
                    totalCompleted = 1,
                    totalAired = 62,
                    lastWatchedAt = 1000L
                )
            )

            // Local device has already watched S01E03 (3 episodes completed)
            val localProgress = ShowWatchProgressEntity(
                showId = 101,
                showTitle = "Breaking Bad",
                showPosterPath = "/poster.jpg",
                seasonNumber = 1,
                episodeNumber = 4,
                episodeTitle = "Local Episode 4 (Next Up)",
                seasonCompleted = 3,
                seasonTotalAired = 7,
                totalCompleted = 3,
                totalAired = 62,
                lastWatchedAt = 2000L
            )

            coEvery { mockShowWatchProgressDao.getProgress(101) } returns localProgress
            coEvery { mockEpisodeWatchHistoryDao.getWatchedCount(101) } returns 3

            val payload = JsonObject().apply {
                add(BackupMetadata.KEY_SHOW_PROGRESS, gson.toJsonTree(cloudProgress))
            }

            contributor.importData(featurePayload = payload, fullSnapshot = JsonObject())

            val savedSlot = slot<ShowWatchProgressEntity>()
            coVerify { mockShowWatchProgressDao.insertOrUpdate(capture(savedSlot)) }

            val saved = savedSlot.captured
            // Must preserve the further ahead local episode pointer
            assertThat(saved.seasonNumber).isEqualTo(1)
            assertThat(saved.episodeNumber).isEqualTo(4)
            assertThat(saved.totalCompleted).isEqualTo(3)
            assertThat(saved.lastWatchedAt).isEqualTo(2000L)
        }

    @Test
    fun `importData advances show progress when cloud backup is ahead of local progress`() =
        runTest {
            // Cloud backup is ahead at S02E01 (7 episodes completed)
            val cloudProgress = listOf(
                ShowWatchProgressEntity(
                    showId = 101,
                    showTitle = "Breaking Bad",
                    showPosterPath = "/poster.jpg",
                    seasonNumber = 2,
                    episodeNumber = 1,
                    episodeTitle = "Seven Thirty-Seven",
                    seasonCompleted = 0,
                    seasonTotalAired = 13,
                    totalCompleted = 7,
                    totalAired = 62,
                    lastWatchedAt = 3000L
                )
            )

            // Local device is behind at S01E02 (1 episode completed)
            val localProgress = ShowWatchProgressEntity(
                showId = 101,
                showTitle = "Breaking Bad",
                showPosterPath = "/poster.jpg",
                seasonNumber = 1,
                episodeNumber = 2,
                episodeTitle = "Local Episode 2",
                seasonCompleted = 1,
                seasonTotalAired = 7,
                totalCompleted = 1,
                totalAired = 62,
                lastWatchedAt = 1000L
            )

            coEvery { mockShowWatchProgressDao.getProgress(101) } returns localProgress
            coEvery { mockEpisodeWatchHistoryDao.getWatchedCount(101) } returns 7

            val payload = JsonObject().apply {
                add(BackupMetadata.KEY_SHOW_PROGRESS, gson.toJsonTree(cloudProgress))
            }

            contributor.importData(featurePayload = payload, fullSnapshot = JsonObject())

            val savedSlot = slot<ShowWatchProgressEntity>()
            coVerify { mockShowWatchProgressDao.insertOrUpdate(capture(savedSlot)) }

            val saved = savedSlot.captured
            // Must advance to the cloud episode pointer
            assertThat(saved.seasonNumber).isEqualTo(2)
            assertThat(saved.episodeNumber).isEqualTo(1)
            assertThat(saved.totalCompleted).isEqualTo(7)
            assertThat(saved.lastWatchedAt).isEqualTo(3000L)
        }

    @Test
    fun `importData inserts new show progress when show does not exist locally`() = runTest {
        val cloudProgress = listOf(
            ShowWatchProgressEntity(
                showId = 202,
                showTitle = "Severance",
                showPosterPath = "/severance.jpg",
                seasonNumber = 1,
                episodeNumber = 3,
                episodeTitle = "In Perpetuity",
                seasonCompleted = 2,
                seasonTotalAired = 9,
                totalCompleted = 2,
                totalAired = 9,
                lastWatchedAt = 1500L
            )
        )

        coEvery { mockShowWatchProgressDao.getProgress(202) } returns null

        val payload = JsonObject().apply {
            add(BackupMetadata.KEY_SHOW_PROGRESS, gson.toJsonTree(cloudProgress))
        }

        contributor.importData(featurePayload = payload, fullSnapshot = JsonObject())

        coVerify {
            mockShowWatchProgressDao.insertOrUpdate(
                match { it.showId == 202 && it.episodeNumber == 3 }
            )
        }
    }
}
