package com.ssverma.shared.domain.usecase.challenge

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.repository.BacklogRepository
import com.ssverma.shared.domain.repository.DiaryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BacklogChallengesUseCaseTest {

    private val backlogRepository: BacklogRepository = mockk(relaxed = true)
    private val diaryRepository: DiaryRepository = mockk(relaxed = true)

    private lateinit var getBacklogChallengesUseCase: GetBacklogChallengesUseCase
    private lateinit var manageChallengeUseCase: ManageChallengeUseCase

    @Before
    fun setUp() {
        every { backlogRepository.curatedChallengesFlow } returns flowOf(emptyList())
        getBacklogChallengesUseCase = GetBacklogChallengesUseCase(
            backlogRepository = backlogRepository,
            diaryRepository = diaryRepository
        )
        manageChallengeUseCase = ManageChallengeUseCase(
            backlogRepository = backlogRepository
        )
    }

    @Test
    fun `invoke calculates item-based challenge progress correctly with movie and tv parity`() =
        runTest {
            val sampleMovie1 = ChallengeMediaItem(
                id = 101,
                title = "The Godfather",
                mediaType = MediaType.Movie,
                posterImageUrl = "/godfather.jpg",
                releaseYear = "1972"
            )
            val sampleMovie2 = ChallengeMediaItem(
                id = 102,
                title = "Inception",
                mediaType = MediaType.Movie,
                posterImageUrl = "/inception.jpg",
                releaseYear = "2010"
            )
            val sampleTvShow1 = ChallengeMediaItem(
                id = 201,
                title = "Succession",
                mediaType = MediaType.Tv,
                posterImageUrl = "/succession.jpg",
                releaseYear = "2018"
            )
            val sampleTvShow2 = ChallengeMediaItem(
                id = 202,
                title = "Breaking Bad",
                mediaType = MediaType.Tv,
                posterImageUrl = "/breakingbad.jpg",
                releaseYear = "2008"
            )

            val challenge = CinephileChallenge(
                id = "sight_and_sound_all",
                title = "All-Time Masterpieces",
                description = "Iconic cinema and television essentials",
                category = ChallengeCategory.Curated,
                mediaTypeFilter = ChallengeMediaTypeFilter.ALL,
                targetCount = 4,
                targetMediaItems = listOf(sampleMovie1, sampleMovie2, sampleTvShow1, sampleTvShow2)
            )

            // User watched The Godfather (Movie 101) and Succession (TV 201)
            val diaryEntries = listOf(
                DiaryEntry(
                    id = 1L,
                    mediaId = 101,
                    mediaType = MediaType.Movie,
                    title = "The Godfather",
                    posterImageUrl = "/godfather.jpg",
                    loggedAt = 1700000000000L,
                    userRating = 5.0f,
                    review = "Masterpiece"
                ),
                DiaryEntry(
                    id = 2L,
                    mediaId = 201,
                    mediaType = MediaType.Tv,
                    title = "Succession",
                    posterImageUrl = "/succession.jpg",
                    loggedAt = 1705000000000L,
                    userRating = 5.0f,
                    review = "Peak drama"
                )
            )

            every { backlogRepository.activeChallengesFlow } returns flowOf(listOf(challenge))
            every { diaryRepository.getAllDiaryEntries() } returns flowOf(diaryEntries)

            val progressList = getBacklogChallengesUseCase().first()
            assertEquals(1, progressList.size)

            val progress = progressList.first()
            assertEquals(4, progress.totalCount)
            assertEquals(2, progress.watchedCount)
            assertEquals(50, progress.progressPercentage)
            assertFalse(progress.isCompleted)
            assertEquals("Silver Connoisseur 🥈", progress.milestoneTitle)
            assertEquals(2, progress.watchedItems.size)
            assertEquals(2, progress.remainingItems.size)
            assertTrue(progress.watchedItems.any { it.id == 101 && it.mediaType == MediaType.Movie })
            assertTrue(progress.watchedItems.any { it.id == 201 && it.mediaType == MediaType.Tv })
            assertTrue(progress.remainingItems.any { it.id == 102 && it.mediaType == MediaType.Movie })
            assertTrue(progress.remainingItems.any { it.id == 202 && it.mediaType == MediaType.Tv })
        }

    @Test
    fun `invoke marks 100 percent completion when all items watched`() = runTest {
        val sampleMovie = ChallengeMediaItem(
            id = 550,
            title = "Fight Club",
            mediaType = MediaType.Movie,
            posterImageUrl = "/fightclub.jpg",
            releaseYear = "1999"
        )
        val challenge = CinephileChallenge(
            id = "fincher_sprint",
            title = "Fincher Sprint",
            description = "Watch Fincher masterworks",
            category = ChallengeCategory.DirectorSpotlight,
            targetCount = 1,
            targetMediaItems = listOf(sampleMovie)
        )

        val diaryEntries = listOf(
            DiaryEntry(
                id = 1L,
                mediaId = 550,
                mediaType = MediaType.Movie,
                title = "Fight Club",
                posterImageUrl = "/fightclub.jpg",
                loggedAt = 1700000000000L,
                userRating = 5.0f,
                review = "Iconic"
            )
        )

        every { backlogRepository.activeChallengesFlow } returns flowOf(listOf(challenge))
        every { diaryRepository.getAllDiaryEntries() } returns flowOf(diaryEntries)

        val progress = getBacklogChallengesUseCase().first().first()
        assertEquals(100, progress.progressPercentage)
        assertTrue(progress.isCompleted)
        assertEquals("Master Cinephile 🏆", progress.milestoneTitle)
    }

    @Test
    fun `manageChallengeUseCase delegates blindspot and challenge operations`() = runTest {
        val blindspot = BlindspotPriorityItem(
            mediaId = 300,
            mediaType = MediaType.Movie,
            title = "Casablanca",
            posterImageUrl = "/casablanca.jpg",
            releaseYear = "1942"
        )

        manageChallengeUseCase.addBlindspot(blindspot)
        coVerify(exactly = 1) { backlogRepository.addBlindspot(blindspot) }

        manageChallengeUseCase.removeBlindspot(300, MediaType.Movie)
        coVerify(exactly = 1) { backlogRepository.removeBlindspot(300, MediaType.Movie) }

        coEvery { backlogRepository.isBlindspot(300, MediaType.Movie) } returns true
        assertTrue(manageChallengeUseCase.isBlindspot(300, MediaType.Movie))
    }

    @Test
    fun `invoke enriches active curated challenges with updated curated metadata`() = runTest {
        val staleActiveChallenge = CinephileChallenge(
            id = "curated_prestige_tv",
            title = "Old Title",
            description = "Old Desc",
            category = ChallengeCategory.Curated,
            mediaTypeFilter = ChallengeMediaTypeFilter.TV,
            targetCount = 1,
            targetMediaItems = listOf(
                ChallengeMediaItem(
                    id = 1398,
                    title = "The Sopranos",
                    mediaType = MediaType.Tv,
                    posterImageUrl = "/old_spiderman_poster.jpg",
                    releaseYear = "1999"
                )
            )
        )

        val freshCuratedChallenge = CinephileChallenge(
            id = "curated_prestige_tv",
            title = "Prestige TV Hall of Fame",
            description = "Updated Desc",
            category = ChallengeCategory.Curated,
            mediaTypeFilter = ChallengeMediaTypeFilter.TV,
            targetCount = 1,
            targetMediaItems = listOf(
                ChallengeMediaItem(
                    id = 1398,
                    title = "The Sopranos",
                    mediaType = MediaType.Tv,
                    posterImageUrl = "/rTc7ZXdroqjkKivFPvCPX0Ru7uw.jpg",
                    releaseYear = "1999"
                )
            )
        )

        every { backlogRepository.activeChallengesFlow } returns flowOf(listOf(staleActiveChallenge))
        every { backlogRepository.curatedChallengesFlow } returns flowOf(
            listOf(
                freshCuratedChallenge
            )
        )
        every { diaryRepository.getAllDiaryEntries() } returns flowOf(emptyList())

        val result = getBacklogChallengesUseCase().first()
        assertEquals(1, result.size)
        val progress = result.first()
        assertEquals("Prestige TV Hall of Fame", progress.challenge.title)
        assertEquals(
            "/rTc7ZXdroqjkKivFPvCPX0Ru7uw.jpg",
            progress.challenge.targetMediaItems.first().posterImageUrl
        )
    }

    @Test
    fun `getChallengeDetailFlow returns enriched metadata and joined status`() = runTest {
        val staleActiveChallenge = CinephileChallenge(
            id = "curated_prestige_tv",
            title = "Old Title",
            description = "Old Desc",
            category = ChallengeCategory.Curated,
            mediaTypeFilter = ChallengeMediaTypeFilter.TV,
            targetCount = 1,
            targetMediaItems = listOf(
                ChallengeMediaItem(
                    id = 1398,
                    title = "The Sopranos",
                    mediaType = MediaType.Tv,
                    posterImageUrl = "/wrong.jpg",
                    releaseYear = "1999"
                )
            )
        )

        val freshCuratedChallenge = CinephileChallenge(
            id = "curated_prestige_tv",
            title = "Prestige TV Hall of Fame",
            description = "Updated Desc",
            category = ChallengeCategory.Curated,
            mediaTypeFilter = ChallengeMediaTypeFilter.TV,
            targetCount = 1,
            targetMediaItems = listOf(
                ChallengeMediaItem(
                    id = 1398,
                    title = "The Sopranos",
                    mediaType = MediaType.Tv,
                    posterImageUrl = "/rTc7ZXdroqjkKivFPvCPX0Ru7uw.jpg",
                    releaseYear = "1999"
                )
            )
        )

        every { backlogRepository.activeChallengesFlow } returns flowOf(listOf(staleActiveChallenge))
        every { backlogRepository.curatedChallengesFlow } returns flowOf(
            listOf(
                freshCuratedChallenge
            )
        )
        every { diaryRepository.getAllDiaryEntries() } returns flowOf(emptyList())

        val (progress, isJoined) = getBacklogChallengesUseCase.getChallengeDetailFlow("curated_prestige_tv")
            .first()
        assertTrue(isJoined)
        assertEquals("Prestige TV Hall of Fame", progress?.challenge?.title)
        assertEquals(
            "/rTc7ZXdroqjkKivFPvCPX0Ru7uw.jpg",
            progress?.challenge?.targetMediaItems?.first()?.posterImageUrl
        )
    }
}
