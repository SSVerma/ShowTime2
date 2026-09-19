package com.ssverma.core.networking.interceptor

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.cache.HttpCacheConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class ResponseCacheControlNetworkInterceptorTest {

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
    fun `request with custom cache max-age header rewrites response and strips internal header`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Cache-Control", "private, no-cache")
                .setHeader("Pragma", "no-cache")
                .setBody("""{"status":"ok"}""")
        )

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(
                ResponseCacheControlNetworkInterceptor(
                    cacheConfig = HttpCacheConfig.Default
                )
            )
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/trending/movie/day"))
            .header(HttpCacheConfig.HEADER_SHOWTIME_CACHE_MAX_AGE, "1800")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.isSuccessful).isTrue()
        assertThat(response.header("Cache-Control")).isEqualTo("public, max-age=1800")
        assertThat(response.header("Pragma")).isNull()

        val recordedRequest = mockWebServer.takeRequest()
        assertThat(recordedRequest.headers[HttpCacheConfig.HEADER_SHOWTIME_CACHE_MAX_AGE]).isNull()
    }

    @Test
    fun `request without custom header preserves server original cache-control by default`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Cache-Control", "public, max-age=310")
                .setBody("""{"status":"ok"}""")
        )

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(
                ResponseCacheControlNetworkInterceptor(
                    cacheConfig = HttpCacheConfig.Default
                )
            )
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/movie/550"))
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.isSuccessful).isTrue()
        assertThat(response.header("Cache-Control")).isEqualTo("public, max-age=310")
    }

    @Test
    fun `successful GET response is rewritten when global override is enabled`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Cache-Control", "private, no-cache")
                .setHeader("Pragma", "no-cache")
                .setBody("""{"status":"ok"}""")
        )

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(
                ResponseCacheControlNetworkInterceptor(
                    cacheConfig = HttpCacheConfig(
                        maxAgeSeconds = 1800,
                        overrideServerCacheControl = true
                    )
                )
            )
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/trending/movie/day"))
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.isSuccessful).isTrue()
        assertThat(response.header("Cache-Control")).isEqualTo("public, max-age=1800")
        assertThat(response.header("Pragma")).isNull()
    }

    @Test
    fun `POST request is not rewritten with Cache-Control header`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Cache-Control", "no-cache")
                .setBody("""{"status":"created"}""")
        )

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(
                ResponseCacheControlNetworkInterceptor(
                    cacheConfig = HttpCacheConfig(
                        maxAgeSeconds = 1800,
                        overrideServerCacheControl = true
                    )
                )
            )
            .build()

        val body = "{}".toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(mockWebServer.url("/media/favorite"))
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.isSuccessful).isTrue()
        assertThat(response.header("Cache-Control")).isEqualTo("no-cache")
    }

    @Test
    fun `error response is not rewritten with long-lived Cache-Control header`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setHeader("Cache-Control", "no-cache")
                .setBody("""{"error":"not_found"}""")
        )

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(
                ResponseCacheControlNetworkInterceptor(
                    cacheConfig = HttpCacheConfig(
                        maxAgeSeconds = 1800,
                        overrideServerCacheControl = true
                    )
                )
            )
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/movie/999999"))
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.isSuccessful).isFalse()
        assertThat(response.header("Cache-Control")).isEqualTo("no-cache")
    }

    @Test
    fun `explicit client no-cache request header bypasses Cache-Control rewrite`() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Cache-Control", "no-cache")
                .setBody("""{"status":"fresh"}""")
        )

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(
                ResponseCacheControlNetworkInterceptor(
                    cacheConfig = HttpCacheConfig(
                        maxAgeSeconds = 1800,
                        overrideServerCacheControl = true
                    )
                )
            )
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/trending/movie/day"))
            .header("Cache-Control", "no-cache")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.isSuccessful).isTrue()
        assertThat(response.header("Cache-Control")).isEqualTo("no-cache")
    }
}

