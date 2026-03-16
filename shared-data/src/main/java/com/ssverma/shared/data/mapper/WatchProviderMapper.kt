package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.RemoteProviderInfo
import com.ssverma.api.service.tmdb.response.RemoteWatchProvider
import com.ssverma.api.service.tmdb.response.RemoteWatchProviderRegion
import com.ssverma.api.service.tmdb.response.RemoteWatchProviderResponse
import com.ssverma.api.service.tmdb.response.WatchProviderPayload
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.WatchProvider
import com.ssverma.shared.domain.model.WatchProviderRegion

fun RemoteProviderInfo.asProviderInfo(): ProviderInfo {
    return ProviderInfo(
        logoPath = logoPath.convertToFullTmdbImageUrl(),
        providerId = providerId,
        providerName = providerName,
        displayPriority = displayPriority
    )
}

fun List<RemoteProviderInfo>.asProviderInfos(): List<ProviderInfo> {
    return map { it.asProviderInfo() }.sortedBy { it.displayPriority }
}

fun RemoteWatchProvider.asWatchProvider(): WatchProvider {
    return WatchProvider(
        link = link.orEmpty(),
        flatrate = flatRate?.asProviderInfos().orEmpty(),
        rent = rent?.asProviderInfos().orEmpty(),
        buy = buy?.asProviderInfos().orEmpty(),
        free = free?.asProviderInfos().orEmpty(),
        ads = ads?.asProviderInfos().orEmpty(),
    )
}

fun RemoteWatchProviderResponse.asWatchProvidersMap(): Map<String, WatchProvider> {
    val map = mutableMapOf<String, WatchProvider>()
    this.results?.forEach { (regionCode, remoteProvider) ->
        map[regionCode] = remoteProvider.asWatchProvider()
    }
    return map
}

fun RemoteWatchProviderRegion.asWatchProviderRegion(): WatchProviderRegion {
    return WatchProviderRegion(
        iso31661 = iso31661,
        englishName = englishName,
        nativeName = nativeName
    )
}

fun List<RemoteWatchProviderRegion>.asWatchProviderRegions(): List<WatchProviderRegion> {
    return map { it.asWatchProviderRegion() }
}

fun WatchProviderPayload.asProviderInfos(): List<ProviderInfo> {
    return results.asProviderInfos()
}
