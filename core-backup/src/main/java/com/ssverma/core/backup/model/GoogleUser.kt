package com.ssverma.core.backup.model

data class GoogleUser(
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val idToken: String,
    val uid: String = ""
)

class GoogleSignInCancelledException(
    message: String = "Google Sign-In was cancelled by the user",
    cause: Throwable? = null
) : Exception(message, cause)

fun Throwable?.isGoogleSignInCancelled(): Boolean {
    if (this == null) return false
    return this is GoogleSignInCancelledException ||
            this is java.util.concurrent.CancellationException ||
            this.javaClass.simpleName.contains("Cancel", ignoreCase = true) ||
            (this.message?.contains("cancel", ignoreCase = true) == true)
}
