package com.ssverma.shared.data.game

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.api.service.tmdb.response.RemoteMovie
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.shared.domain.model.game.GameClueType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DailyCinemaPuzzleProviderTest {

    private val mockTmdbApiService: TmdbApiService = mockk(relaxed = true)
    private lateinit var provider: DailyCinemaPuzzleProvider

    @Before
    fun setUp() {
        val mockRemoteMovie: RemoteMovie = mockk(relaxed = true) {
            every { id } returns 27205
            every { title } returns "Inception"
            every { releaseDate } returns "2010-07-16"
            every { tagline } returns "Your mind is the scene of the crime."
            every { overview } returns "A thief who steals corporate secrets..."
            every { posterPath } returns "/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg"
            every { backdropPath } returns "/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg"
        }

        coEvery {
            mockTmdbApiService.getMovieDetails(movieId = any(), queryMap = any())
        } returns ApiResponse.Success(body = mockRemoteMovie, payload = mockk(relaxed = true))

        provider = DailyCinemaPuzzleProvider(mockTmdbApiService)
    }

    @Test
    fun `getPuzzleForEpochDay returns deterministic puzzle with 5 clues`() = runTest {
        val epochDay = 20000L
        val puzzle = provider.getPuzzleForEpochDay(epochDay)

        assertNotNull(puzzle)
        assertTrue(puzzle.targetMovieTitle.isNotBlank())
        assertEquals(5, puzzle.clues.size)

        assertEquals(GameClueType.BLURRED_SHOT, puzzle.clues[0].type)
        assertEquals(GameClueType.SCENE_STILL, puzzle.clues[1].type)
        assertEquals(GameClueType.RELEASE_YEAR, puzzle.clues[2].type)
        assertEquals(GameClueType.CAST_DIRECTOR, puzzle.clues[3].type)
        assertEquals(GameClueType.PLOT_TAGLINE, puzzle.clues[4].type)
    }

    @Test
    fun `getPuzzleForEpochDay is idempotent for identical epoch days`() = runTest {
        val epochDay = 20345L
        val puzzle1 = provider.getPuzzleForEpochDay(epochDay)
        val puzzle2 = provider.getPuzzleForEpochDay(epochDay)

        assertEquals(puzzle1.targetMovieId, puzzle2.targetMovieId)
        assertEquals(puzzle1.targetMovieTitle, puzzle2.targetMovieTitle)
        assertEquals(puzzle1.puzzleNumber, puzzle2.puzzleNumber)
    }

    @Test
    fun `getTodayPuzzle returns valid daily puzzle`() = runTest {
        val todayPuzzle = provider.getTodayPuzzle()

        assertNotNull(todayPuzzle)
        assertTrue(todayPuzzle.puzzleNumber >= 1)
        assertTrue(todayPuzzle.targetMovieTitle.isNotBlank())
        assertTrue(todayPuzzle.posterImageUrl.isNotBlank())
    }
}
