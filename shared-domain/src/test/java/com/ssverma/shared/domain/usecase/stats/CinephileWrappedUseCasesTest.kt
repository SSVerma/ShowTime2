package com.ssverma.shared.domain.usecase.stats

import com.ssverma.shared.domain.fakes.FakeDiaryRepository
import com.ssverma.shared.domain.fakes.FakeLibraryRepository
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.utils.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CinephileWrappedUseCasesTest {

    private lateinit var fakeDiaryRepository: FakeDiaryRepository
    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var getCinephileWrappedUseCase: GetCinephileWrappedUseCase

    @Before
    fun setUp() {
        fakeDiaryRepository = FakeDiaryRepository()
        fakeLibraryRepository = FakeLibraryRepository()
        getCinephileWrappedUseCase = GetCinephileWrappedUseCase(
            diaryRepository = fakeDiaryRepository,
            libraryRepository = fakeLibraryRepository
        )
    }

    @Test
    fun `empty diary produces empty wrapped summary`() = runTest {
        val summary = getCinephileWrappedUseCase().first()

        assertEquals(0, summary.totalLogged)
        assertEquals(0, summary.totalMovies)
        assertEquals(0, summary.totalTvShows)
        assertEquals(0, summary.totalWatchHours)
        assertEquals(0f, summary.averageUserRating, 0.01f)
        assertTrue(summary.topRatedMedia.isEmpty())
    }

    @Test
    fun `wrapped calculates annual totals and top rated media correctly`() = runTest {
        val date2026 = DateUtils.toMillis(LocalDate.of(2026, 5, 10))
        val date2025 = DateUtils.toMillis(LocalDate.of(2025, 8, 15))

        fakeDiaryRepository.saveDiaryEntry(
            DiaryEntry(
                id = 1,
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Oppenheimer",
                posterImageUrl = "/opp.jpg",
                releaseDate = "2023-07-21",
                userRating = 5.0f,
                isRewatch = false,
                loggedAt = date2026
            )
        )
        fakeDiaryRepository.saveDiaryEntry(
            DiaryEntry(
                id = 2,
                mediaId = 102,
                mediaType = MediaType.Tv,
                title = "Succession",
                posterImageUrl = "/succ.jpg",
                releaseDate = "2018-06-03",
                userRating = 4.5f,
                isRewatch = true,
                loggedAt = date2026
            )
        )
        fakeDiaryRepository.saveDiaryEntry(
            DiaryEntry(
                id = 3,
                mediaId = 103,
                mediaType = MediaType.Movie,
                title = "Past Lives",
                posterImageUrl = "/past.jpg",
                releaseDate = "2023-06-02",
                userRating = 4.0f,
                isRewatch = false,
                loggedAt = date2025
            )
        )

        val summary2026 = getCinephileWrappedUseCase(2026).first()

        assertEquals(2026, summary2026.year)
        assertEquals("2026", summary2026.yearLabel)
        assertEquals(2, summary2026.totalLogged)
        assertEquals(1, summary2026.totalMovies)
        assertEquals(1, summary2026.totalTvShows)
        assertEquals(4.75f, summary2026.averageUserRating, 0.01f)
        assertEquals(1, summary2026.rewatchCount)
        assertEquals(1, summary2026.fiveStarCount)
        assertEquals(2, summary2026.topRatedMedia.size)
        assertEquals("Oppenheimer", summary2026.topRatedMedia[0].title)

        // Milestones
        val firstReelMilestone = summary2026.milestones.find { it.id == "first_reel" }
        assertNotNull(firstReelMilestone)
        assertTrue(firstReelMilestone!!.isUnlocked)

        val nostalgiaMilestone = summary2026.milestones.find { it.id == "nostalgia_junkie" }
        assertNotNull(nostalgiaMilestone)
        assertEquals(1, nostalgiaMilestone!!.currentProgress)
    }

    @Test
    fun `all time wrapped aggregates all years`() = runTest {
        val date2026 = DateUtils.toMillis(LocalDate.of(2026, 1, 10))
        val date2025 = DateUtils.toMillis(LocalDate.of(2025, 2, 10))

        fakeDiaryRepository.saveDiaryEntry(
            DiaryEntry(
                id = 1,
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Movie 1",
                posterImageUrl = "",
                releaseDate = "2020-01-01",
                userRating = 4.0f,
                loggedAt = date2026
            )
        )
        fakeDiaryRepository.saveDiaryEntry(
            DiaryEntry(
                id = 2,
                mediaId = 102,
                mediaType = MediaType.Movie,
                title = "Movie 2",
                posterImageUrl = "",
                releaseDate = "2010-01-01",
                userRating = 5.0f,
                loggedAt = date2025
            )
        )

        val allTimeSummary = getCinephileWrappedUseCase(0).first()

        assertEquals(0, allTimeSummary.year)
        assertEquals("All-Time", allTimeSummary.yearLabel)
        assertEquals(2, allTimeSummary.totalLogged)
        assertEquals(4.5f, allTimeSummary.averageUserRating, 0.01f)
    }
}
