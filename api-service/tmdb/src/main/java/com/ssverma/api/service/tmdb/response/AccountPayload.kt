package com.ssverma.api.service.tmdb.response

import com.google.gson.annotations.SerializedName

class AccountPayload(
    @SerializedName("id")
    val id: Int,

    @SerializedName("username")
    val userName: String?,

    @SerializedName("name")
    val displayName: String?,

    @SerializedName("iso_639_1")
    val languageIso: String?,

    @SerializedName("iso_3166_1")
    val regionIso: String?,

    @SerializedName("avatar")
    val avatar: RemoteTmdbAvatar?
)

class RemoteAvatar(
    @SerializedName("tmdb")
    val tmdbAvatar: RemoteTmdbAvatar?
)

class RemoteTmdbAvatar(
    @SerializedName("avatar_path")
    val avatarPath: String?
)