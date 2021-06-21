package com.ssverma.showtime.data.remote.response

import com.google.gson.annotations.SerializedName

class RemoteReview(
    @SerializedName("id")
    val id: String?,

    @SerializedName("author_details")
    val author: RemoteReviewAuthor?,

    @SerializedName("content")
    val content: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)

class RemoteReviewAuthor(
    @SerializedName("name")
    val name: String?,

    @SerializedName("username")
    val userName: String?,

    @SerializedName("avatar_path")
    val avatarPath: String?,

    @SerializedName("rating")
    val rating: Float
)