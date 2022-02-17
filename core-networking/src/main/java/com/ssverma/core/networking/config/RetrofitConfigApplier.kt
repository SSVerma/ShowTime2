package com.ssverma.core.networking.config

import com.ssverma.core.networking.convertor.AnnotatedConvertorFactory
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit

internal fun Retrofit.Builder.applyConfig(config: RetrofitConfig) = apply {
    // Order matters
    this.applyAnnotatedFactories(config.annotatedConvertorFactories)
    this.applyConvertorFactories(config.convertorFactories)
    this.applyCallAdapterFactories(config.callAdapterFactories)
}

private fun Retrofit.Builder.applyAnnotatedFactories(
    configFactories: Map<Class<out Annotation>, Converter.Factory>
) {
    converterFactories().also { existingFactories ->
        if (existingFactories.isEmpty()) {
            configFactories.takeIf { it.isNotEmpty() }
                ?.let { annotatedFactories ->
                    val builder = AnnotatedConvertorFactory.builder()
                    annotatedFactories.forEach {
                        builder.add(it.key, it.value)
                    }
                    existingFactories.add(builder.build())
                }
        } else {
            existingFactories.forEach { existingFactory ->
                if (existingFactory is AnnotatedConvertorFactory) {
                    configFactories.takeIf { it.isNotEmpty() }
                        ?.let { annotatedFactories ->
                            val builder = existingFactory.newBuilder()
                            annotatedFactories.forEach {
                                builder.add(it.key, it.value)
                            }
                            existingFactories.add(builder.build())
                        }
                }
            }
        }
    }
}

private fun Retrofit.Builder.applyConvertorFactories(
    configFactories: List<Converter.Factory>
) {
    configFactories.takeIf { it.isNotEmpty() }?.let { converterFactories().addAll(it) }
}

private fun Retrofit.Builder.applyCallAdapterFactories(
    configFactories: List<CallAdapter.Factory>
) {
    callAdapterFactories().also { existingFactories ->
        if (existingFactories.isEmpty()) {
            configFactories.takeIf { it.isNotEmpty() }?.let {
                existingFactories.addAll(it)
            }
        } else {
            existingFactories.forEach { existingFactory ->
                configFactories.reversed().forEach { configFactory ->
                    if (existingFactory.javaClass == configFactory.javaClass) {
                        existingFactories.remove(existingFactory)
                    }
                    existingFactories.add(0, configFactory)
                }
            }
        }
    }
}