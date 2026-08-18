package com.ssverma.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AffiliateConfig(
    val appleTv: PartnerConfig = PartnerConfig(),
    val amazonPrime: AmazonPartnerConfig = AmazonPartnerConfig(),
    val tickets: TicketingPartnerConfig = TicketingPartnerConfig()
)

@Serializable
data class PartnerConfig(
    val enabled: Boolean = false,
    val partnerToken: String = "",
    val campaignId: String = ""
)

@Serializable
data class AmazonPartnerConfig(
    val enabled: Boolean = false,
    val tagDefault: String = "",
    val regionalTags: Map<String, String> = emptyMap()
)

@Serializable
data class TicketingPartnerConfig(
    val enabled: Boolean = false,
    val fandangoPartnerId: String = "",
    val bookMyShowPartnerId: String = ""
)
