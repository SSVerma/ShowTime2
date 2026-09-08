package com.ssverma.core.billing

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.billing.model.BillingProduct
import com.ssverma.core.billing.model.BillingState
import com.ssverma.core.billing.model.DebugProOverride
import com.ssverma.core.billing.model.ProOverrideProvider
import com.ssverma.core.billing.model.ProStatus
import com.ssverma.core.billing.model.ProductType
import com.ssverma.core.billing.model.PurchaseResult
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.ccm.AppConfigProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BillingRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockBillingClientWrapper: BillingClientWrapper = mockk(relaxed = true)
    private val mockProOverrideProvider: ProOverrideProvider = mockk(relaxed = true)
    private val mockAppConfigProvider: AppConfigProvider = mockk(relaxed = true)

    private val proStatusFlow = MutableStateFlow<ProStatus>(ProStatus.Inactive)
    private val billingStateFlow = MutableStateFlow<BillingState>(BillingState.Connected)
    private val purchaseEventsFlow = MutableSharedFlow<PurchaseResult>()
    private val debugProOverrideFlow = MutableStateFlow(DebugProOverride.AUTO)
    private val billingEnabledFlow = MutableStateFlow(true)

    private lateinit var repository: BillingRepository

    @Before
    fun setUp() {
        every {
            mockAppConfigProvider.observeBoolean(
                BillingRepositoryImpl.REMOTE_KEY_BILLING_ENABLED,
                true
            )
        } returns billingEnabledFlow
        every {
            mockAppConfigProvider.getBoolean(
                BillingRepositoryImpl.REMOTE_KEY_BILLING_ENABLED,
                true
            )
        } returns true
        coEvery { mockBillingClientWrapper.proStatus } returns proStatusFlow
        coEvery { mockBillingClientWrapper.billingState } returns billingStateFlow
        coEvery { mockBillingClientWrapper.purchaseEvents } returns purchaseEventsFlow
        coEvery { mockProOverrideProvider.proOverride } returns debugProOverrideFlow

        repository = BillingRepositoryImpl(
            billingClientWrapper = mockBillingClientWrapper,
            proOverrideProvider = mockProOverrideProvider,
            appConfigProvider = mockAppConfigProvider
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
    fun `isProActive emits true when proOverride is FORCE_ACTIVE even if proStatus is Inactive`() =
        runTest {
            repository.isProActive.test {
                assertThat(awaitItem()).isFalse()

                debugProOverrideFlow.value = DebugProOverride.FORCE_ACTIVE
                assertThat(awaitItem()).isTrue()
            }
        }

    @Test
    fun `isBillingEnabled emits false when remotely disabled and returns empty products`() =
        runTest {
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

            repository.isBillingEnabled.test {
                assertThat(awaitItem()).isTrue()

                billingEnabledFlow.value = false
                assertThat(awaitItem()).isFalse()

                val products = repository.getAvailableProducts()
                assertThat(products).isEmpty()
            }
        }
}
