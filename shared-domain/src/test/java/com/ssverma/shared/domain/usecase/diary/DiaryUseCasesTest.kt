package com.ssverma.shared.domain.usecase.diary

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.testing.fakes.FakeDiaryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DiaryUseCasesTest {

    private lateinit var fakeRepository: FakeDiaryRepository
    private lateinit var saveDiaryEntryUseCase: SaveDiaryEntryUseCase
    private lateinit var getDiaryEntriesUseCase: GetDiaryEntriesUseCase
    private lateinit var deleteDiaryEntryUseCase: DeleteDiaryEntryUseCase
    private lateinit var getDiarySummaryStatsUseCase: GetDiarySummaryStatsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeDiaryRepository()
        saveDiaryEntryUseCase = SaveDiaryEntryUseCase(fakeRepository)
        getDiaryEntriesUseCase = GetDiaryEntriesUseCase(fakeRepository)
        deleteDiaryEntryUseCase = DeleteDiaryEntryUseCase(fakeRepository)
        getDiarySummaryStatsUseCase = GetDiarySummaryStatsUseCase(fakeRepository)
    }

    @Test
    fun `save and retrieve diary entries with movie and tv parity`() = runTest {
        val movieEntry = DiaryEntry(
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Inception",
            posterImageUrl = "/inception.jpg",
            userRating = 5.0f,
            review = "Masterpiece",
            isRewatch = true
        )
        val tvEntry = DiaryEntry(
            mediaId = 202,
            mediaType = MediaType.Tv,
            title = "Breaking Bad",
            posterImageUrl = "/bb.jpg",
            userRating = 5.0f,
            review = "Peak TV",
            isRewatch = false
        )

        saveDiaryEntryUseCase(movieEntry)
        saveDiaryEntryUseCase(tvEntry)

        val allEntries = getDiaryEntriesUseCase(DiaryFilterType.ALL).first()
        assertEquals(2, allEntries.size)

        val movieEntries = getDiaryEntriesUseCase(DiaryFilterType.MOVIES_ONLY).first()
        assertEquals(1, movieEntries.size)
        assertEquals("Inception", movieEntries.first().title)

        val tvEntries = getDiaryEntriesUseCase(DiaryFilterType.TV_ONLY).first()
        assertEquals(1, tvEntries.size)
        assertEquals("Breaking Bad", tvEntries.first().title)

        val stats = getDiarySummaryStatsUseCase().first()
        assertEquals(2, stats.totalLogged)
        assertEquals(1, stats.totalMovies)
        assertEquals(1, stats.totalTvShows)
        assertEquals(5.0f, stats.averageUserRating, 0.01f)
        assertEquals(1, stats.rewatchCount)
        assertEquals(2, stats.fiveStarCount)
    }

    @Test
    fun `delete diary entry updates stats and list correctly`() = runTest {
        val id = saveDiaryEntryUseCase(
            DiaryEntry(
                mediaId = 303,
                mediaType = MediaType.Movie,
                title = "Interstellar",
                posterImageUrl = "/interstellar.jpg",
                userRating = 4.5f
            )
        )

        val beforeDelete = getDiaryEntriesUseCase().first()
        assertEquals(1, beforeDelete.size)

        deleteDiaryEntryUseCase(id)

        val afterDelete = getDiaryEntriesUseCase().first()
        assertTrue(afterDelete.isEmpty())

        val stats = getDiarySummaryStatsUseCase().first()
        assertEquals(0, stats.totalLogged)
    }
}
