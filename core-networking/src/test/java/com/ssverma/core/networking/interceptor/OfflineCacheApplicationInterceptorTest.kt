package com.ssverma.core.networking.interceptor

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.cache.HttpCacheConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class OfflineCacheApplicationInterceptorTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `when network is available original request proceeds normally`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"status":"online"}""")
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(
                OfflineCacheApplicationInterceptor(
                    isNetworkAvailable = { true },
                    cacheConfig = HttpCacheConfig(maxStaleSeconds = 86400)
                )
            )
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/data"))
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.isSuccessful).isTrue()
        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.headers["Cache-Control"]).isNull()
    }

    @Test
    fun `when network is offline request adds only-if-cached and max-stale headers`() {
        val interceptor = OfflineCacheApplicationInterceptor(
            isNetworkAvailable = { false },
            cacheConfig = HttpCacheConfig(maxStaleSeconds = 86400)
        )

        val originalRequest = Request.Builder()
            .url("https://api.example.com/data")
            .get()
            .build()

        val requestSlot = slot<Request>()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            Response.Builder()
                .code(200)
                .protocol(Protocol.HTTP_1_1)
                .message("OK")
                .request(requestSlot.captured)
                .body("{}".toResponseBody())
                .build()
        }

        interceptor.intercept(chain)

        assertThat(requestSlot.isCaptured).isTrue()
        val cacheControl = requestSlot.captured.header("Cache-Control")
        assertThat(cacheControl).isNotNull()
        assertThat(cacheControl).contains("only-if-cached")
        assertThat(cacheControl).contains("max-stale=86400")
    }
}


