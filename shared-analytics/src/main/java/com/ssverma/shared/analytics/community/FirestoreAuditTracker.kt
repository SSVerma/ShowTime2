package com.ssverma.shared.analytics.community

/**
 * Technical telemetry contract for auditing Firestore query volumes, read quota consumption,
 * cache hit ratios, and remote outages.
 */
interface FirestoreAuditTracker {
    /**
     * Records a completed Firestore query with execution metrics.
     *
     * @param queryTag A human-readable identifier for the query (e.g., "discussions_preview", "reactions_batch")
     * @param screenName Contextual screen where the query originated (optional)
     * @param docsCount Number of documents returned in the snapshot
     * @param isFromCache True if the result was served from local cache (0 billed reads), false if from network
     * @param durationMs Execution latency in milliseconds
     */
    fun trackQuery(
        queryTag: String,
        screenName: String? = null,
        docsCount: Int,
        isFromCache: Boolean,
        durationMs: Long
    )

    /**
     * Records a Firestore failure, classifying quota, security rule, or network outage errors.
     *
     * @param queryTag Identifier for the failed operation
     * @param throwable The root cause exception
     * @param durationMs Duration until failure occurred
     */
    fun trackFirestoreError(
        queryTag: String,
        throwable: Throwable,
        durationMs: Long
    )
}
