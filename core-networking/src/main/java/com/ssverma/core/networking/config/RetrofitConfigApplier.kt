package com.ssverma.core.networking.config

import com.ssverma.core.networking.convertor.AnnotatedConvertorFactory
import com.ssverma.core.networking.utils.mergeOrderedWith
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit

internal fun Retrofit.Builder.applyConfig(config: RetrofitConfig) = apply {
    // Order matters
    config.annotatedConvertorFactories.takeIf { it.isNotEmpty() }?.let {
        this.applyAnnotatedFactories(it)
    }
    config.convertorFactories.takeIf { it.isNotEmpty() }?.let {
        this.applyConvertorFactories(it)
    }
    config.callAdapterFactories.takeIf { it.isNotEmpty() }?.let {
        this.applyCallAdapterFactories(it)
    }
}

private fun Retrofit.Builder.applyAnnotatedFactories(
    configFactories: Map<Class<out Annotation>, Converter.Factory>
) {
    converterFactories().also { existingFactories ->
        val existingAnnotatedFactories =
            existingFactories.filterIsInstance<AnnotatedConvertorFactory>()

        if (existingAnnotatedFactories.isNullOrEmpty()) {
            val builder = AnnotatedConvertorFactory.builder()
            configFactories.forEach {
                builder.add(it.key, it.value)
            }
            existingFactories.add(0, builder.build())
        } else {
            existingFactories.forEachIndexed { index, existingFactory ->
                if (existingFactory is AnnotatedConvertorFactory) {
                    val builder = existingFactory.newBuilder()
                    configFactories.forEach {
                        builder.add(it.key, it.value)
                    }
                    existingFactories[index] = builder.build()
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