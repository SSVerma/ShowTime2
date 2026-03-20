package com.ssverma.shared.ads.injection

/**
 * Injects ads into a regular list of items based on the provided configuration.
 */
fun <T> List<T>.injectAds(
    config: AdInjectionConfig
): List<AdInjectable<T>> {
    if (config.placement is AdPlacement.None || isEmpty()) {
        return map { InjectableContent(it) }
    }

    val result = mutableListOf<AdInjectable<T>>()
    val content = this

    when (val placement = config.placement) {
        is AdPlacement.Fixed -> {
            val sortedPositions = placement.positions.sorted()
            var contentIndex = 0
            var posIndex = 0

            // We iterate until all content is added and all fixed positions are handled
            var currentIndex = 0
            while (contentIndex < content.size || posIndex < sortedPositions.size) {
                if (posIndex < sortedPositions.size && currentIndex == sortedPositions[posIndex]) {
                    result.add(InjectableAd(config.style))
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
                    result.add(InjectableAd(config.style))
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
