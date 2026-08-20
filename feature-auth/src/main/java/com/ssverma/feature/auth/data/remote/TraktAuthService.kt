package com.ssverma.feature.auth.data.remote

import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.feature.auth.domain.model.TraktDeviceCodeRequest
import com.ssverma.feature.auth.domain.model.TraktDeviceCodeResponse
import com.ssverma.feature.auth.domain.model.TraktDeviceTokenRequest
import com.ssverma.feature.auth.domain.model.TraktTokenResponse
import com.ssverma.feature.auth.domain.model.TraktUserPayload
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit Service for Trakt.tv Authentication & OAuth endpoints.
 */
interface TraktAuthService {

    @Headers(
        "Content-Type: application/json",
        "trakt-api-version: 2"
    )
    @POST("oauth/device/code")
    suspend fun generateDeviceCode(
        @Body request: TraktDeviceCodeRequest
    ): ApiResponse<TraktDeviceCodeResponse, Any>

    @Headers(
        "Content-Type: application/json",
        "trakt-api-version: 2"
    )
    @POST("oauth/device/token")
    suspend fun pollDeviceToken(
        @Body request: TraktDeviceTokenRequest
    ): ApiResponse<TraktTokenResponse, Any>

    @Headers(
        "Content-Type: application/json",
        "trakt-api-version: 2"
    )
    @GET("users/me?extended=full")
    suspend fun getCurrentUserProfile(
        @Header("Authorization") bearerToken: String,
        @Header("trakt-api-key") clientId: String
    ): ApiResponse<TraktUserPayload, Any>
}
