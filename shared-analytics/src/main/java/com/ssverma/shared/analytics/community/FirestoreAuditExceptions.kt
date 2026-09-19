package com.ssverma.shared.analytics.community

/**
 * Technical exception logged to Crashlytics when Firestore quota (reads/writes) is exhausted.
 * Triggers instant Crashlytics Velocity alerts for admins.
 */
class FirestoreQuotaExhaustedException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Technical exception logged to Crashlytics when a Firestore security rule or permission check fails.
 */
class FirestorePermissionDeniedException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Technical exception logged to Crashlytics when Firestore service is temporarily unavailable / down.
 */
class FirestoreOutageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
