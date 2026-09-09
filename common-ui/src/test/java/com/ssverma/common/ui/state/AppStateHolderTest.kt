package com.ssverma.common.ui.state

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.testing.fakes.FakeAppConfigRepository
import com.ssverma.shared.testing.fakes.FakeBackupRepository
import com.ssverma.shared.testing.fakes.FakeConfigurationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppStateHolderTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeAppConfigRepository = FakeAppConfigRepository()
    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val fakeBackupRepository = FakeBackupRepository()
    private val fakeConfigurationRepository = FakeConfigurationRepository()

    private lateinit var appStateHolder: AppStateHolder
    private lateinit var testScope: TestScope

    @Before
    fun setUp() {
        testScope = TestScope(mainDispatcherRule.testDispatcher)
        appStateHolder = AppStateHolder(
            appConfigRepository = fakeAppConfigRepository,
            configurationRepository = fakeConfigurationRepository,
            billingRepository = fakeBillingRepository,
            backupRepository = fakeBackupRepository,
            coroutineScope = testScope
        )
    }

    @Test
    fun `isProActive reflects billing repository status`() = runTest {
        appStateHolder.isProActive.test {
            assertThat(awaitItem()).isFalse()

            fakeBillingRepository.setProActive(true)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `updateAppTheme changes theme to Dark successfully`() = runTest {
        appStateHolder.appTheme.test {
            assertThat(awaitItem()).isEqualTo(AppTheme.System)

            appStateHolder.updateAppTheme(AppTheme.Dark)
            assertThat(awaitItem()).isEqualTo(AppTheme.Dark)
        }
    }

    @Test
    fun `updateAppTheme to OledMidnight ignored when pro is inactive`() = runTest {
        appStateHolder.appTheme.test {
            assertThat(awaitItem()).isEqualTo(AppTheme.System)

            appStateHolder.updateAppTheme(AppTheme.OledMidnight)
            expectNoEvents()
        }
    }

    @Test
    fun `updateAppTheme to OledMidnight succeeds when pro is active`() = runTest {
        fakeBillingRepository.setProActive(true)
        testScope.testScheduler.advanceUntilIdle()

        appStateHolder.appTheme.test {
            assertThat(awaitItem()).isEqualTo(AppTheme.System)

            appStateHolder.updateAppTheme(AppTheme.OledMidnight)
            assertThat(awaitItem()).isEqualTo(AppTheme.OledMidnight)
        }
    }

    @Test
    fun `updateDynamicColor updates dynamic color flow`() = runTest {
        appStateHolder.isDynamicColorEnabled.test {
            assertThat(awaitItem()).isFalse()

            appStateHolder.updateDynamicColor(true)
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `googleUser reflects backup repository status`() = runTest {
        appStateHolder.googleUser.test {
            assertThat(awaitItem()).isNull()

            val testUser = com.ssverma.core.backup.model.GoogleUser(
                email = "cinephile@showtime.app",
                displayName = "Cinephile",
                photoUrl = "https://example.com/avatar.png",
                idToken = "fake_id_token"
            )
            fakeBackupRepository.setGoogleUser(testUser)
            assertThat(awaitItem()).isEqualTo(testUser)
        }
    }
}
