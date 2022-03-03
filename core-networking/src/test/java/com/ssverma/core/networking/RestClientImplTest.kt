package com.ssverma.core.networking

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.networking.adapter.ApiResponseCallAdaptorFactory
import com.ssverma.core.networking.config.AdditionalServiceConfig
import com.ssverma.core.networking.convertor.FakeMoshiConvertor
import com.ssverma.core.networking.convertor.FakeMoshiUser
import com.ssverma.core.networking.convertor.FakeUser
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.service.FakeUserApiService
import com.ssverma.core.networking.service.ServiceEnvironment
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

@ExperimentalCoroutinesApi
class RestClientImplTest {

    private var mockWebServer: MockWebServer = MockWebServer()

    private lateinit var retrofitBuilder: Retrofit.Builder

    private lateinit var okHttpClient: OkHttpClient

    private val fakeServiceEnvironment = object : ServiceEnvironment<FakeUserApiService> {
        override val baseUrl: String
            get() = mockWebServer.url("/").toString()

        override val serviceClass: Class<FakeUserApiService>
            get() = FakeUserApiService::class.java
    }

    @Before
    fun setUp() {
        okHttpClient = OkHttpClient.Builder().build()

        retrofitBuilder = Retrofit.Builder()
            .addCallAdapterFactory(ApiResponseCallAdaptorFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `verify rest client creates correct service`() = runTest {
        mockWebServer.enqueueResponse("fake-user-api-response-200.json")

        val restClient = RestClientImpl(
            retrofitBuilder = retrofitBuilder,
            okHttpClient = okHttpClient
        )

        val service = restClient.createService(
            environment = fakeServiceEnvironment
        )

        assertThat(service).isInstanceOf(FakeUserApiService::class.java)

        val apiResponse = service.getFakeUser()
        val responsePayload = apiResponse as ApiResponse.Success<FakeUser>
        val requestUrl = responsePayload.payload.row.request.url.toString()

        assertThat(requestUrl).isEqualTo(fakeServiceEnvironment.baseUrl + "fakeUser")
    }

    @Test
    fun `verify every service request creates new instance of service`() {
        val restClient = RestClientImpl(
            retrofitBuilder = retrofitBuilder,
            okHttpClient = okHttpClient
        )

        val serviceOne = restClient.createService(
            environment = fakeServiceEnvironment
        )

        val serviceTwo = restClient.createService(
            environment = fakeServiceEnvironment
        )

        assertThat(serviceOne).isNotSameInstanceAs(serviceTwo)
    }

    @Test
    fun `verify client applies interceptors from additional config of service`() {
        val fakeAppInterceptor = FakeAppInterceptor()

        val serviceConfig = object : AdditionalServiceConfig() {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = listOf(fakeAppInterceptor)
        }

        val mockedHttpClient = mockk<OkHttpClient>(relaxed = true)

        val result = mutableListOf<Interceptor>()

        every { mockedHttpClient.newBuilder().interceptors() } returns result

        val restClient = RestClientImpl(retrofitBuilder, mockedHttpClient)

        val service = restClient.createService(
            environment = fakeServiceEnvironment,
            serviceConfig = serviceConfig
        )

        assertThat(service).isNotNull()
        assertThat(result).isNotEmpty()
        assertThat(result).contains(fakeAppInterceptor)
    }

    @Test
    fun `verify client applies cache from additional config of service`() {
        val testCache = Cache(File.createTempFile("pre", "suffix"), 1)

        val serviceConfig = object : AdditionalServiceConfig() {
            override val cache: Cache
                get() = testCache
        }

        val mockedHttpClient = mockk<OkHttpClient>(relaxed = true)

        val restClient = RestClientImpl(retrofitBuilder, mockedHttpClient)

        val service = restClient.createService(
            environment = fakeServiceEnvironment,
            serviceConfig = serviceConfig
        )

        assertThat(service).isNotNull()
        verify(exactly = 1) { mockedHttpClient.newBuilder().cache(testCache) }
    }

    @Test
    fun `verify additional json convertor factory applies correctly`() = runTest {
        mockWebServer.enqueueResponse("fake-user-api-response-200.json")

        val serviceConfig = object : AdditionalServiceConfig() {
            override val annotatedConvertorFactories: Map<Class<out Annotation>, Converter.Factory>
                get() = mapOf(
                    FakeMoshiConvertor::class.java to MoshiConverterFactory.create()
                )
        }

        val restClient = RestClientImpl(retrofitBuilder, okHttpClient)

        val service = restClient.createService(
            environment = fakeServiceEnvironment,
            serviceConfig = serviceConfig
        )

        assertThat(service).isNotNull()
        assertThat(service).isInstanceOf(FakeUserApiService::class.java)

        val apiResponse = service.getFakeMoshiUser()
        val responsePayload = apiResponse as ApiResponse.Success<FakeMoshiUser>

        val fakeMoshiUser = responsePayload.body

        assertThat(fakeMoshiUser.id).isEqualTo(1232)
        assertThat(fakeMoshiUser.fullName).isEqualTo("SS Verma")
    }
}

private class FakeAppInterceptor : ApplicationInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}