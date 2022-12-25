package com.ssverma.core.networking.config

import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.interceptor.NetworkInterceptor
import com.ssverma.core.networking.utils.mergeOrderedWith
import okhttp3.OkHttpClient

internal fun OkHttpClient.Builder.applyConfig(
    config: OkHttpConfig
) = apply {
    this.applyAppInterceptors(config.applicationInterceptors)
    this.applyNetworkInterceptors(config.networkInterceptors)
    this.cache(config.cache)
}

private fun OkHttpClient.Builder.applyAppInterceptors(
    configAppInterceptors: List<ApplicationInterceptor>
) {
    val updated = interceptors().mergeOrderedWith(configAppInterceptors) { oldValue, newValue ->
        val generic = !newValue::class.java.typeParameters.isNullOrEmpty()
        !generic && oldValue::class.java == newValue::class.java
    }

    interceptors().apply {
        clear()
        addAll(updated)
    }
}

private fun OkHttpClient.Builder.applyNetworkInterceptors(
    configInterceptors: List<NetworkInterceptor>
) {
    val updated = networkInterceptors().mergeOrderedWith(configInterceptors) { oldValue, newValue ->
        val generic = !newValue::class.java.typeParameters.isNullOrEmpty()
        !generic && oldValue::class.java == newValue::class.java
    }

    networkInterceptors().apply {
        clear()
        addAll(updated)
    }
}
