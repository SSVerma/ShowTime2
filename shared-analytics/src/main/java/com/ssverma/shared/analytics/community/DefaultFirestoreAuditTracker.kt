package com.ssverma.shared.analytics.community

import com.ssverma.core.analytics.Analytics
import com.ssverma.core.analytics.CrashReporter
import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.shared.domain.model.community.CommunityOptimizationConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultFirestoreAuditTracker @Inject constructor(
    private val analytics: Analytics,
    private val crashReporter: CrashReporter,
    private val appConfigProvider: AppConfigProvider
) : FirestoreAuditTracker {

    override fun trackQuery(
        queryTag: String,
        screenName: String?,
        docsCount: Int,
        isFromCache: Boolean,
        durationMs: Long
    ) {
        runCatching {
            val isEnabled = appConfigProvider.getBoolean(
                CommunityOptimizationConfig.REMOTE_KEY_FIRESTORE_AUDIT_ENABLED,
                CommunityOptimizationConfig.DEFAULT_FIRESTORE_AUDIT_ENABLED
            )
            if (!isEnabled) return@runCatching

            analytics.logEvent(
                FirestoreAuditAnalyticsEvent(
                    queryTag = queryTag,
                    screenName = screenName,
                    docsCount = docsCount,
                    isFromCache = isFromCache,
                    durationMs = durationMs
                )
            )
        }
    }

    override fun trackFirestoreError(
        queryTag: String,
        throwable: Throwable,
        durationMs: Long
    ) {
        runCatching {
            val errorMsg = throwable.message.orEmpty()
            val errorType = throwable::class.java.simpleName

            crashReporter.logBreadcrumb(
                "Firestore error on $queryTag (${durationMs}ms): $errorType - $errorMsg"
            )

            val attributes = mapOf(
                "query_tag" to queryTag,
                "duration_ms" to durationMs.toString(),
                "error_type" to errorType
            )

            when {
                errorMsg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                        errorMsg.contains("quota", ignoreCase = true) -> {
                    crashReporter.recordException(
                        throwable = FirestoreQuotaExhaustedException(
                            message = "Firestore Quota Exceeded on $queryTag (${durationMs}ms): $errorMsg",
                            cause = throwable
                        ),
                        attributes = attributes
                    )
                }

                errorMsg.contains("PERMISSION_DENIED", ignoreCase = true) -> {
                    crashReporter.recordException(
                        throwable = FirestorePermissionDeniedException(
                            message = "Firestore Permission Denied on $queryTag (${durationMs}ms): $errorMsg",
                            cause = throwable
                        ),
                        attributes = attributes
                    )
                }

                errorMsg.contains("UNAVAILABLE", ignoreCase = true) ||
                        errorMsg.contains("DEADLINE_EXCEEDED", ignoreCase = true) -> {
                    crashReporter.recordException(
                        throwable = FirestoreOutageException(
                            message = "Firestore Outage on $queryTag (${durationMs}ms): $errorMsg",
                            cause = throwable
                        ),
                        attributes = attributes
                    )
                }
            }
        }
    }
}
