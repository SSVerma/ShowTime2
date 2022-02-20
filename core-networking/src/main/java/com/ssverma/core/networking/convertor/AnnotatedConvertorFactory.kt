package com.ssverma.core.networking.convertor

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class AnnotatedConvertorFactory private constructor(
    internal val factories: Map<Class<out Annotation>, Converter.Factory>
) : Converter.Factory() {

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {
        annotations.forEach { annotation ->
            val factory = factories[annotation::class.java]
            if (factory != null) {
                return factory.responseBodyConverter(type, annotations, retrofit)
            }
        }
        return null
    }

    fun newBuilder(): Builder {
        return Builder(this)
    }

    class Builder constructor() {
        private val factories = LinkedHashMap<Class<out Annotation>, Converter.Factory>()

        fun add(clazz: Class<out Annotation>, factory: Converter.Factory) = apply {
            factories[clazz] = factory
        }

        fun build(): AnnotatedConvertorFactory {
            return AnnotatedConvertorFactory(factories)
        }

        constructor(annotatedConvertorFactory: AnnotatedConvertorFactory) : this() {
            this.factories.putAll(annotatedConvertorFactory.factories)
        }
    }
}