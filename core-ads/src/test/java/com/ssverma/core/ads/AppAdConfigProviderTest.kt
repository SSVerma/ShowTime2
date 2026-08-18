package com.ssverma.core.ads

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.fakes.FakeAppConfigProvider
import com.ssverma.core.testing.fakes.FakeBillingRepository
import org.junit.Before
import org.junit.Test

class AppAdConfigProviderTest {

    private val fakeAppConfigProvider = FakeAppConfigProvider()
    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)

    private lateinit var appAdConfigProvider: AppAdConfigProvider

    @Before
    fun setUp() {
        appAdConfigProvider = AppAdConfigProvider(
            appConfigProvider = fakeAppConfigProvider,
            billingRepository = fakeBillingRepository
        )
    }

    @Test
    fun `isAdsEnabled returns true when remote ads enabled and user is free`() {
        fakeAppConfigProvider.setBoolean("remote_ads_enabled", true)
        fakeBillingRepository.setProActive(false)

        assertThat(appAdConfigProvider.isAdsEnabled).isTrue()
    }

    @Test
    fun `isAdsEnabled returns false when user is Pro even if remote ads enabled`() {
        fakeAppConfigProvider.setBoolean("remote_ads_enabled", true)
        fakeBillingRepository.setProActive(true)

        // Pro users must have 100% ad suppression
        assertThat(appAdConfigProvider.isAdsEnabled).isFalse()
    }

    @Test
    fun `isAdsEnabled returns false when remote kill switch disables ads`() {
        fakeAppConfigProvider.setBoolean("remote_ads_enabled", false)
        fakeBillingRepository.setProActive(false)

        assertThat(appAdConfigProvider.isAdsEnabled).isFalse()
    }
}
