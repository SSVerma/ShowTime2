package com.ssverma.core.networking.config

import com.ssverma.core.networking.convertor.AnnotatedConvertorFactory
import com.ssverma.core.networking.utils.mergeOrderedWith
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
            existingFactories.forEachIndexed { index, existingFactory ->
                if (existingFactory is AnnotatedConvertorFactory) {
                    configFactories.takeIf { it.isNotEmpty() }
                        ?.let { annotatedFactories ->
                            val builder = existingFactory.newBuilder()
                            annotatedFactories.forEach {
                                builder.add(it.key, it.value)
                            }
                            existingFactories.set(index, builder.build())
                        }
                }
            }
        }
    }
}

private fun Retrofit.Builder.applyConvertorFactories(
    configFactories: List<Converter.Factory>
) {
    val updated = converterFactories().mergeOrderedWith(configFactories) { oldValue, newValue ->
        val generic = !newValue::class.java.typeParameters.isNullOrEmpty()
        !generic && oldValue::class.java == newValue::class.java
    }

    converterFactories().apply {
        clear()
        addAll(updated)
    }
}

private fun Retrofit.Builder.applyCallAdapterFactories(
    configFactories: List<CallAdapter.Factory>
) {
    val updated = callAdapterFactories().mergeOrderedWith(configFactories) { oldValue, newValue ->
        val generic = !newValue::class.java.typeParameters.isNullOrEmpty()
        !generic && oldValue::class.java == newValue::class.java
    }

    callAdapterFactories().apply {
        clear()
        addAll(updated)
    }
}