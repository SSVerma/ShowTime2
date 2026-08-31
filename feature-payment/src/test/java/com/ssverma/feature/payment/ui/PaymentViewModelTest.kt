package com.ssverma.feature.payment.ui

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import com.ssverma.core.testing.fakes.FakeBillingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val fakeAppConfigProvider = FakeAppConfigProvider()

    private lateinit var viewModel: PaymentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeAppConfigProvider.setBoolean("show_pro_paywall_enabled", true)
        viewModel = PaymentViewModel(
            billingRepository = fakeBillingRepository,
            appConfigProvider = fakeAppConfigProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads products and reflects free user status`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isProActive).isFalse()
        assertThat(state.isPaywallRemoteEnabled).isTrue()
        assertThat(state.products).isNotEmpty()
    }

    @Test
    fun `isProActive updates when pro status changes in billing repository`() = runTest {
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isProActive).isFalse()

        fakeBillingRepository.setProActive(true)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isProActive).isTrue()
    }
}
