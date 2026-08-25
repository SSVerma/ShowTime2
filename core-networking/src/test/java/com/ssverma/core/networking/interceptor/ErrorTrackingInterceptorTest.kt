package com.ssverma.core.networking.interceptor

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.tracker.NetworkErrorTracker
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ErrorTrackingInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var tracker: NetworkErrorTracker

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        tracker = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `verify successful request does not trigger error tracker`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))

        val client = OkHttpClient.Builder()
            .addInterceptor(ErrorTrackingInterceptor(tracker))
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/test/path"))
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.isSuccessful).isTrue()
        verify(exactly = 0) { tracker.trackHttpError(any(), any(), any(), any()) }
        verify(exactly = 0) { tracker.trackNetworkException(any(), any(), any()) }
    }

    @Test
    fun `verify http error response triggers trackHttpError`() {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(404).setStatus("HTTP/1.1 404 Not Found")
        )

        val client = OkHttpClient.Builder()
            .addInterceptor(ErrorTrackingInterceptor(tracker))
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/api/v1/resource"))
            .build()

        val response = client.newCall(request).execute()

        assertThat(response.code).isEqualTo(404)
        verify(exactly = 1) {
            tracker.trackHttpError(
                endpoint = "/api/v1/resource",
                httpCode = 404,
                message = any(),
                durationMs = any()
            )
        }
        verify(exactly = 0) { tracker.trackNetworkException(any(), any(), any()) }
    }

    @Test
    fun `verify network exception triggers trackNetworkException`() {
        mockWebServer.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val client = OkHttpClient.Builder()
            .addInterceptor(ErrorTrackingInterceptor(tracker))
            .build()

        val request = Request.Builder()
            .url(mockWebServer.url("/api/v1/failure"))
            .build()

        try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            // Expected
        }

        verify(exactly = 1) {
            tracker.trackNetworkException(
                endpoint = "/api/v1/failure",
                throwable = any(),
                durationMs = any()
            )
        }
        verify(exactly = 0) { tracker.trackHttpError(any(), any(), any(), any()) }
    }
}
