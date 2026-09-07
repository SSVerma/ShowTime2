package com.ssverma.shared.data.repository

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ccm.AppConfigProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class AffiliateRepositoryTest {

    private val mockAppConfigProvider: AppConfigProvider = mockk(relaxed = true)
    private lateinit var repository: AffiliateRepositoryImpl

    @Before
    fun setUp() {
        repository = AffiliateRepositoryImpl(appConfigProvider = mockAppConfigProvider)
    }

    @Test
    fun getAffiliateConfig_whenConfigEmpty_returnsDefaultConfig() {
        every { mockAppConfigProvider.getString("affiliate_config", any()) } returns ""
        every { mockAppConfigProvider.getString("affiliate_config") } returns ""

        val config = repository.getAffiliateConfig()

        assertThat(config.amazonPrime.enabled).isFalse()
        assertThat(config.appleTv.enabled).isFalse()
        assertThat(config.justWatch.enabled).isFalse()
    }

    @Test
    fun getAffiliateConfig_whenConfigValidJson_parsesCorrectly() {
        val jsonStr = """
            {
                "amazonPrime": {
                    "enabled": true,
                    "tagDefault": "showtime-default-20",
                    "regionalTags": {
                        "US": "showtime-us-20",
                        "IN": "showtime-in-21"
                    }
                },
                "appleTv": {
                    "enabled": true,
                    "partnerToken": "1000l3b",
                    "campaignId": "showtime_android"
                },
                "justWatch": {
                    "enabled": true,
                    "partnerToken": "jw_partner_99",
                    "campaignId": "jw_campaign"
                }
            }
        """.trimIndent()

        every { mockAppConfigProvider.getString("affiliate_config", any()) } returns jsonStr
        every { mockAppConfigProvider.getString("affiliate_config") } returns jsonStr

        val config = repository.getAffiliateConfig()

        assertThat(config.amazonPrime.enabled).isTrue()
        assertThat(config.amazonPrime.tagDefault).isEqualTo("showtime-default-20")
        assertThat(config.amazonPrime.regionalTags["US"]).isEqualTo("showtime-us-20")
        assertThat(config.appleTv.enabled).isTrue()
        assertThat(config.appleTv.partnerToken).isEqualTo("1000l3b")
        assertThat(config.justWatch.enabled).isTrue()
        assertThat(config.justWatch.partnerToken).isEqualTo("jw_partner_99")
    }

    @Test
    fun buildProviderWatchUrl_forAmazon_withRegionalTag_injectsTag() {
        val jsonStr = """
            {
                "amazonPrime": {
                    "enabled": true,
                    "tagDefault": "default-tag-20",
                    "regionalTags": { "US": "us-tag-20" }
                }
            }
        """.trimIndent()
        every { mockAppConfigProvider.getString("affiliate_config", any()) } returns jsonStr
        every { mockAppConfigProvider.getString("affiliate_config") } returns jsonStr

        val watchUrl = repository.buildProviderWatchUrl(
            providerId = AffiliateRepositoryImpl.PROVIDER_AMAZON_PRIME_ALT,
            mediaTitle = "Inception",
            region = "US"
        )

        assertThat(watchUrl).contains("amazon.com/s?k=Inception")
        assertThat(watchUrl).contains("tag=us-tag-20")
    }

    @Test
    fun buildProviderWatchUrl_forAppleTv_injectsPartnerAndCampaignTokens() {
        val jsonStr = """
            {
                "appleTv": {
                    "enabled": true,
                    "partnerToken": "apple_token_123",
                    "campaignId": "spring_campaign"
                }
            }
        """.trimIndent()
        every { mockAppConfigProvider.getString("affiliate_config", any()) } returns jsonStr
        every { mockAppConfigProvider.getString("affiliate_config") } returns jsonStr

        val watchUrl = repository.buildProviderWatchUrl(
            providerId = AffiliateRepositoryImpl.PROVIDER_APPLE_TV,
            mediaTitle = "Severance",
            region = "US"
        )

        assertThat(watchUrl).contains("tv.apple.com/search?term=Severance")
        assertThat(watchUrl).contains("at=apple_token_123")
        assertThat(watchUrl).contains("ct=spring_campaign")
    }

    @Test
    fun buildProviderWatchUrl_forNetflix_returnsSearchUrl() {
        val watchUrl = repository.buildProviderWatchUrl(
            providerId = AffiliateRepositoryImpl.PROVIDER_NETFLIX,
            mediaTitle = "Stranger Things",
            region = "US"
        )

        assertThat(watchUrl).isEqualTo("https://www.netflix.com/search?q=Stranger+Things")
    }

    @Test
    fun buildProviderWatchUrl_forDisneyPlus_returnsSearchUrl() {
        val watchUrl = repository.buildProviderWatchUrl(
            providerId = AffiliateRepositoryImpl.PROVIDER_DISNEY_PLUS,
            mediaTitle = "Loki",
            region = "US"
        )

        assertThat(watchUrl).isEqualTo("https://www.disneyplus.com/search?q=Loki")
    }

    @Test
    fun getProviderPackageName_returnsAccuratePackages() {
        assertThat(repository.getProviderPackageName(AffiliateRepositoryImpl.PROVIDER_NETFLIX))
            .isEqualTo("com.netflix.mediaclient")
        assertThat(repository.getProviderPackageName(AffiliateRepositoryImpl.PROVIDER_AMAZON_PRIME_ALT))
            .isEqualTo("com.amazon.avod.thirdpartyclient")
        assertThat(repository.getProviderPackageName(AffiliateRepositoryImpl.PROVIDER_APPLE_TV))
            .isEqualTo("com.apple.atve.androidtv.appletv")
        assertThat(repository.getProviderPackageName(AffiliateRepositoryImpl.PROVIDER_DISNEY_PLUS))
            .isEqualTo("com.disney.disneyplus")
        assertThat(repository.getProviderPackageName(AffiliateRepositoryImpl.PROVIDER_MAX))
            .isEqualTo("com.wbd.stream")
        assertThat(repository.getProviderPackageName(AffiliateRepositoryImpl.PROVIDER_HULU))
            .isEqualTo("com.hulu.plus")
        assertThat(repository.getProviderPackageName(99999)).isNull()
    }

    @Test
    fun buildJustWatchUrl_withPartnerConfig_appendsPartnerAndCampaign() {
        val jsonStr = """
            {
                "justWatch": {
                    "enabled": true,
                    "partnerToken": "my_jw_token",
                    "campaignId": "launch_promo"
                }
            }
        """.trimIndent()
        every { mockAppConfigProvider.getString("affiliate_config", any()) } returns jsonStr
        every { mockAppConfigProvider.getString("affiliate_config") } returns jsonStr

        val url = repository.buildJustWatchUrl("https://www.justwatch.com/us/movie/inception")

        assertThat(url).contains("partner=my_jw_token")
        assertThat(url).contains("campaign=launch_promo")
    }
}
