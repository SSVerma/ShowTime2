package com.ssverma.showtime.ui.onboarding

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.backup.BackupRepository
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.repository.FilterRepository
import com.ssverma.shared.domain.repository.WatchProviderRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val watchProviderRepository: WatchProviderRepository = mockk(relaxed = true)
    private val filterRepository: FilterRepository = mockk(relaxed = true)
    private val backupRepository: BackupRepository = mockk(relaxed = true)

    private val googleUserFlow = MutableStateFlow<com.ssverma.core.backup.model.GoogleUser?>(null)

    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        every { backupRepository.googleUser } returns googleUserFlow

        val testProviders = listOf(
            ProviderInfo(
                logoPath = "/netflix.jpg",
                providerId = 8,
                providerName = "Netflix",
                displayPriority = 0
            ),
            ProviderInfo(
                logoPath = "/prime.jpg",
                providerId = 9,
                providerName = "Prime Video",
                displayPriority = 1
            )
        )
        coEvery { watchProviderRepository.fetchAllMovieWatchProviders() } returns Result.Success(
            testProviders
        )

        val testGenres = listOf(
            Genre(id = 28, name = "Action"),
            Genre(id = 12, name = "Adventure"),
            Genre(id = 878, name = "Sci-Fi"),
            Genre(id = 35, name = "Comedy")
        )
        coEvery { filterRepository.fetchMovieGenres() } returns Result.Success(testGenres)

        viewModel = OnboardingViewModel(
            watchProviderRepository = watchProviderRepository,
            filterRepository = filterRepository,
            backupRepository = backupRepository
        )
    }

    @Test
    fun `initial state loads providers and genres correctly`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.streamingProviders).hasSize(2)
        assertThat(state.streamingProviders.map { it.providerId }).containsExactly(8, 9)
        assertThat(state.availableGenres).hasSize(4)
        assertThat(state.availableGenres.map { it.id }).containsExactly(28, 12, 878, 35)
        assertThat(state.selectedProviderIds).isEmpty()
        assertThat(state.selectedGenreIds).isEmpty()
        assertThat(state.canProceedFromTaste).isFalse()
    }

    @Test
    fun `toggleProvider adds and removes provider selection`() = runTest {
        advanceUntilIdle()

        viewModel.toggleProvider(8)
        assertThat(viewModel.uiState.value.selectedProviderIds).containsExactly(8)

        viewModel.toggleProvider(9)
        assertThat(viewModel.uiState.value.selectedProviderIds).containsExactly(8, 9)

        viewModel.toggleProvider(8)
        assertThat(viewModel.uiState.value.selectedProviderIds).containsExactly(9)
    }

    @Test
    fun `selectAllPopularProviders selects top providers`() = runTest {
        advanceUntilIdle()

        viewModel.selectAllPopularProviders()
        assertThat(viewModel.uiState.value.selectedProviderIds).containsExactly(8, 9)
    }

    @Test
    fun `clearSelectedProviders clears selection`() = runTest {
        advanceUntilIdle()

        viewModel.toggleProvider(8)
        viewModel.toggleProvider(9)
        assertThat(viewModel.uiState.value.selectedProviderIds).isNotEmpty()

        viewModel.clearSelectedProviders()
        assertThat(viewModel.uiState.value.selectedProviderIds).isEmpty()
    }

    @Test
    fun `toggleGenre validates minimum threshold of 3 genres`() = runTest {
        advanceUntilIdle()

        viewModel.toggleGenre(28)
        assertThat(viewModel.uiState.value.canProceedFromTaste).isFalse()

        viewModel.toggleGenre(12)
        assertThat(viewModel.uiState.value.canProceedFromTaste).isFalse()

        viewModel.toggleGenre(878)
        assertThat(viewModel.uiState.value.canProceedFromTaste).isTrue()

        viewModel.toggleGenre(878)
        assertThat(viewModel.uiState.value.canProceedFromTaste).isFalse()
    }
}
