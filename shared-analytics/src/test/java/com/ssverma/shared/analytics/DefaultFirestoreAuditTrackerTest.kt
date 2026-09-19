package com.ssverma.shared.analytics

import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.CrashReporter
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.shared.analytics.community.DefaultFirestoreAuditTracker
import com.ssverma.shared.analytics.community.FirestoreAnalyticsConstants
import com.ssverma.shared.analytics.community.FirestoreOutageException
import com.ssverma.shared.analytics.community.FirestorePermissionDeniedException
import com.ssverma.shared.analytics.community.FirestoreQuotaExhaustedException
import com.ssverma.shared.domain.model.community.CommunityOptimizationConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DefaultFirestoreAuditTrackerTest {

    private val mockAnalytics: Analytics = mockk(relaxed = true)
    private val mockCrashReporter: CrashReporter = mockk(relaxed = true)
    private val mockAppConfigProvider: AppConfigProvider = mockk(relaxed = true)
    private lateinit var tracker: DefaultFirestoreAuditTracker

    @Before
    fun setUp() {
        every {
            mockAppConfigProvider.getBoolean(
                CommunityOptimizationConfig.REMOTE_KEY_FIRESTORE_AUDIT_ENABLED,
                CommunityOptimizationConfig.DEFAULT_FIRESTORE_AUDIT_ENABLED
            )
        } returns true

        tracker = DefaultFirestoreAuditTracker(
            analytics = mockAnalytics,
            crashReporter = mockCrashReporter,
            appConfigProvider = mockAppConfigProvider
        )
    }

    @Test
    fun `trackQuery when enabled logs firestore_query_audit event with correct parameters`() {
        tracker.trackQuery(
            queryTag = "discussions_preview",
            screenName = "movie_details",
            docsCount = 2,
            isFromCache = false,
            durationMs = 120L
        )

        verify(exactly = 1) {
            mockAnalytics.logEvent(
                match {
                    it.eventName == FirestoreAnalyticsConstants.EVENT_FIRESTORE_QUERY_AUDIT
                }
            )
        }
    }

    @Test
    fun `trackQuery when disabled in config does not log analytics event`() {
        every {
            mockAppConfigProvider.getBoolean(
                CommunityOptimizationConfig.REMOTE_KEY_FIRESTORE_AUDIT_ENABLED,
                CommunityOptimizationConfig.DEFAULT_FIRESTORE_AUDIT_ENABLED
            )
        } returns false

        tracker.trackQuery(
            queryTag = "reactions_batch",
            screenName = "tv_details",
            docsCount = 5,
            isFromCache = true,
            durationMs = 10L
        )

        verify(exactly = 0) {
            mockAnalytics.logEvent(any())
        }
    }

    @Test
    fun `trackFirestoreError with RESOURCE_EXHAUSTED records FirestoreQuotaExhaustedException`() {
        val quotaError = RuntimeException("RESOURCE_EXHAUSTED: Quota exceeded for read requests")

        tracker.trackFirestoreError(
            queryTag = "discussions_thread",
            throwable = quotaError,
            durationMs = 250L
        )

        verify(exactly = 1) {
            mockCrashReporter.logBreadcrumb(match { it.contains("Firestore error on discussions_thread (250ms)") })
        }
        verify(exactly = 1) {
            mockCrashReporter.recordException(
                throwable = match { it is FirestoreQuotaExhaustedException && it.message?.contains("discussions_thread") == true },
                attributes = match {
                    it["query_tag"] == "discussions_thread" &&
                            it["duration_ms"] == "250"
                }
            )
        }
    }

    @Test
    fun `trackFirestoreError with PERMISSION_DENIED records FirestorePermissionDeniedException`() {
        val permError = RuntimeException("PERMISSION_DENIED: Missing or insufficient permissions")

        tracker.trackFirestoreError(
            queryTag = "post_comment",
            throwable = permError,
            durationMs = 180L
        )

        verify(exactly = 1) {
            mockCrashReporter.recordException(
                throwable = match { it is FirestorePermissionDeniedException },
                attributes = match { it["query_tag"] == "post_comment" }
            )
        }
    }

    @Test
    fun `trackFirestoreError with UNAVAILABLE records FirestoreOutageException`() {
        val outageError = RuntimeException("UNAVAILABLE: The service is currently unavailable")

        tracker.trackFirestoreError(
            queryTag = "reactions_batch",
            throwable = outageError,
            durationMs = 3000L
        )

        verify(exactly = 1) {
            mockCrashReporter.recordException(
                throwable = match { it is FirestoreOutageException },
                attributes = match { it["query_tag"] == "reactions_batch" }
            )
        }
    }

    @Test
    fun `trackFirestoreError with generic error logs breadcrumb without recording critical exception`() {
        val genericError = RuntimeException("Unknown parse error")

        tracker.trackFirestoreError(
            queryTag = "curated_lists",
            throwable = genericError,
            durationMs = 50L
        )

        verify(exactly = 1) {
            mockCrashReporter.logBreadcrumb(match { it.contains("Firestore error on curated_lists (50ms)") })
        }
        verify(exactly = 0) {
            mockCrashReporter.recordException(any(), any())
        }
    }
}
