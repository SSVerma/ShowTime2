package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.library.SecretSharedList
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import kotlinx.coroutines.flow.Flow

interface SecretSharedListRepository {
    suspend fun createSecretShare(
        title: String,
        description: String?,
        items: List<SecretSharedListItem>,
        isCollaborative: Boolean,
        ownerName: String
    ): Result<SecretSharedList, Failure<*>>

    suspend fun getSecretSharedList(shareCode: String): Result<SecretSharedList, Failure<*>>

    fun observeSecretSharedList(shareCode: String): Flow<SecretSharedList?>

    suspend fun addMediaToSharedList(
        shareCode: String,
        item: SecretSharedListItem
    ): Result<Unit, Failure<*>>

    suspend fun removeMediaFromSharedList(
        shareCode: String,
        mediaId: Int
    ): Result<Unit, Failure<*>>

    suspend fun revokeSecretShare(shareCode: String): Result<Unit, Failure<*>>
}
