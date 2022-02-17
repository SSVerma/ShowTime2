package com.ssverma.core.networking.config

import retrofit2.CallAdapter
import retrofit2.Converter

internal interface RetrofitConfig {
    val annotatedConvertorFactories: Map<Class<out Annotation>, Converter.Factory>

    val convertorFactories: List<Converter.Factory>

    val callAdapterFactories: List<CallAdapter.Factory>
}