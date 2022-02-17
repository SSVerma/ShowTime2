package com.ssverma.core.networking.config

import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.interceptor.NetworkInterceptor
import okhttp3.Interceptor
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
    interceptors().also { existingInterceptors ->
        if (existingInterceptors.isEmpty()) {
            configAppInterceptors.toSet().takeIf { it.isNotEmpty() }?.let {
                existingInterceptors.addAll(it)
            }
        } else {
            // To prevent java.util.ConcurrentModificationException
            val addableInterceptors = mutableListOf<Interceptor>()

            existingInterceptors.forEachIndexed { index, existingInterceptor ->
                configAppInterceptors.forEach { configInterceptor ->
                    if (configInterceptor::class.java == existingInterceptor::class.java) {
                        existingInterceptors[index] = configInterceptor
                    } else {
                        addableInterceptors.add(configInterceptor)
                    }
                }
            }

            existingInterceptors.addAll(addableInterceptors)
        }
    }
}

private fun OkHttpClient.Builder.applyNetworkInterceptors(
    configNetworkInterceptors: List<NetworkInterceptor>
) {
    networkInterceptors().also { existingInterceptors ->
        if (existingInterceptors.isEmpty()) {
            configNetworkInterceptors.toSet().takeIf { it.isNotEmpty() }?.let {
                existingInterceptors.addAll(it)
            }
        } else {
            // To prevent java.util.ConcurrentModificationException
            val addableInterceptors = mutableListOf<Interceptor>()

            existingInterceptors.forEachIndexed { index, existingInterceptor ->
                configNetworkInterceptors.forEach { configInterceptor ->
                    if (configInterceptor::class.java == existingInterceptor::class.java) {
                        existingInterceptors[index] = configInterceptor
                    } else {
                        addableInterceptors.add(configInterceptor)
                    }
                }
            }

            existingInterceptors.addAll(addableInterceptors)
        }
    }
}
