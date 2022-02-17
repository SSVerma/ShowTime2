package com.ssverma.core.networking.convertor

import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * Avoids leaking envelope related metadata throughout the service / app.
 * Provides a centric way to handle all the envelop metadata.
 *
 * Usage:
 *      Rest response Json:
 *      {
 *          meta: {
 *              timestamp: 129012
 *          },
 *          user: {
 *              id: 1232
 *              name: "SS Verma"
 *          }
 *      }
 *
 *  without envelope factory:
 *
 *      class UserEnvelope {
 *          val meta: Meta
 *          val user: User
 *      }
 *
 *
 *      interface UserService {
 *          fun getUser(): UserEnvelope
 *      }
 *
 *      service.getUser().user
 *
 *  with envelope factory:
 *
 *      class UserEnvelope: Envelope<User> {
 *          val meta: Meta
 *          override val response: User
 *      }
 *
 *      interface UserService {
 *          fun getUser(): User
 *      }
 *
 *      service.getUser()
 *
 */
class EnvelopeConvertorFactory<T : Envelope<T>> private constructor(
    private val clazz: Class<T>
) : Converter.Factory() {

    companion object {
        fun <T : Envelope<T>> create(clazz: Class<T>): EnvelopeConvertorFactory<T> {
            return EnvelopeConvertorFactory(clazz)
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val envelopeType = TypeToken.getParameterized(clazz, type).type
        val delegate =
            retrofit.nextResponseBodyConverter<T>(this, envelopeType, annotations)

        return Converter<ResponseBody, Any> {
            val envelope: T? = delegate.convert(it)
            envelope?.response
        }
    }
}

interface Envelope<T> {
    val response: T
}