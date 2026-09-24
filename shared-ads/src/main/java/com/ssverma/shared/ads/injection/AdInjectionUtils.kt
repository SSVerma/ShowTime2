package com.ssverma.shared.ads.injection

import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.mapSuccess

/**
 * Injects ads into a regular list of items based on the provided configuration.
 */
fun <T> List<T>.injectAds(
    config: AdInjectionConfig,
    isAdsEnabled: Boolean = true
): List<AdInjectable<T>> {
    if (!isAdsEnabled || config.placement is AdPlacement.None || isEmpty()) {
        return map { InjectableContent(it) }
    }

    val result = mutableListOf<AdInjectable<T>>()
    val content = this

    fun makeAdId(index: Int): String {
        return if (config.sectionTag.isNotBlank()) {
            "${config.sectionTag}_ad_${config.style.name}_$index"
        } else {
            "ad_${config.style.name}_$index"
        }
    }

    when (val placement = config.placement) {
        is AdPlacement.Fixed -> {
            val sortedPositions = placement.positions.sorted()
            var contentIndex = 0
            var posIndex = 0

            // We iterate until all content is added and all fixed positions are handled
            var currentIndex = 0
            while (contentIndex < content.size || posIndex < sortedPositions.size) {
                if (posIndex < sortedPositions.size && currentIndex == sortedPositions[posIndex]) {
                    result.add(
                        InjectableAd(
                            style = config.style,
                            id = makeAdId(currentIndex)
                        )
                    )
                    posIndex++
                } else if (contentIndex < content.size) {
                    result.add(InjectableContent(content[contentIndex]))
                    contentIndex++
                } else {
                    // No more content, but maybe more fixed positions?
                    // Usually we stop if no more content, but let's fulfill the fixed positions if they are relevant.
                    break
                }
                currentIndex++
            }
        }

        is AdPlacement.Repeating -> {
            var contentIndex = 0
            var currentIndex = 0

            while (contentIndex < content.size) {
                if (currentIndex >= placement.start &&
                    (currentIndex - placement.start) % (placement.frequency + 1) == 0
                ) {
                    result.add(
                        InjectableAd(
                            style = config.style,
                            id = makeAdId(currentIndex)
                        )
                    )
                } else {
                    result.add(InjectableContent(content[contentIndex]))
                    contentIndex++
                }
                currentIndex++
            }
        }

    }

    return result
}

/**
 * Updates a matching [InjectableAd] with the provided [NativeAd] in a list of [AdInjectable] items.
 * If no matching ad is found, returns the exact same list reference to preserve referential equality and Compose stability.
 */
fun <T> List<AdInjectable<T>>.updateNativeAd(
    injectableAd: InjectableAd,
    nativeAd: NativeAd
): List<AdInjectable<T>> {
    return updateNativeAd(adId = injectableAd.id, nativeAd = nativeAd)
}

/**
 * Updates a matching [InjectableAd] by [adId] with the provided [NativeAd] in a list of [AdInjectable] items.
 * If no matching ad is found, returns the exact same list reference to preserve referential equality and Compose stability.
 */
fun <T> List<AdInjectable<T>>.updateNativeAd(
    adId: String,
    nativeAd: NativeAd
): List<AdInjectable<T>> {
    var adFound = false
    val updatedList = this.map { item ->
        if (item is InjectableAd && item.id == adId) {
            adFound = true
            item.copy(ad = nativeAd)
        } else {
            item
        }
    }
    return if (adFound) updatedList else this
}

/**
 * Removes a matching [InjectableAd] from a list of [AdInjectable] items.
 * If no matching ad is found, returns the exact same list reference to preserve referential equality and Compose stability.
 */
fun <T> List<AdInjectable<T>>.removeNativeAd(
    injectableAd: InjectableAd
): List<AdInjectable<T>> {
    return removeNativeAd(adId = injectableAd.id)
}

/**
 * Removes a matching [InjectableAd] by [adId] from a list of [AdInjectable] items.
 * If no matching ad is found, returns the exact same list reference to preserve referential equality and Compose stability.
 */
fun <T> List<AdInjectable<T>>.removeNativeAd(
    adId: String
): List<AdInjectable<T>> {
    var adFound = false
    val updatedList = this.filterNot { item ->
        val matches = item is InjectableAd && item.id == adId
        if (matches) adFound = true
        matches
    }
    return if (adFound) updatedList else this
}

/**
 * Updates a matching [InjectableAd] inside a [UiState] wrapping a list of [AdInjectable] items.
 */
fun <T, E> UiState<List<AdInjectable<T>>, E>.updateNativeAd(
    injectableAd: InjectableAd,
    nativeAd: NativeAd
): UiState<List<AdInjectable<T>>, E> {
    return mapSuccess { it.updateNativeAd(injectableAd = injectableAd, nativeAd = nativeAd) }
}

/**
 * Updates a matching [InjectableAd] by [adId] inside a [UiState] wrapping a list of [AdInjectable] items.
 */
fun <T, E> UiState<List<AdInjectable<T>>, E>.updateNativeAd(
    adId: String,
    nativeAd: NativeAd
): UiState<List<AdInjectable<T>>, E> {
    return mapSuccess { it.updateNativeAd(adId = adId, nativeAd = nativeAd) }
}

/**
 * Removes a matching [InjectableAd] inside a [UiState] wrapping a list of [AdInjectable] items.
 */
fun <T, E> UiState<List<AdInjectable<T>>, E>.removeNativeAd(
    injectableAd: InjectableAd
): UiState<List<AdInjectable<T>>, E> {
    return mapSuccess { it.removeNativeAd(injectableAd = injectableAd) }
}

/**
 * Removes a matching [InjectableAd] by [adId] inside a [UiState] wrapping a list of [AdInjectable] items.
 */
fun <T, E> UiState<List<AdInjectable<T>>, E>.removeNativeAd(
    adId: String
): UiState<List<AdInjectable<T>>, E> {
    return mapSuccess { it.removeNativeAd(adId = adId) }
}
