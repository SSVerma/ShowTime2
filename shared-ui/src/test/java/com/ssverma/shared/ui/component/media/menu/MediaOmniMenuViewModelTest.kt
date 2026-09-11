package com.ssverma.shared.ui.component.media.menu

import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.usecase.diary.GetDiaryEntriesUseCase
import com.ssverma.shared.domain.usecase.diary.SaveDiaryEntryUseCase
import com.ssverma.shared.testing.fakes.FakeDiaryRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MediaOmniMenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeDiaryRepository: FakeDiaryRepository
    private lateinit var viewModel: MediaOmniMenuViewModel

    @Before
    fun setUp() {
        fakeLibraryRepository = FakeLibraryRepository()
        fakeDiaryRepository = FakeDiaryRepository()
        viewModel = MediaOmniMenuViewModel(
            libraryRepository = fakeLibraryRepository,
            saveDiaryEntryUseCase = SaveDiaryEntryUseCase(fakeDiaryRepository),
            getDiaryEntriesUseCase = GetDiaryEntriesUseCase(fakeDiaryRepository)
        )
    }

    @Test
    fun `getDiaryEntries returns empty list when title has not been logged`() = runTest {
        val entries = viewModel.getDiaryEntries(mediaId = 42, mediaType = MediaType.Movie).first()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun `saving a new diary entry creates single entry and updates on second save`() = runTest {
        val newEntry = DiaryEntry(
            id = 0L,
            mediaId = 42,
            mediaType = MediaType.Movie,
            title = "Inception",
            posterImageUrl = "/inception.jpg",
            userRating = 4.5f,
            review = "Mind-bending masterpiece"
        )

        viewModel.saveDiaryEntry(newEntry)
        advanceUntilIdle()

        val savedEntries =
            viewModel.getDiaryEntries(mediaId = 42, mediaType = MediaType.Movie).first()
        assertEquals(1, savedEntries.size)
        val existingEntry = savedEntries.first()
        assertEquals("Inception", existingEntry.title)
        assertEquals(4.5f, existingEntry.userRating)
        assertEquals("Mind-bending masterpiece", existingEntry.review)
        assertTrue("Generated ID must be greater than 0", existingEntry.id > 0L)

        // Second tap / update: using existing entry's ID
        val updatedEntry = existingEntry.copy(
            userRating = 5.0f,
            review = "Even better upon rewatch!",
            isRewatch = true
        )

        viewModel.saveDiaryEntry(updatedEntry)
        advanceUntilIdle()

        val afterUpdateEntries =
            viewModel.getDiaryEntries(mediaId = 42, mediaType = MediaType.Movie).first()
        assertEquals(
            "Must not create duplicate entries for the same media",
            1,
            afterUpdateEntries.size
        )
        val finalEntry = afterUpdateEntries.first()
        assertEquals(existingEntry.id, finalEntry.id)
        assertEquals(5.0f, finalEntry.userRating)
        assertEquals("Even better upon rewatch!", finalEntry.review)
        assertTrue(finalEntry.isRewatch)
    }
}
