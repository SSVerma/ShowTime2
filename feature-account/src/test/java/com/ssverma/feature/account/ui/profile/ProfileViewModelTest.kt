package com.ssverma.feature.account.ui.profile

import android.app.Activity
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import com.ssverma.core.testing.fakes.FakeAppConfigRepository
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.model.AuthState
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

            // State changes during restore
            val restoringState = awaitItem()
            assertThat(restoringState.isRestoringPurchases).isTrue()

            val successState = awaitItem()
            assertThat(successState.isRestoringPurchases).isFalse()
            assertThat(successState.message).isEqualTo("Pro purchase successfully restored!")
            assertThat(successState.isProActive).isTrue()
        }
    }
}
