package com.ssverma.core.networking.config

import retrofit2.CallAdapter
import retrofit2.Converter

/**
 * Allows to specify configs related to Retrofit.
 *
 * [annotatedConvertorFactories] provides a way for clients to provide their own
 * converter factories using annotations. Using so order of the factories does not matters and
 * multiple clients can provide factories without knowing the order of the factories.
 *
 * [convertorFactories] allows to provide default factories like Gson / Moshi and limited to
 * module only. Client can't use it as of now. @see [AdditionalServiceConfig]
 *
 * [callAdapterFactories] allows to provide call adapters factories.
 *
 */
internal interface RetrofitConfig {
    val annotatedConvertorFactories: Map<Class<out Annotation>, Converter.Factory>

    val convertorFactories: List<Converter.Factory>

    val callAdapterFactories: List<CallAdapter.Factory>
}