package com.ssverma.shared.data.repository

import androidx.core.net.toUri
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.shared.domain.model.AffiliateConfig
import com.ssverma.shared.domain.repository.AffiliateRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AffiliateRepositoryImpl @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) : AffiliateRepository {

    companion object {
        private const val KEY_AFFILIATE_CONFIG = "affiliate_config"

        // Known Watch Provider IDs from TMDB
        private const val PROVIDER_APPLE_TV = 2
        private const val PROVIDER_APPLE_TV_PLUS = 350
        private const val PROVIDER_AMAZON_PRIME = 9
        private const val PROVIDER_AMAZON_VIDEO = 10
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun getAffiliateConfig(): AffiliateConfig {
        val configStr = appConfigProvider.getString(KEY_AFFILIATE_CONFIG)
        if (configStr.isBlank()) return AffiliateConfig()

        return try {
            json.decodeFromString<AffiliateConfig>(configStr)
        } catch (e: Exception) {
            AffiliateConfig()
        }
    }

    override fun buildAffiliateUrl(rawUrl: String, providerId: Int, region: String): String {
        val config = getAffiliateConfig()

        return try {
            val uri = rawUrl.toUri()

            when (providerId) {
                PROVIDER_APPLE_TV, PROVIDER_APPLE_TV_PLUS -> {
                    if (config.appleTv.enabled && config.appleTv.partnerToken.isNotBlank()) {
                        uri.buildUpon()
                            .appendQueryParameter("at", config.appleTv.partnerToken)
                            .apply {
                                if (config.appleTv.campaignId.isNotBlank()) {
                                    appendQueryParameter("ct", config.appleTv.campaignId)
                                }
                            }
                            .build().toString()
                    } else rawUrl
                }

                PROVIDER_AMAZON_PRIME, PROVIDER_AMAZON_VIDEO -> {
                    if (config.amazonPrime.enabled) {
                        val tag = config.amazonPrime.regionalTags[region.uppercase()]
                            ?: config.amazonPrime.tagDefault

                        if (tag.isNotBlank()) {
                            uri.buildUpon()
                                .appendQueryParameter("tag", tag)
                                .build().toString()
                        } else rawUrl
                    } else rawUrl
                }

                else -> rawUrl
            }
        } catch (e: Exception) {
            rawUrl
        }
    }
}
