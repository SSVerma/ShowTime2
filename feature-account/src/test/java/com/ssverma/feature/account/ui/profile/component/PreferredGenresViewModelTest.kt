package com.ssverma.feature.account.ui.profile.component

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.repository.FilterRepository
import com.ssverma.shared.testing.fakes.FakeAppConfigRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferredGenresViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val filterRepository: FilterRepository = mockk(relaxed = true)
    private val fakeAppConfigRepository = FakeAppConfigRepository()

    private val testGenres = listOf(
        Genre(id = 28, name = "Action"),
        Genre(id = 12, name = "Adventure"),
        Genre(id = 16, name = "Animation"),
        Genre(id = 35, name = "Comedy"),
        Genre(id = 80, name = "Crime"),
        Genre(id = 878, name = "Sci-Fi")
    )

    private lateinit var viewModel: PreferredGenresViewModel

    @Before
    fun setUp() = runTest {
        coEvery { filterRepository.fetchMovieGenres() } returns Result.Success(testGenres)
        fakeAppConfigRepository.updateSeededGenres(setOf(28, 12, 878))

        viewModel = PreferredGenresViewModel(
            filterRepository = filterRepository,
            appConfigRepository = fakeAppConfigRepository
        )
    }

    @Test
    fun `initial state loads genres and reflects saved seeded genres`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.genresState).isInstanceOf(UiState.Success::class.java)
        val loadedGenres = (state.genresState as UiState.Success).data
        assertThat(loadedGenres).hasSize(6)
        assertThat(state.selectedGenreIds).containsExactly(28, 12, 878)
        assertThat(state.selectedCount).isEqualTo(3)
        assertThat(state.isThresholdMet).isTrue()
        assertThat(state.isMaxReached).isFalse()
        assertThat(state.canSave).isTrue()
    }

    @Test
    fun `toggleGenre adds and removes genres within 3 to 5 range`() = runTest {
        advanceUntilIdle()

        // Deselect genre 28
        viewModel.toggleGenre(28)
        assertThat(viewModel.uiState.value.selectedGenreIds).containsExactly(12, 878)
        assertThat(viewModel.uiState.value.selectedCount).isEqualTo(2)
        assertThat(viewModel.uiState.value.isThresholdMet).isFalse()
        assertThat(viewModel.uiState.value.canSave).isFalse()

        // Re-select genre 28
        viewModel.toggleGenre(28)
        assertThat(viewModel.uiState.value.selectedGenreIds).containsExactly(12, 878, 28)
        assertThat(viewModel.uiState.value.canSave).isTrue()
    }

    @Test
    fun `toggleGenre enforces maximum cap of 5 genres`() = runTest {
        advanceUntilIdle()

        viewModel.toggleGenre(16) // 4 selected
        viewModel.toggleGenre(35) // 5 selected (max)
        assertThat(viewModel.uiState.value.selectedCount).isEqualTo(5)
        assertThat(viewModel.uiState.value.isMaxReached).isTrue()
        assertThat(viewModel.uiState.value.canSave).isTrue()

        // Attempting to select 6th genre (80) is ignored
        viewModel.toggleGenre(80)
        assertThat(viewModel.uiState.value.selectedCount).isEqualTo(5)
        assertThat(viewModel.uiState.value.selectedGenreIds).containsExactly(28, 12, 878, 16, 35)

        // Deselecting one allows another
        viewModel.toggleGenre(35)
        assertThat(viewModel.uiState.value.selectedCount).isEqualTo(4)
        viewModel.toggleGenre(80)
        assertThat(viewModel.uiState.value.selectedCount).isEqualTo(5)
        assertThat(viewModel.uiState.value.selectedGenreIds).containsExactly(28, 12, 878, 16, 80)
    }

    @Test
    fun `savePreferences updates repository and emits GenresSaved effect`() = runTest {
        advanceUntilIdle()

        viewModel.toggleGenre(16) // now 28, 12, 878, 16

        viewModel.uiEffect.test {
            viewModel.savePreferences()
            advanceUntilIdle()

            val effect = awaitItem()
            assertThat(effect).isEqualTo(PreferredGenresUiEffect.GenresSaved)

            val persisted = fakeAppConfigRepository.userSeededGenres.first()
            assertThat(persisted).containsExactly(28, 12, 878, 16)
        }
    }

    @Test
    fun `savePreferences does not proceed when selection is below minimum threshold`() = runTest {
        advanceUntilIdle()

        viewModel.toggleGenre(28) // down to 2 genres: 12, 878
        assertThat(viewModel.uiState.value.canSave).isFalse()

        viewModel.savePreferences()
        advanceUntilIdle()

        // Repository should still have original 3
        val persisted = fakeAppConfigRepository.userSeededGenres.first()
        assertThat(persisted).containsExactly(28, 12, 878)
    }
}
