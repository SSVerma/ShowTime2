package com.ssverma.shared.domain.model

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    val avatarImageUrl: String,
    val creditId: String
)

data class Crew(
    val id: Int,
    val name: String,
    val avatarImageUrl: String,
    val creditId: String,
    val department: String,
    val job: String
)