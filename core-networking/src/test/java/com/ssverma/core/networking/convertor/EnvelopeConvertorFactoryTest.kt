package com.ssverma.core.networking.convertor

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.RestClient
import com.ssverma.core.networking.RestClientImpl
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.networking.adapter.ApiResponseCallAdaptorFactory
import com.ssverma.core.networking.config.AdditionalServiceConfig
import com.ssverma.core.networking.enqueueResponse
import com.ssverma.core.networking.service.FakeUserApiService
import com.ssverma.core.networking.service.ServiceEnvironment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@ExperimentalCoroutinesApi
class EnvelopeConvertorFactoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofitBuilder: Retrofit.Builder
    private lateinit var restClient: RestClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        okHttpClient = OkHttpClient.Builder().build()
        retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(ApiResponseCallAdaptorFactory.create())
            .client(okHttpClient)
        restClient = RestClientImpl(retrofitBuilder, okHttpClient)
    }

    @Test
    fun `verify envelop factory applies correctly`() = runTest {
        mockWebServer.enqueueResponse("fake-enveloped-api-response-200.json")

        val environment = object : ServiceEnvironment<FakeUserApiService> {
            override val baseUrl: String
                get() = mockWebServer.url("/").toString()
            override val serviceClass: Class<FakeUserApiService>
                get() = FakeUserApiService::class.java

        }

        val userEnvelopFactory = EnvelopeConvertorFactory.create(FakeUserEnvelop::class.java)

        val serviceConfig = object : AdditionalServiceConfig() {
            override val annotatedConvertorFactories: Map<Class<out Annotation>, Converter.Factory>
                get() = mapOf(FakeUserEnvelopConvertor::class.java to userEnvelopFactory)
        }

        val service = restClient.createService(
            environment = environment,
            serviceConfig = serviceConfig
        )

        assertThat(service).isNotNull()

        val apiResponse = service.getEnvelopedFakeUser()
        assertThat(apiResponse).isInstanceOf(ApiResponse.Success::class.java)

        val succeededResponse = apiResponse as ApiResponse.Success<FakeUser>

        assertThat(succeededResponse.body).isNotNull()
        assertThat(succeededResponse.body).isInstanceOf(FakeUser::class.java)
        assertThat(succeededResponse.body.id).isEqualTo(1232)
        assertThat(succeededResponse.body.name).isEqualTo("SS Verma")
    }

    @Test
    fun `verify multiple envelop factories apply correctly`() = runTest {
        mockWebServer.enqueueResponse("fake-enveloped-api-response-200.json")

        val environment = object : ServiceEnvironment<FakeUserApiService> {
            override val baseUrl: String
                get() = mockWebServer.url("/").toString()
            override val serviceClass: Class<FakeUserApiService>
                get() = FakeUserApiService::class.java

        }

        val userEnvelopFactory = EnvelopeConvertorFactory.create(FakeUserEnvelop::class.java)
        val fooEnvelopFactory = EnvelopeConvertorFactory.create(FooEnvelop::class.java)

        val serviceConfig = object : AdditionalServiceConfig() {
            override val annotatedConvertorFactories: Map<Class<out Annotation>, Converter.Factory>
                get() = mapOf(
                    FakeUserEnvelopConvertor::class.java to userEnvelopFactory,
                    FakeFooEnvelopConvertor::class.java to fooEnvelopFactory
                )
        }

        val service = restClient.createService(
            environment = environment,
            serviceConfig = serviceConfig
        )

        assertThat(service).isNotNull()

        val fooApiResponse = service.getEnvelopedFoo()
        assertThat(fooApiResponse).isInstanceOf(ApiResponse.Success::class.java)

        val succeededFooResponse = fooApiResponse as ApiResponse.Success<Foo>

        assertThat(succeededFooResponse.body).isNotNull()
        assertThat(succeededFooResponse.body).isInstanceOf(Foo::class.java)
        assertThat(succeededFooResponse.body.id).isEqualTo(143)
        assertThat(succeededFooResponse.body.name).isEqualTo("Foo Bar")
    }

}