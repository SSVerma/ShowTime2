package com.ssverma.shared.domain.model.library

data class JoinedSecretList(
    val shareCode: String,
    val title: String,
    val description: String? = null,
    val ownerName: String,
    val coverImageUrl: String? = null,
    val itemCount: Int = 0,
    val isCollaborative: Boolean = false,
    val lastOpenedEpochMs: Long = System.currentTimeMillis()
)
