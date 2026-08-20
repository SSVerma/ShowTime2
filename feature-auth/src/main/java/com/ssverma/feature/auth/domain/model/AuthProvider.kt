package com.ssverma.feature.auth.domain.model

/**
 * Supported Authentication and Cloud Identity Providers in ShowTime.
 */
enum class AuthProvider {
    GOOGLE,
    TRAKT,
    TMDB
}

/**
 * Unified Account Representation for any connected authentication provider.
 */
data class AuthAccount(
    val provider: AuthProvider,
    val id: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val isLinked: Boolean = true
)
