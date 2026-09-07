package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.AffiliateConfig

interface AffiliateRepository {
    fun getAffiliateConfig(): AffiliateConfig

    fun buildAffiliateUrl(rawUrl: String, providerId: Int, region: String = "US"): String

    fun buildProviderWatchUrl(
        providerId: Int,
        mediaTitle: String,
        fallbackLink: String? = null,
        region: String = "US"
    ): String

    fun buildProviderHubUrl(providerId: Int, region: String = "US"): String

    fun getProviderPackageName(providerId: Int): String?

    fun buildJustWatchUrl(rawUrl: String? = null): String
}
