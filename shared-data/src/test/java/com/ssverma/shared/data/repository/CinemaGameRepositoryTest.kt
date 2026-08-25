package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.emptyPreferences
import com.google.common.truth.Truth.assertThat
import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.shared.data.game.DailyCinemaPuzzleProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CinemaGameRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockKeyValueStorageClient: KeyValueStorageClient = mockk(relaxed = true)
    private val mockStorage: KeyValueStorage = mockk(relaxed = true)
    private val mockTmdbApiService: TmdbApiService = mockk(relaxed = true)
    private lateinit var puzzleProvider: DailyCinemaPuzzleProvider

    private lateinit var repository: CinemaGameRepositoryImpl

    @Before
    fun setUp() {
        puzzleProvider = DailyCinemaPuzzleProvider(mockTmdbApiService)
        every { mockKeyValueStorageClient.createKeyValueStorage(any(), any()) } returns mockStorage
        every { mockStorage.data } returns flowOf(emptyPreferences())

        repository = CinemaGameRepositoryImpl(
            context = mockContext,
            keyValueStorageClient = mockKeyValueStorageClient,
            puzzleProvider = puzzleProvider
        )
    }

    @Test
    fun `getTodayPuzzle returns non-null puzzle with valid metadata`() = runTest {
        val puzzle = repository.getTodayPuzzle()

        assertThat(puzzle).isNotNull()
        assertThat(puzzle.targetMovieTitle).isNotEmpty()
        assertThat(puzzle.clues).hasSize(5)
    }

    @Test
    fun `isTodayPuzzleCompleted returns false when no game played today`() = runTest {
        every { mockStorage.data } returns flowOf(emptyPreferences())

        val isCompleted = repository.isTodayPuzzleCompleted()
        assertThat(isCompleted).isFalse()
    }
}
