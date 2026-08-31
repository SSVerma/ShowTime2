package com.ssverma.core.backup.model

data class GoogleUser(
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val idToken: String,
    val uid: String = ""
)
