package com.ssverma.shared.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.SecretSharedList
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.domain.repository.SecretSharedListRepository
import com.ssverma.shared.domain.utils.ShareMediaUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SecretSharedListRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : SecretSharedListRepository {

    private val gson = Gson()
    private val isDebug: Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    private val colPrefix: String = if (isDebug) "dev_" else ""
    private val colSecretSharedLists: String get() = "${colPrefix}secret_shared_lists"

    private val persistentUserId: String by lazy {
        val storedId = context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
            .getString("persistent_user_uuid", null)
        if (storedId != null) {
            storedId
        } else {
            val newId = UUID.randomUUID().toString()
            context.getSharedPreferences("showtime_device_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("persistent_user_uuid", newId)
                .apply()
            newId
        }
    }

    override suspend fun createSecretShare(
        title: String,
        description: String?,
        items: List<SecretSharedListItem>,
        isCollaborative: Boolean,
        ownerName: String
    ): Result<SecretSharedList, Failure<*>> {
        val safeOwnerName =
            ownerName.takeIf { it.isNotBlank() && !it.equals("Me", ignoreCase = true) } ?: "Friend"
        val shareCode = generateShareCode()
        val docId = normalizeShareCode(shareCode)
        val stampedItems = items.map { item ->
            if (item.addedByUserId.isNullOrBlank()) {
                item.copy(addedByUserId = persistentUserId, addedByName = safeOwnerName)
            } else {
                item
            }
        }
        val dtos = stampedItems.map { it.toDto() }
        val itemsJson = gson.toJson(dtos)
        val now = System.currentTimeMillis()

        val listData = hashMapOf(
            "shareCode" to shareCode,
            "title" to title,
            "description" to description,
            "ownerUserId" to persistentUserId,
            "ownerName" to safeOwnerName,
            "isCollaborative" to isCollaborative,
            "isRevoked" to false,
            "itemCount" to items.size,
            "itemsJson" to itemsJson,
            "createdAtEpochMs" to now,
            "updatedAtEpochMs" to now
        )

        return try {
            firestore.collection(colSecretSharedLists).document(docId).set(listData).await()
            Result.Success(
                SecretSharedList(
                    shareCode = shareCode,
                    title = title,
                    description = description,
                    ownerUserId = persistentUserId,
                    ownerName = ownerName,
                    isCollaborative = isCollaborative,
                    isRevoked = false,
                    items = items,
                    createdAtEpochMs = now,
                    updatedAtEpochMs = now
                )
            )
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    override suspend fun getSecretSharedList(shareCode: String): Result<SecretSharedList, Failure<*>> {
        val docId = normalizeShareCode(shareCode)
        return try {
            val snapshot = firestore.collection(colSecretSharedLists).document(docId).get().await()
            if (!snapshot.exists()) {
                Result.Error(Failure.CoreFailure.UnexpectedFailure)
            } else {
                val list = parseSecretSharedList(snapshot)
                if (list != null) {
                    Result.Success(list)
                } else {
                    Result.Error(Failure.CoreFailure.UnexpectedFailure)
                }
            }
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    override fun observeSecretSharedList(shareCode: String): Flow<SecretSharedList?> =
        callbackFlow {
            val docId = normalizeShareCode(shareCode)
            val docRef = firestore.collection(colSecretSharedLists).document(docId)

            val listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(parseSecretSharedList(snapshot))
                } else {
                    trySend(null)
                }
            }

            awaitClose {
                listenerRegistration.remove()
            }
        }

    override suspend fun addMediaToSharedList(
        shareCode: String,
        item: SecretSharedListItem
    ): Result<Unit, Failure<*>> {
        val docId = normalizeShareCode(shareCode)
        val docRef = firestore.collection(colSecretSharedLists).document(docId)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Secret shared list not found")
                }
                val isRevoked = snapshot.getBoolean("isRevoked") ?: false
                if (isRevoked) {
                    throw IllegalStateException("Secret shared list is revoked")
                }
                val isCollaborative = snapshot.getBoolean("isCollaborative") ?: false
                val ownerUserId = snapshot.getString("ownerUserId")
                if (!isCollaborative && ownerUserId != persistentUserId) {
                    throw IllegalStateException("Only the list owner or co-curators can add items")
                }

                val itemsJson = snapshot.getString("itemsJson").orEmpty()
                val currentItems = parseItemsJson(itemsJson).toMutableList()

                if (currentItems.none { it.mediaId == item.mediaId }) {
                    currentItems.add(item)
                    val updatedJson = gson.toJson(currentItems.map { it.toDto() })
                    val now = System.currentTimeMillis()
                    transaction.update(
                        docRef,
                        mapOf(
                            "itemsJson" to updatedJson,
                            "itemCount" to currentItems.size,
                            "updatedAtEpochMs" to now
                        )
                    )
                }
            }.await()
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    override suspend fun removeMediaFromSharedList(
        shareCode: String,
        mediaId: Int
    ): Result<Unit, Failure<*>> {
        val docId = normalizeShareCode(shareCode)
        val docRef = firestore.collection(colSecretSharedLists).document(docId)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Secret shared list not found")
                }
                val isRevoked = snapshot.getBoolean("isRevoked") ?: false
                if (isRevoked) {
                    throw IllegalStateException("Secret shared list is revoked")
                }

                val ownerUserId = snapshot.getString("ownerUserId").orEmpty()
                val isCollaborative = snapshot.getBoolean("isCollaborative") ?: false
                val isOwner = ownerUserId == persistentUserId

                val itemsJson = snapshot.getString("itemsJson").orEmpty()
                val currentItems = parseItemsJson(itemsJson).toMutableList()

                val itemToRemove = currentItems.firstOrNull { it.mediaId == mediaId }
                if (itemToRemove != null) {
                    val isAddedByCurrentUser = itemToRemove.addedByUserId == persistentUserId
                    if (!isOwner && (!isCollaborative || !isAddedByCurrentUser)) {
                        throw IllegalStateException("Collaborators can only remove items they added")
                    }

                    currentItems.removeAll { it.mediaId == mediaId }
                    val updatedJson = gson.toJson(currentItems.map { it.toDto() })
                    val now = System.currentTimeMillis()
                    transaction.update(
                        docRef,
                        mapOf(
                            "itemsJson" to updatedJson,
                            "itemCount" to currentItems.size,
                            "updatedAtEpochMs" to now
                        )
                    )
                }
            }.await()
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    override suspend fun revokeSecretShare(shareCode: String): Result<Unit, Failure<*>> {
        val docId = normalizeShareCode(shareCode)
        val docRef = firestore.collection(colSecretSharedLists).document(docId)

        return try {
            docRef.update(
                mapOf(
                    "isRevoked" to true,
                    "updatedAtEpochMs" to System.currentTimeMillis()
                )
            ).await()
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    override suspend fun updateCollaborativeStatus(
        shareCode: String,
        isCollaborative: Boolean
    ): Result<Unit, Failure<*>> {
        val docId = normalizeShareCode(shareCode)
        val docRef = firestore.collection(colSecretSharedLists).document(docId)

        return try {
            docRef.update(
                mapOf(
                    "isCollaborative" to isCollaborative,
                    "updatedAtEpochMs" to System.currentTimeMillis()
                )
            ).await()
            Result.Success(Unit)
        } catch (_: Exception) {
            Result.Error(Failure.CoreFailure.NetworkFailure)
        }
    }

    private fun parseSecretSharedList(snapshot: DocumentSnapshot): SecretSharedList? {
        val shareCode = snapshot.getString("shareCode") ?: snapshot.id
        val title = snapshot.getString("title") ?: return null
        val description = snapshot.getString("description")
        val ownerUserId = snapshot.getString("ownerUserId").orEmpty()
        val rawOwnerName = snapshot.getString("ownerName").orEmpty()
        val ownerName = if (rawOwnerName.isBlank() || rawOwnerName.equals(
                "Me",
                ignoreCase = true
            )
        ) "Friend" else rawOwnerName
        val isCollaborative = snapshot.getBoolean("isCollaborative") ?: false
        val isRevoked = snapshot.getBoolean("isRevoked") ?: false
        val itemsJson = snapshot.getString("itemsJson").orEmpty()
        val createdAt = snapshot.getLong("createdAtEpochMs") ?: System.currentTimeMillis()
        val updatedAt = snapshot.getLong("updatedAtEpochMs") ?: createdAt

        val items = parseItemsJson(itemsJson)

        return SecretSharedList(
            shareCode = shareCode,
            title = title,
            description = description,
            ownerUserId = ownerUserId,
            ownerName = ownerName,
            isCollaborative = isCollaborative,
            isRevoked = isRevoked,
            items = items,
            createdAtEpochMs = createdAt,
            updatedAtEpochMs = updatedAt
        )
    }

    private fun parseItemsJson(itemsJson: String): List<SecretSharedListItem> {
        if (itemsJson.isBlank()) return emptyList()
        val type = object : TypeToken<List<SecretSharedListItemDto>>() {}.type
        return try {
            val dtos: List<SecretSharedListItemDto> = gson.fromJson(itemsJson, type) ?: emptyList()
            dtos.map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun generateShareCode(): String {
        val num = Random.nextInt(1000, 10000)
        return "SL-$num"
    }

    companion object {
        fun normalizeShareCode(rawCode: String): String =
            ShareMediaUtils.normalizeSecretShareCode(rawCode)
    }
}

internal data class SecretSharedListItemDto(
    val mediaId: Int = 0,
    val mediaType: String = "",
    val title: String = "",
    val posterImageUrl: String = "",
    val backdropImageUrl: String = "",
    val voteAvg: Float = 0f,
    val releaseYear: String? = null,
    val overview: String? = null,
    val addedByName: String? = null,
    val addedByUserId: String? = null,
    val addedAtEpochMs: Long = 0L
) {
    fun toDomain(): SecretSharedListItem = SecretSharedListItem(
        mediaId = mediaId,
        mediaType = mediaType.asMediaType(),
        title = title,
        posterImageUrl = posterImageUrl,
        backdropImageUrl = backdropImageUrl,
        voteAvg = voteAvg,
        releaseYear = releaseYear,
        overview = overview,
        addedByName = if (addedByName?.equals(
                "Me",
                ignoreCase = true
            ) == true
        ) "Friend" else addedByName,
        addedByUserId = addedByUserId,
        addedAtEpochMs = addedAtEpochMs
    )
}

private fun SecretSharedListItem.toDto(): SecretSharedListItemDto = SecretSharedListItemDto(
    mediaId = mediaId,
    mediaType = mediaType.asString(),
    title = title,
    posterImageUrl = posterImageUrl,
    backdropImageUrl = backdropImageUrl,
    voteAvg = voteAvg,
    releaseYear = releaseYear,
    overview = overview,
    addedByName = addedByName,
    addedByUserId = addedByUserId,
    addedAtEpochMs = addedAtEpochMs
)

private fun MediaType.asString(): String = when (this) {
    MediaType.Movie -> "movie"
    MediaType.Tv -> "tv"
    MediaType.Person -> "person"
    MediaType.Unknown -> "unknown"
}

private fun String.asMediaType(): MediaType = when (this.lowercase()) {
    "movie" -> MediaType.Movie
    "tv" -> MediaType.Tv
    "person" -> MediaType.Person
    else -> MediaType.Unknown
}

