package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class RemoteProviderInfo(
    @SerializedName("logo_path")
    val logoPath: String?,

    @SerializedName("provider_id")
    val providerId: Int,

    @SerializedName("provider_name")
    val providerName: String,

    @SerializedName("display_priority")
    val displayPriority: Int
)

class RemoteWatchProvider(
    @SerializedName("link")
    val link: String?,

    @SerializedName("flatrate")
    val flatRate: List<RemoteProviderInfo>?,

    @SerializedName("rent")
    val rent: List<RemoteProviderInfo>?,

    @SerializedName("buy")
    val buy: List<RemoteProviderInfo>?,

    @SerializedName("free")
    val free: List<RemoteProviderInfo>?,

    @SerializedName("ads")
    val ads: List<RemoteProviderInfo>?,
)

class RemoteWatchProviderResponse(
    @SerializedName("results")
    val results: Map<String, RemoteWatchProvider>?
)
