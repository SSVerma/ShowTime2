package com.ssverma.shared.domain.model

data class ProviderInfo(
    val logoPath: String,
    val providerId: Int,
    val providerName: String,
    val displayPriority: Int
)

data class WatchProvider(
    val link: String,
    val flatrate: List<ProviderInfo>,
    val rent: List<ProviderInfo>,
    val buy: List<ProviderInfo>,
    val free: List<ProviderInfo>,
    val ads: List<ProviderInfo>,
) {
    val hasProviders: Boolean
        get() = flatrate.isNotEmpty()
                || rent.isNotEmpty()
                || buy.isNotEmpty()
                || ads.isNotEmpty()
                || free.isNotEmpty()
}
