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
    fun `saveDiaryEntry calls dao insert with entity conversion`() = runTest {
        val entry = DiaryEntry(
            id = 0L,
            mediaId = 1399,
            mediaType = MediaType.Tv,
            title = "Game of Thrones",
            posterImageUrl = "/got.jpg",
            userRating = 4.0f
        )

        coEvery { mockDiaryDao.insertDiaryEntry(any()) } returns 42L

        val generatedId = repository.saveDiaryEntry(entry)
        assertThat(generatedId).isEqualTo(42L)

        coVerify {
            mockDiaryDao.insertDiaryEntry(
                match { it.mediaId == 1399 && it.mediaType == "tv" && it.title == "Game of Thrones" }
            )
        }
    }

    @Test
    fun `deleteDiaryEntry calls dao delete by id`() = runTest {
        repository.deleteDiaryEntry(99L)
        coVerify { mockDiaryDao.deleteDiaryEntryById(99L) }
    }
}
