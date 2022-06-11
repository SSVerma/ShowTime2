package com.ssverma.core.networking.adapter

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type

class FakeCallAdapterFactoryOne : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *> {
        return object : CallAdapter<Any, Any> {
            override fun responseType(): Type {
                return returnType
            }

            override fun adapt(call: Call<Any>): Any {
                return "anything"
            }
        }
    }
}

class FakeCallAdapterFactoryTwo : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *> {
        return object : CallAdapter<Any, Any> {
            override fun responseType(): Type {
                return returnType
            }

            override fun adapt(call: Call<Any>): Any {
                return "anything2"
            }
        }
    }
}