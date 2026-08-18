package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.AffiliateConfig

interface AffiliateRepository {
    fun getAffiliateConfig(): AffiliateConfig
    fun buildAffiliateUrl(rawUrl: String, providerId: Int, region: String = "US"): String
}
