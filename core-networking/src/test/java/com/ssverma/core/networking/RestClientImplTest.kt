package com.ssverma.core.networking

import com.google.common.truth.Truth.assertThat
import com.google.gson.annotations.SerializedName
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.networking.adapter.ApiResponseCallAdaptorFactory
import com.ssverma.core.networking.config.AdditionalServiceConfig
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.File

@ExperimentalCoroutinesApi
class RestClientImplTest {

    private lateinit var mockWebServer: MockWebServer

    private lateinit var retrofitBuilder: Retrofit.Builder

    private lateinit var okHttpClient: OkHttpClient

    private val fakeServiceEnvironment = object : ServiceEnvironment<FakeApiService> {
        override val baseUrl: String
            get() = "https://localhost:8080/"

        override val serviceClass: Class<FakeApiService>
            get() = FakeApiService::class.java
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        okHttpClient = OkHttpClient.Builder().build()

        retrofitBuilder = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
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
        mockWebServer.enqueueResponse("fake-api-response-200.json")

        val environment = object : ServiceEnvironment<FakeApiService> {
            override val baseUrl: String
                get() = mockWebServer.url("/").toString()

            override val serviceClass: Class<FakeApiService>
                get() = FakeApiService::class.java
        }

        val restClient = RestClientImpl(
            retrofitBuilder = retrofitBuilder,
            okHttpClient = okHttpClient
        )

        val service = restClient.createService(
            environment = environment
        )

        assertThat(service).isInstanceOf(FakeApiService::class.java)

        val apiResponse = service.getFakeUser()
        val responsePayload = apiResponse as ApiResponse.Success<FakeUser>
        val requestUrl = responsePayload.payload.row.request.url.toString()

        assertThat(requestUrl).isEqualTo(environment.baseUrl + "fakeUser")
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
}

private class FakeAppInterceptor : ApplicationInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}

private data class FakeUser(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

private interface FakeApiService {
    @GET("/fakeUser")
    suspend fun getFakeUser(): ApiResponse<FakeUser, Any>
}