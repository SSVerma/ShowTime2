package com.ssverma.feature.auth.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Trakt Device Code Request Payload.
 * Only requires public client_id, ensuring zero secret leakage in open-source clients.
 */
data class TraktDeviceCodeRequest(
    @SerializedName("client_id")
    val clientId: String
)

/**
 * Response from Trakt POST /oauth/device/code
 */
data class TraktDeviceCodeResponse(
    @SerializedName("device_code")
    val deviceCode: String,
    @SerializedName("user_code")
    val userCode: String,
    @SerializedName("verification_url")
    val verificationUrl: String,
    @SerializedName("expires_in")
    val expiresInSeconds: Int,
    @SerializedName("interval")
    val intervalSeconds: Int
)

/**
 * Device token polling request payload.
 */
data class TraktDeviceTokenRequest(
    @SerializedName("code")
    val deviceCode: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String? = null
)

/**
 * Trakt Token Response with OAuth Access & Refresh tokens.
 */
data class TraktTokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String = "bearer",
    @SerializedName("expires_in")
    val expiresIn: Long,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("scope")
    val scope: String = "public",
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis() / 1000
)

/**
 * Trakt User Profile payload.
 */
data class TraktUserPayload(
    @SerializedName("username")
    val username: String,
    @SerializedName("name")
    val name: String?,
    @SerializedName("vip")
    val isVip: Boolean = false,
    @SerializedName("images")
    val images: TraktUserImages? = null
)

data class TraktUserImages(
    @SerializedName("avatar")
    val avatar: TraktAvatarImage? = null
)

data class TraktAvatarImage(
    @SerializedName("full")
    val fullUrl: String? = null
)

/**
 * Normalized Trakt User domain model.
 */
data class TraktUser(
    val username: String,
    val displayName: String,
    val isVip: Boolean,
    val avatarUrl: String?
)

/**
 * Trakt Connection UI & Session States.
 */
sealed interface TraktAuthState {
    data object Disconnected : TraktAuthState

    data class Authorizing(
        val userCode: String,
        val verificationUrl: String,
        val secondsRemaining: Int
    ) : TraktAuthState

    data class Connected(
        val user: TraktUser,
        val accessToken: String
    ) : TraktAuthState

    data class Error(
        val message: String
    ) : TraktAuthState
}
