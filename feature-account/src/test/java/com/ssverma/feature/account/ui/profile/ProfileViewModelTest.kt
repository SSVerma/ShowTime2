package com.ssverma.feature.account.ui.profile

import android.app.Activity
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.backup.model.BackupStatus
import com.ssverma.core.backup.model.GoogleSignInCancelledException
import com.ssverma.core.storage.debug.DebugConfigManager
import com.ssverma.core.storage.debug.DebugProOverride
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.core.ui.UiText
import com.ssverma.feature.account.R
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.account.domain.seeder.DatabaseSeeder
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.testing.fakes.FakeAppConfigRepository
import com.ssverma.shared.testing.fakes.FakeBackupRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Optional

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val fakeBackupRepository = FakeBackupRepository()
    private val fakeAppConfigRepository = FakeAppConfigRepository(initialTheme = AppTheme.System)
    private val fakeAppConfigProvider = FakeAppConfigProvider()
    private val fakeConfigurationRepository =
        com.ssverma.shared.testing.fakes.FakeConfigurationRepository()

    private val mockAccountRepository: AccountRepository = mockk(relaxed = true)
    private val mockAuthManager: AuthManager = mockk(relaxed = true)
    private val mockTraktAuthManager: TraktAuthManager = mockk(relaxed = true)
    private val mockDebugConfigManager: DebugConfigManager = mockk(relaxed = true)
    private val mockDatabaseSeeder: DatabaseSeeder = mockk(relaxed = true)

    private val authFlow = MutableSharedFlow<AuthState>(replay = 1)
    private val traktAuthFlow = MutableStateFlow<TraktAuthState>(TraktAuthState.Disconnected)

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        coEvery { mockAuthManager.authFlow } returns authFlow
        authFlow.tryEmit(AuthState.Unauthorized)
        every { mockTraktAuthManager.authState } returns traktAuthFlow
        every { mockDebugConfigManager.proOverride } returns MutableStateFlow(DebugProOverride.AUTO)
        every { mockDebugConfigManager.isMockTraktEnabled } returns MutableStateFlow(true)
        every { mockDebugConfigManager.customTraktClientId } returns MutableStateFlow("")
        every { mockDebugConfigManager.isAdsDisabled } returns MutableStateFlow(false)

        viewModel = ProfileViewModel(
            accountRepository = mockAccountRepository,
            authManager = mockAuthManager,
            billingRepository = fakeBillingRepository,
            backupRepository = fakeBackupRepository,
            appConfigRepository = fakeAppConfigRepository,
            configurationRepository = fakeConfigurationRepository,
            appConfigProvider = fakeAppConfigProvider,
            traktAuthManager = mockTraktAuthManager,
            debugConfigManager = mockDebugConfigManager,
            optionalDatabaseSeeder = Optional.of(mockDatabaseSeeder)
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
    fun `openPaywall sets paywall visibility to true`() = runTest {
        viewModel.openPaywall()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isPaywallVisible).isTrue()
        }
    }

    @Test
    fun `dismissPaywall sets paywall visibility to false`() = runTest {
        viewModel.openPaywall()
        viewModel.dismissPaywall()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isPaywallVisible).isFalse()
        }
    }

    @Test
    fun `restorePurchases sets message on success`() = runTest {
        fakeBillingRepository.restoreSuccessToReturn = true

        viewModel.restorePurchases()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isRestoringPurchases).isFalse()
            assertThat(state.message).isInstanceOf(UiText.StaticText::class.java)
            val staticText = state.message as UiText.StaticText
            assertThat(staticText.resId).isEqualTo(R.string.restore_success)
        }
    }

    @Test
    fun `updateTheme delegates to AppConfigRepository`() = runTest {
        viewModel.updateTheme(AppTheme.Dark)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentTheme).isEqualTo(AppTheme.Dark)
        }
    }

    @Test
    fun `logout delegates to authManager and accountRepository`() = runTest {
        viewModel.logout()

        coVerify {
            mockAuthManager.logout()
            mockAccountRepository.removeUserAccount()
        }
    }

    @Test
    fun `populateDemoFavorites delegates to databaseSeeder`() = runTest {
        viewModel.populateDemoFavorites()

        coVerify {
            mockDatabaseSeeder.seedFavorites()
        }
    }

    @Test
    fun `populateDemoWatchlist delegates to databaseSeeder`() = runTest {
        viewModel.populateDemoWatchlist()

        coVerify {
            mockDatabaseSeeder.seedWatchlist()
        }
    }

    @Test
    fun `populateDemoHistory delegates to databaseSeeder`() = runTest {
        viewModel.populateDemoHistory()

        coVerify {
            mockDatabaseSeeder.seedHistory()
        }
    }

    @Test
    fun `clearLocalDatabase delegates to databaseSeeder`() = runTest {
        viewModel.clearLocalDatabase()

        coVerify {
            mockDatabaseSeeder.clearDatabase()
        }
    }

    @Test
    fun `resetCinemaGame delegates to databaseSeeder`() = runTest {
        viewModel.resetCinemaGame()

        coVerify {
            mockDatabaseSeeder.resetCinemaGame()
        }
    }

    @Test
    fun `signOutGoogle updates google user and message`() = runTest {
        viewModel.signOutGoogle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.googleUser).isNull()
            assertThat(state.guestPseudonym).startsWith("Cinephile #")
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
        fakeBackupRepository.signInFailureException = RuntimeException("Network down")

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
