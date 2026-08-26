package com.ssverma.feature.account.ui.backup

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.shared.testing.fakes.FakeBackupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BackupSyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val fakeBackupRepository = FakeBackupRepository()

    private lateinit var viewModel: BackupSyncViewModel

    @Before
    fun setUp() {
        viewModel = BackupSyncViewModel(
            backupRepository = fakeBackupRepository,
            billingRepository = fakeBillingRepository
        )
    }

    @Test
    fun `initial state reflects unauthenticated and idle backup status`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.googleUser).isNull()
            assertThat(state.backupStatus).isEqualTo(BackupStatus.Idle)
            assertThat(state.backupFrequency).isEqualTo(BackupFrequency.OFF)
            assertThat(state.backupOverWifiOnly).isTrue()
        }
    }

    @Test
    fun `signOutGoogle updates state and message`() = runTest {
        fakeBackupRepository.setGoogleUser(
            GoogleUser(
                email = "alex@gmail.com",
                displayName = "Alex Verma",
                photoUrl = null,
                idToken = "fake_id_token"
            )
        )

        viewModel.signOutGoogle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.message).isInstanceOf(UiText.StaticText::class.java)
            val staticText = state.message as UiText.StaticText
            assertThat(staticText.resId).isEqualTo(R.string.google_signed_out)
        }
    }

    @Test
    fun `backupNow emits success message on success`() = runTest {
        fakeBackupRepository.setGoogleUser(
            GoogleUser(
                email = "alex@gmail.com",
                displayName = "Alex Verma",
                photoUrl = null,
                idToken = "fake_id_token"
            )
        )

        viewModel.backupNow()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.message).isInstanceOf(UiText.StaticText::class.java)
            val staticText = state.message as UiText.StaticText
            assertThat(staticText.resId).isEqualTo(R.string.backup_success)
        }
    }

    @Test
    fun `onBackupFrequencySelected updates backup repository frequency`() = runTest {
        viewModel.onBackupFrequencySelected(BackupFrequency.DAILY)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.backupFrequency).isEqualTo(BackupFrequency.DAILY)
        }
    }

    @Test
    fun `onBackupOverWifiOnlyChanged updates backup repository wifi setting`() = runTest {
        viewModel.onBackupOverWifiOnlyChanged(false)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.backupOverWifiOnly).isFalse()
        }
    }
}
