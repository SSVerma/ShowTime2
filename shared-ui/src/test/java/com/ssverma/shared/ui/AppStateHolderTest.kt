package com.ssverma.shared.ui

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.shared.domain.model.AppTheme
import com.ssverma.shared.testing.fakes.FakeAppConfigRepository
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

    private lateinit var appStateHolder: AppStateHolder
    private lateinit var testScope: TestScope

    @Before
    fun setUp() {
        testScope = TestScope(mainDispatcherRule.testDispatcher)
        appStateHolder = AppStateHolder(
            appConfigRepository = fakeAppConfigRepository,
            billingRepository = fakeBillingRepository,
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
    fun `updateAppTheme blocks OLED Midnight when isProActive is false`() = runTest {
        appStateHolder.appTheme.test {
            assertThat(awaitItem()).isEqualTo(AppTheme.System)

            // Attempt to update to OLED Midnight without Pro
            appStateHolder.updateAppTheme(AppTheme.OledMidnight)

            // Expect theme to remain System
            expectNoEvents()
        }
    }

    @Test
    fun `updateAppTheme allows OLED Midnight when isProActive is true`() = runTest {
        fakeBillingRepository.setProActive(true)

        appStateHolder.appTheme.test {
            assertThat(awaitItem()).isEqualTo(AppTheme.System)

            // Attempt to update to OLED Midnight with Pro active
            appStateHolder.updateAppTheme(AppTheme.OledMidnight)

            // Expect theme to update to OLED Midnight
            assertThat(awaitItem()).isEqualTo(AppTheme.OledMidnight)
        }
    }

    @Test
    fun `updateDynamicColor toggles dynamic color state`() = runTest {
        appStateHolder.isDynamicColorEnabled.test {
            assertThat(awaitItem()).isFalse()

            appStateHolder.updateDynamicColor(true)
            assertThat(awaitItem()).isTrue()
        }
    }
}
