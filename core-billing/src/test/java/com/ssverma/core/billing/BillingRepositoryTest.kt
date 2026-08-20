package com.ssverma.core.billing

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.BillingState
import com.ssverma.core.billing.model.ProStatus
import com.ssverma.core.billing.model.ProductType
import com.ssverma.core.billing.model.PurchaseResult
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BillingRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockBillingClientWrapper: BillingClientWrapper = mockk(relaxed = true)
    private val mockDebugConfigManager: com.ssverma.core.storage.debug.DebugConfigManager = mockk(relaxed = true)

    private val proStatusFlow = MutableStateFlow<ProStatus>(ProStatus.Inactive)
    private val billingStateFlow = MutableStateFlow<BillingState>(BillingState.Connected)
    private val purchaseEventsFlow = MutableSharedFlow<PurchaseResult>()
    private val debugProOverrideFlow = MutableStateFlow(com.ssverma.core.storage.debug.DebugProOverride.AUTO)

    private lateinit var repository: BillingRepository

    @Before
    fun setUp() {
        coEvery { mockBillingClientWrapper.proStatus } returns proStatusFlow
        coEvery { mockBillingClientWrapper.billingState } returns billingStateFlow
        coEvery { mockBillingClientWrapper.purchaseEvents } returns purchaseEventsFlow
        coEvery { mockDebugConfigManager.proOverride } returns debugProOverrideFlow

        repository = BillingRepositoryImpl(
            billingClientWrapper = mockBillingClientWrapper,
            debugConfigManager = mockDebugConfigManager
        )
    }

    @Test
    fun `isProActive emits false initially when proStatus is Inactive`() = runTest {
        repository.isProActive.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `isProActive emits true when proStatus transitions to Active`() = runTest {
        repository.isProActive.test {
            assertThat(awaitItem()).isFalse()

            proStatusFlow.value = ProStatus.Active(
                productId = BillingConstants.SKU_PRO_LIFETIME,
                purchaseToken = "test_token",
                isLifetime = true
            )

            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `getAvailableProducts returns list from wrapper`() = runTest {
        val expectedProducts = listOf(
            BillingProduct(
                id = BillingConstants.SKU_PRO_LIFETIME,
                name = "ShowTime Pro Lifetime",
                description = "Lifetime access",
                formattedPrice = "$4.99",
                priceAmountMicros = 4990000,
                priceCurrencyCode = "USD",
                productType = ProductType.INAPP
            )
        )
        coEvery { mockBillingClientWrapper.queryAvailableProducts() } returns expectedProducts

        val actualProducts = repository.getAvailableProducts()

        assertThat(actualProducts).isEqualTo(expectedProducts)
    }

    @Test
    fun `restorePurchases returns true on successful refresh`() = runTest {
        coEvery { mockBillingClientWrapper.refreshPurchases() } returns true

        val result = repository.restorePurchases()

        assertThat(result).isTrue()
    }

    @Test
    fun `isProActive emits true when proOverride is FORCE_ACTIVE even if proStatus is Inactive`() = runTest {
        repository.isProActive.test {
            assertThat(awaitItem()).isFalse()

            debugProOverrideFlow.value = com.ssverma.core.storage.debug.DebugProOverride.FORCE_ACTIVE
            assertThat(awaitItem()).isTrue()
        }
    }
}
