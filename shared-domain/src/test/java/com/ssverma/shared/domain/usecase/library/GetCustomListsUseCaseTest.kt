package com.ssverma.shared.domain.usecase.library

import com.ssverma.shared.domain.model.library.CustomList
import com.ssverma.shared.domain.repository.LibraryRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCustomListsUseCaseTest {

    private val libraryRepository: LibraryRepository = mockk(relaxed = true)
    private lateinit var getCustomListsUseCase: GetCustomListsUseCase

    @Before
    fun setUp() {
        getCustomListsUseCase = GetCustomListsUseCase(libraryRepository = libraryRepository)
    }

    @Test
    fun `invoke delegates to libraryRepository getCustomListsFlow`() = runTest {
        val expectedLists = listOf(
            CustomList(
                listId = "list_1",
                title = "Sci-Fi Favorites",
                description = "Best sci-fi movies",
                isPublic = false,
                items = emptyList(),
                createdAt = 1000L,
                updatedAt = 2000L
            )
        )
        every { libraryRepository.getCustomListsFlow() } returns flowOf(expectedLists)

        val result = getCustomListsUseCase().first()

        assertEquals(expectedLists, result)
    }
}
