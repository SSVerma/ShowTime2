package com.ssverma.feature.movie.domain.usecase

import com.ssverma.feature.movie.domain.repository.MovieRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.movie.MovieCollection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieCollectionUseCaseTest {

    private val movieRepository: MovieRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var useCase: MovieCollectionUseCase

    @Before
    fun setUp() {
        useCase = MovieCollectionUseCase(
            coroutineDispatcher = testDispatcher,
            movieRepository = movieRepository
        )
    }

    @Test
    fun `execute returns movie collection from repository`() = runTest {
        val collectionId = 86311
        val expectedCollection = MovieCollection(
            id = collectionId,
            name = "The Avengers Collection",
            posterImageUrl = "/poster.jpg",
            backdropImageUrl = "/backdrop.jpg",
            overview = "Marvel's Avengers series.",
            parts = emptyList()
        )

        coEvery { movieRepository.fetchMovieCollection(collectionId) } returns Result.Success(
            expectedCollection
        )

        val result = useCase(collectionId)

        assertTrue(result is Result.Success)
        assertEquals(expectedCollection, (result as Result.Success).data)
        coVerify(exactly = 1) { movieRepository.fetchMovieCollection(collectionId) }
    }

    @Test
    fun `execute returns failure when repository returns error`() = runTest {
        val collectionId = 99999
        val expectedError = Failure.CoreFailure.UnexpectedFailure

        coEvery { movieRepository.fetchMovieCollection(collectionId) } returns Result.Error(
            expectedError
        )

        val result = useCase(collectionId)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, (result as Result.Error).error)
        coVerify(exactly = 1) { movieRepository.fetchMovieCollection(collectionId) }
    }
}
