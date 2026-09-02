package com.ssverma.shared.domain.usecase.stats

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.fakes.FakeDiaryRepository
import com.ssverma.shared.domain.fakes.FakeDiscoveryRepository
import com.ssverma.shared.domain.fakes.FakeLibraryRepository
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.usecase.recommendation.GetSmartRecommendationsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TasteUseCasesTest {

    private lateinit var fakeDiaryRepository: FakeDiaryRepository
    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeDiscoveryRepository: FakeDiscoveryRepository
    private lateinit var getTasteProfileUseCase: GetTasteProfileUseCase
    private lateinit var getSmartRecommendationsUseCase: GetSmartRecommendationsUseCase

    @Before
    fun setUp() {
        fakeDiaryRepository = FakeDiaryRepository()
        fakeLibraryRepository = FakeLibraryRepository()
        fakeDiscoveryRepository = FakeDiscoveryRepository()

        getTasteProfileUseCase = GetTasteProfileUseCase(
            diaryRepository = fakeDiaryRepository,
            libraryRepository = fakeLibraryRepository
        )
        getSmartRecommendationsUseCase = GetSmartRecommendationsUseCase(
            discoveryRepository = fakeDiscoveryRepository
        )
    }

    @Test
    fun `empty diary returns default empty taste profile`() = runTest {
        val profile = getTasteProfileUseCase(DiaryFilterType.ALL).first()
        assertEquals(0, profile.totalItemsLogged)
        assertEquals(0, profile.totalWatchedMinutes)
        assertEquals(0f, profile.averageRating, 0.01f)
    }

    @Test
    fun `taste profile calculates watch time, ratings, and eras correctly`() = runTest {
        val entry1 = DiaryEntry(
            id = 1,
            mediaId = 101,
            mediaType = MediaType.Movie,
            title = "Oppenheimer",
            posterImageUrl = "/oppenheimer.jpg",
            releaseDate = "2023-07-21",
            userRating = 5.0f,
            isRewatch = true
        )
        val entry2 = DiaryEntry(
            id = 2,
            mediaId = 202,
            mediaType = MediaType.Tv,
            title = "Succession",
            posterImageUrl = "/succession.jpg",
            releaseDate = "2018-06-03",
            userRating = 4.0f,
            isRewatch = false
        )

        fakeDiaryRepository.saveDiaryEntry(entry1)
        fakeDiaryRepository.saveDiaryEntry(entry2)

        val profile = getTasteProfileUseCase(DiaryFilterType.ALL).first()

        assertEquals(2, profile.totalItemsLogged)
        assertEquals(1, profile.totalMoviesLogged)
        assertEquals(1, profile.totalTvLogged)
        assertEquals(4.5f, profile.averageRating, 0.01f)
        assertEquals(1, profile.rewatchCount)
        assertEquals(50f, profile.rewatchPercentage, 0.01f)
        assertTrue(profile.totalWatchedMinutes > 0)
        assertTrue(profile.eraDistribution.isNotEmpty())
        assertEquals("Oppenheimer", profile.topRatedSeedTitles.first())
    }

    @Test
    fun `smart recommendations returns curated shelves`() = runTest {
        val result = getSmartRecommendationsUseCase(
            filterType = DiaryFilterType.ALL
        )

        assertTrue(result is Result.Success)
        val shelves = (result as Result.Success).data
        assertTrue(shelves.isNotEmpty())
        assertNotNull(shelves.find { it.id == "top_picks" })
        assertNotNull(shelves.find { it.id == "masterpieces" })
    }
}
