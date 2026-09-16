package com.ssverma.shared.analytics

import com.ssverma.core.analytics.CrashReporter
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DefaultNetworkErrorTrackerTest {

    private val mockCrashReporter: CrashReporter = mockk(relaxed = true)
    private lateinit var tracker: DefaultNetworkErrorTracker

    @Before
    fun setUp() {
        tracker = DefaultNetworkErrorTracker(crashReporter = mockCrashReporter)
    }

    @Test
    fun `trackHttpError with 4xx logs breadcrumb and does not record exception`() {
        tracker.trackHttpError(
            endpoint = "/movie/123",
            httpCode = 404,
            message = "Not Found",
            durationMs = 120L
        )

        verify(exactly = 1) {
            mockCrashReporter.logBreadcrumb(match { it.contains("HTTP 404 on /movie/123 (120ms)") })
        }
        verify(exactly = 0) {
            mockCrashReporter.recordException(any(), any())
        }
    }

    @Test
    fun `trackHttpError with 5xx logs breadcrumb and records non-fatal exception with attributes`() {
        tracker.trackHttpError(
            endpoint = "/trending/movie/day",
            httpCode = 503,
            message = "Service Unavailable",
            durationMs = 450L
        )

        verify(exactly = 1) {
            mockCrashReporter.logBreadcrumb(match { it.contains("HTTP 503 on /trending/movie/day (450ms)") })
        }
        verify(exactly = 1) {
            mockCrashReporter.recordException(
                throwable = match { it is RemoteServerException && it.message?.contains("503") == true },
                attributes = match {
                    it["endpoint"] == "/trending/movie/day" &&
                            it["http_code"] == "503" &&
                            it["duration_ms"] == "450"
                }
            )
        }
    }

    @Test
    fun `trackNetworkException logs breadcrumb and does not record non-fatal exception`() {
        tracker.trackNetworkException(
            endpoint = "/search/movie",
            throwable = IOException("Unable to resolve host"),
            durationMs = 80L
        )

        verify(exactly = 1) {
            mockCrashReporter.logBreadcrumb(match { it.contains("Network failure on /search/movie (80ms)") })
        }
        verify(exactly = 0) {
            mockCrashReporter.recordException(any(), any())
        }
    }
}
