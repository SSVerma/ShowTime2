package com.ssverma.shared.ads.injection

import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import java.util.UUID

sealed interface AdInjectable<out T>

data class InjectableContent<out T>(val item: T) : AdInjectable<T>

data class InjectableAd(
    val style: NativeAdStyle,
    val ad: NativeAd? = null,
    val id: String = UUID.randomUUID().toString()
) : AdInjectable<Nothing>
