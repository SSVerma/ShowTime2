package com.ssverma.shared.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.data.local.db.dao.DiaryDao
import com.ssverma.shared.data.local.db.entity.DiaryEntryEntity
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DefaultDiaryRepositoryTest {

    private val mockDiaryDao: DiaryDao = mockk(relaxed = true)
    private lateinit var repository: DefaultDiaryRepository

    @Before
    fun setUp() {
        repository = DefaultDiaryRepository(diaryDao = mockDiaryDao)
    }

    @Test
    fun `getAllDiaryEntries maps entity flow to domain model`() = runTest {
        val entity = DiaryEntryEntity(
            id = 1L,
            mediaId = 550,
            mediaType = "Movie",
            title = "Fight Club",
            posterImageUrl = "/fightclub.jpg",
            backdropImageUrl = "/fc_bg.jpg",
            releaseDate = "1999-10-15",
            tmdbRating = 8.4f,
            userRating = 5.0f,
            review = "Classic",
            isRewatch = true,
            loggedAt = 123456789L
        )

        coEvery { mockDiaryDao.getAllDiaryEntries() } returns flowOf(listOf(entity))

        val result = repository.getAllDiaryEntries().first()
        assertThat(result).hasSize(1)
        val domain = result.first()
        assertThat(domain.id).isEqualTo(1L)
        assertThat(domain.mediaId).isEqualTo(550)
        assertThat(domain.mediaType).isEqualTo(MediaType.Movie)
        assertThat(domain.title).isEqualTo("Fight Club")
        assertThat(domain.userRating).isEqualTo(5.0f)
        assertThat(domain.isRewatch).isTrue()
    }

    @Test
    fun `getDiaryEntryForMedia calls dao and maps to domain`() = runTest {
        val entity = DiaryEntryEntity(
            id = 5L,
            mediaId = 101,
            mediaType = "movie",
            title = "Inception",
            posterImageUrl = "/inc.jpg",
            userRating = 5.0f
        )
        coEvery { mockDiaryDao.getDiaryEntryByMedia(101, "movie") } returns entity

        val result = repository.getDiaryEntryForMedia(101, MediaType.Movie)
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(5L)
        assertThat(result?.title).isEqualTo("Inception")
    }

    @Test
    fun `saveDiaryEntry with id 0 and no existing record inserts new entity`() = runTest {
        val entry = DiaryEntry(
            id = 0L,
            mediaId = 1399,
            mediaType = MediaType.Tv,
            title = "Game of Thrones",
            posterImageUrl = "/got.jpg",
            userRating = 4.0f
        )

        coEvery { mockDiaryDao.getDiaryEntryByMedia(1399, "tv") } returns null
        coEvery { mockDiaryDao.insertDiaryEntry(any()) } returns 42L

        val generatedId = repository.saveDiaryEntry(entry)
        assertThat(generatedId).isEqualTo(42L)

        coVerify {
            mockDiaryDao.insertDiaryEntry(
                match { it.id == 0L && it.mediaId == 1399 && it.mediaType == "tv" && it.title == "Game of Thrones" }
            )
        }
    }

    @Test
    fun `saveDiaryEntry with id 0 reuses existing entity id and loggedAt to prevent duplicates`() =
        runTest {
            val existingEntity = DiaryEntryEntity(
                id = 77L,
                mediaId = 101,
                mediaType = "movie",
                title = "Inception",
                posterImageUrl = "/old_poster.jpg",
                userRating = 4.0f,
                loggedAt = 1000L
            )
            val updatedEntry = DiaryEntry(
                id = 0L,
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Inception",
                posterImageUrl = "/new_poster.jpg",
                userRating = 5.0f,
                review = "Rewatched and loved it!",
                isRewatch = true,
                loggedAt = 0L
            )

            coEvery { mockDiaryDao.getDiaryEntryByMedia(101, "movie") } returns existingEntity
            coEvery { mockDiaryDao.insertDiaryEntry(any()) } returns 77L

            val resultId = repository.saveDiaryEntry(updatedEntry)
            assertThat(resultId).isEqualTo(77L)

            coVerify {
                mockDiaryDao.insertDiaryEntry(
                    match { it.id == 77L && it.userRating == 5.0f && it.loggedAt == 1000L && it.isRewatch }
                )
            }
        }

    @Test
    fun `deleteDiaryEntry calls dao delete by id`() = runTest {
        repository.deleteDiaryEntry(99L)
        coVerify { mockDiaryDao.deleteDiaryEntryById(99L) }
    }
}
