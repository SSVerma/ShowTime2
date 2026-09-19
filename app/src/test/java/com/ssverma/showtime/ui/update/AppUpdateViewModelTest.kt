package com.ssverma.showtime.ui.update

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.shared.domain.model.AppUpdateStatus
import com.ssverma.shared.domain.repository.AppUpdateRepository
import io.mockk.coVerify
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
class AppUpdateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appUpdateRepository: AppUpdateRepository = mockk(relaxed = true)
    private val updateStatusFlow = MutableStateFlow<AppUpdateStatus>(AppUpdateStatus.UpToDate)

    private lateinit var viewModel: AppUpdateViewModel

    @Before
    fun setUp() {
        every { appUpdateRepository.observeAppUpdateStatus(any()) } returns updateStatusFlow
    }

    @Test
    fun `initial uiState is UpToDate when repository emits UpToDate`() = runTest {
        viewModel = AppUpdateViewModel(appUpdateRepository)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.status).isEqualTo(AppUpdateStatus.UpToDate)
        assertThat(viewModel.uiState.value.showSoftUpdatePrompt).isFalse()
    }

    @Test
    fun `uiState reflects ForceUpdate when repository emits ForceUpdate`() = runTest {
        val forceUpdate = AppUpdateStatus.ForceUpdate(
            currentVersionCode = 20L,
            minSupportedVersionCode = 25L,
            title = "Critical Update",
            message = "Update required"
        )
        updateStatusFlow.value = forceUpdate

        viewModel = AppUpdateViewModel(appUpdateRepository)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.status).isEqualTo(forceUpdate)
        assertThat(viewModel.uiState.value.showSoftUpdatePrompt).isFalse()
    }

    @Test
    fun `uiState shows soft update prompt when repository emits SoftUpdate`() = runTest {
        val softUpdate = AppUpdateStatus.SoftUpdate(
            currentVersionCode = 20L,
            latestVersionCode = 25L,
            title = "New Version Available"
        )
        updateStatusFlow.value = softUpdate

        viewModel = AppUpdateViewModel(appUpdateRepository)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.status).isEqualTo(softUpdate)
        assertThat(viewModel.uiState.value.showSoftUpdatePrompt).isTrue()
    }

    @Test
    fun `dismissSoftUpdate hides prompt and notifies repository`() = runTest {
        val softUpdate = AppUpdateStatus.SoftUpdate(
            currentVersionCode = 20L,
            latestVersionCode = 25L
        )
        updateStatusFlow.value = softUpdate

        viewModel = AppUpdateViewModel(appUpdateRepository)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showSoftUpdatePrompt).isTrue()

        viewModel.dismissSoftUpdate(versionCode = 25L)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showSoftUpdatePrompt).isFalse()
        coVerify { appUpdateRepository.dismissSoftUpdate(25L) }
    }
}
