package com.ssverma.shared.ads.injection

import com.ssverma.shared.ads.ui.NativeAdStyle

/**
 * Configuration for ad injection.
 * @property placement The strategy used for placing ads.
 * @property style The visual style of the native ad to be injected.
 */
data class AdInjectionConfig(
    val placement: AdPlacement = AdPlacement.None,
    val style: NativeAdStyle = NativeAdStyle.List
)
