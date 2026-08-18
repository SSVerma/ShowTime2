package com.ssverma.feature.account.ui.profile

import android.app.Activity
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.backup.model.BackupOperation
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.shared.testing.fakes.FakeAppConfigRepository
import com.ssverma.shared.testing.fakes.FakeBackupRepository
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.shared.domain.model.AppTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val fakeBackupRepository = FakeBackupRepository()
    private val fakeAppConfigRepository = FakeAppConfigRepository(initialTheme = AppTheme.System)
    private val fakeAppConfigProvider = FakeAppConfigProvider()

    private val mockAccountRepository: AccountRepository = mockk(relaxed = true)
    private val mockAuthManager: AuthManager = mockk(relaxed = true)
    private val authFlow = MutableSharedFlow<AuthState>(replay = 1)

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        coEvery { mockAuthManager.authFlow } returns authFlow
        authFlow.tryEmit(AuthState.Unauthorized)

        viewModel = ProfileViewModel(
            accountRepository = mockAccountRepository,
            authManager = mockAuthManager,
            billingRepository = fakeBillingRepository,
            backupRepository = fakeBackupRepository,
            appConfigRepository = fakeAppConfigRepository,
            appConfigProvider = fakeAppConfigProvider
        )
    }

    @Test
    fun `initial state reflects unauthenticated guest and free user`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isProActive).isFalse()
            assertThat(state.isPaywallVisible).isFalse()
            assertThat(state.isRestoringPurchases).isFalse()
            assertThat(state.currentTheme).isEqualTo(AppTheme.System)
            assertThat(state.googleUser).isNull()
            assertThat(state.backupStatus).isEqualTo(BackupStatus.Idle)
            assertThat(state.profileContent).isInstanceOf(ProfileContentState.Success::class.java)

            val profile = (state.profileContent as ProfileContentState.Success).profile
            assertThat(profile.userName).isEqualTo("guest")
        }
    }

    @Test
    fun `openPaywall sets isPaywallVisible to true`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.isPaywallVisible).isFalse()

            viewModel.openPaywall()
            val updated = awaitItem()
            assertThat(updated.isPaywallVisible).isTrue()
        }
    }

    @Test
    fun `dismissPaywall sets isPaywallVisible to false`() = runTest {
        viewModel.openPaywall()

        viewModel.uiState.test {
            assertThat(awaitItem().isPaywallVisible).isTrue()

            viewModel.dismissPaywall()
            assertThat(awaitItem().isPaywallVisible).isFalse()
        }
    }

    @Test
    fun `updateTheme updates currentTheme via AppConfigRepository`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem().currentTheme).isEqualTo(AppTheme.System)

            viewModel.updateTheme(AppTheme.Dark)
            assertThat(awaitItem().currentTheme).isEqualTo(AppTheme.Dark)
        }
    }

    @Test
    fun `purchaseProduct triggers billing purchase and unlocks Pro`() = runTest {
        val mockActivity: Activity = mockk(relaxed = true)
        val product = fakeBillingRepository.getAvailableProducts().first()

        viewModel.uiState.test {
            assertThat(awaitItem().isProActive).isFalse()

            viewModel.purchaseProduct(mockActivity, product)
            assertThat(awaitItem().isProActive).isTrue()
        }
    }

    @Test
    fun `restorePurchases displays success message on successful restore`() = runTest {
        fakeBillingRepository.restoreSuccessToReturn = true

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.message).isNull()

            viewModel.restorePurchases()

            val restoringState = awaitItem()
            assertThat(restoringState.isRestoringPurchases).isTrue()

            val successState = awaitItem()
            assertThat(successState.isRestoringPurchases).isFalse()
            assertThat(successState.message).isEqualTo(UiText.StaticText(R.string.restore_success))
            assertThat(successState.isProActive).isTrue()
        }
    }

    @Test
    fun `signInWithGoogle updates googleUser and displays welcome message`() = runTest {
        val mockActivity: Activity = mockk(relaxed = true)

        viewModel.uiState.test {
            assertThat(awaitItem().googleUser).isNull()

            viewModel.signInWithGoogle(mockActivity)

            val updatedState = awaitItem()
            assertThat(updatedState.googleUser).isNotNull()
            assertThat(updatedState.googleUser?.displayName).isEqualTo("Test User")
            assertThat(updatedState.message).isEqualTo(UiText.StaticText(R.string.google_sign_in_success, "Test User"))
        }
    }

    @Test
    fun `signOutGoogle clears googleUser and displays signed out message`() = runTest {
        val mockActivity: Activity = mockk(relaxed = true)
        viewModel.signInWithGoogle(mockActivity)

        viewModel.uiState.test {
            assertThat(awaitItem().googleUser).isNotNull()

            viewModel.signOutGoogle()

            val updatedState = awaitItem()
            assertThat(updatedState.googleUser).isNull()
            assertThat(updatedState.message).isEqualTo(UiText.StaticText(R.string.google_signed_out))
        }
    }

    @Test
    fun `backupNow creates backup snapshot and emits success message`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem().backupStatus).isEqualTo(BackupStatus.Idle)

            viewModel.backupNow()

            val successState = awaitItem()
            assertThat(successState.backupStatus).isInstanceOf(BackupStatus.Success::class.java)
            assertThat((successState.backupStatus as BackupStatus.Success).operation).isEqualTo(BackupOperation.BACKUP)
            assertThat(successState.lastBackupMetadata).isNotNull()
            assertThat(successState.message).isEqualTo(UiText.StaticText(R.string.backup_success))
        }
    }

    @Test
    fun `restoreBackup restores snapshot and emits success message`() = runTest {
        viewModel.backupNow() // Create initial backup

        viewModel.uiState.test {
            assertThat(awaitItem().lastBackupMetadata).isNotNull()

            viewModel.restoreBackup()

            val successState = awaitItem()
            assertThat(successState.backupStatus).isInstanceOf(BackupStatus.Success::class.java)
            assertThat((successState.backupStatus as BackupStatus.Success).operation).isEqualTo(BackupOperation.RESTORE)
            assertThat(successState.message).isEqualTo(UiText.StaticText(R.string.restore_success_msg))
        }
    }
}
