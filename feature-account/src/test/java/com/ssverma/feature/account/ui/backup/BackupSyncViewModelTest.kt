package com.ssverma.feature.account.ui.backup

import android.app.Activity
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.ads.quota.RewardPassStatus
import com.ssverma.core.backup.model.BackupFrequency
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleSignInCancelledException
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.shared.testing.fakes.FakeBackupRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BackupSyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val fakeBackupRepository = FakeBackupRepository()
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private val passStatusFlow = MutableStateFlow(RewardPassStatus())

    private lateinit var viewModel: BackupSyncViewModel

    @Before
    fun setUp() {
        every { mockRewardManager.passStatus } returns passStatusFlow
        coEvery { mockRewardManager.isAutoBackupAllowed(any()) } returns true

        viewModel = BackupSyncViewModel(
            backupRepository = fakeBackupRepository,
            billingRepository = fakeBillingRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager
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
    fun `onAttemptBackupNow for free user triggers manual backup gate`() = runTest {
        viewModel.onAttemptBackupNow()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isManualBackupGateVisible).isTrue()
        }
    }

    @Test
    fun `onAttemptBackupNow for pro user executes backup immediately`() = runTest {
        fakeBillingRepository.setProActive(true)

        viewModel.onAttemptBackupNow()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isManualBackupGateVisible).isFalse()
        }
    }

    @Test
    fun `dismissManualBackupGate closes manual backup gate`() = runTest {
        viewModel.onAttemptBackupNow()
        assertThat(viewModel.uiState.value.isManualBackupGateVisible).isTrue()

        viewModel.dismissManualBackupGate()
        assertThat(viewModel.uiState.value.isManualBackupGateVisible).isFalse()
    }

    @Test
    fun `onBackupFrequencySelected with OFF allows turning off for free user`() = runTest {
        viewModel.onBackupFrequencySelected(BackupFrequency.OFF)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.backupFrequency).isEqualTo(BackupFrequency.OFF)
            assertThat(state.isAutoBackupPaywallVisible).isFalse()
        }
    }

    @Test
    fun `onBackupFrequencySelected for free user triggers auto-backup paywall`() = runTest {
        viewModel.onBackupFrequencySelected(BackupFrequency.DAILY)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isAutoBackupPaywallVisible).isTrue()
        }
    }

    @Test
    fun `onBackupFrequencySelected for pro user updates frequency`() = runTest {
        fakeBillingRepository.setProActive(true)

        viewModel.onBackupFrequencySelected(BackupFrequency.WEEKLY)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.backupFrequency).isEqualTo(BackupFrequency.WEEKLY)
            assertThat(state.isAutoBackupPaywallVisible).isFalse()
        }
    }

    @Test
    fun `dismissAutoBackupPaywall resets paywall state`() = runTest {
        viewModel.onBackupFrequencySelected(BackupFrequency.DAILY)
        assertThat(viewModel.uiState.value.isAutoBackupPaywallVisible).isTrue()

        viewModel.dismissAutoBackupPaywall()
        assertThat(viewModel.uiState.value.isAutoBackupPaywallVisible).isFalse()
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
    fun `onBackupOverWifiOnlyChanged updates backup repository wifi setting`() = runTest {
        viewModel.onBackupOverWifiOnlyChanged(false)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.backupOverWifiOnly).isFalse()
        }
    }

    @Test
    fun `signInWithGoogle on cancellation resets loading without error message`() = runTest {
        val mockActivity: Activity = mockk(relaxed = true)
        fakeBackupRepository.signInFailureException = GoogleSignInCancelledException()

        viewModel.signInWithGoogle(mockActivity)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isSigningIn).isFalse()
            assertThat(state.message).isNull()
        }
    }

    @Test
    fun `signInWithGoogle on unexpected error sets error message and resets loading`() = runTest {
        val mockActivity: Activity = mockk(relaxed = true)
        fakeBackupRepository.signInFailureException = RuntimeException("Network timeout")

        viewModel.signInWithGoogle(mockActivity)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isSigningIn).isFalse()
            assertThat(state.message).isInstanceOf(UiText.StaticText::class.java)
            val staticText = state.message as UiText.StaticText
            assertThat(staticText.resId).isEqualTo(R.string.google_sign_in_failed)
        }
    }
}
