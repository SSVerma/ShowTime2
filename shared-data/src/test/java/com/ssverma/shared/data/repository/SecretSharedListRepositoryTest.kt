package com.ssverma.shared.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SecretSharedListRepositoryTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val mockPrefs: SharedPreferences = mockk(relaxed = true)
    private val mockFirestore: FirebaseFirestore = mockk(relaxed = true)
    private val mockCollection: CollectionReference = mockk(relaxed = true)
    private val mockDocument: DocumentReference = mockk(relaxed = true)
    private val mockSnapshot: DocumentSnapshot = mockk(relaxed = true)

    private lateinit var repository: SecretSharedListRepositoryImpl

    @Before
    fun setUp() {
        val appInfo = ApplicationInfo().apply { flags = 0 }
        every { mockContext.applicationInfo } returns appInfo
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.getString("persistent_user_uuid", any()) } returns "test-user-id"

        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument

        repository = SecretSharedListRepositoryImpl(
            context = mockContext,
            firestore = mockFirestore
        )
    }

    @Test
    fun `createSecretShare succeeds and returns valid SecretSharedList`() = runTest {
        every { mockDocument.set(any()) } returns Tasks.forResult(null)

        val items = listOf(
            SecretSharedListItem(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Inception",
                posterImageUrl = "/path.jpg",
                voteAvg = 8.8f
            )
        )

        val result = repository.createSecretShare(
            title = "Mind Bending",
            description = "Best movies",
            items = items,
            isCollaborative = true,
            ownerName = "Alice"
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val list = (result as Result.Success).data
        assertThat(list.title).isEqualTo("Mind Bending")
        assertThat(list.description).isEqualTo("Best movies")
        assertThat(list.ownerName).isEqualTo("Alice")
        assertThat(list.ownerUserId).isEqualTo("test-user-id")
        assertThat(list.isCollaborative).isTrue()
        assertThat(list.isRevoked).isFalse()
        assertThat(list.shareCode).startsWith("SL-")
        assertThat(list.items).hasSize(1)
        assertThat(list.items.first().title).isEqualTo("Inception")
    }

    @Test
    fun `getSecretSharedList returns Success when document exists`() = runTest {
        every { mockDocument.get() } returns Tasks.forResult(mockSnapshot)
        every { mockSnapshot.exists() } returns true
        every { mockSnapshot.id } returns "SL-1234"
        every { mockSnapshot.getString("shareCode") } returns "SL-1234"
        every { mockSnapshot.getString("title") } returns "Cozy Night"
        every { mockSnapshot.getString("description") } returns "Favorites"
        every { mockSnapshot.getString("ownerUserId") } returns "user-456"
        every { mockSnapshot.getString("ownerName") } returns "Bob"
        every { mockSnapshot.getBoolean("isCollaborative") } returns false
        every { mockSnapshot.getBoolean("isRevoked") } returns false
        every { mockSnapshot.getString("itemsJson") } returns """[{"mediaId":202,"mediaType":"tv","title":"Dark","posterImageUrl":"/dark.jpg","voteAvg":9.0}]"""
        every { mockSnapshot.getLong("createdAtEpochMs") } returns 1000L
        every { mockSnapshot.getLong("updatedAtEpochMs") } returns 2000L

        val result = repository.getSecretSharedList("SL-1234")

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val list = (result as Result.Success).data
        assertThat(list.title).isEqualTo("Cozy Night")
        assertThat(list.shareCode).isEqualTo("SL-1234")
        assertThat(list.items).hasSize(1)
        assertThat(list.items.first().mediaId).isEqualTo(202)
        assertThat(list.items.first().mediaType).isEqualTo(MediaType.Tv)
    }

    @Test
    fun `revokeSecretShare updates isRevoked to true`() = runTest {
        every { mockDocument.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        val result = repository.revokeSecretShare("SL-1234")

        assertThat(result).isInstanceOf(Result.Success::class.java)
    }

    @Test
    fun `getSecretSharedList returns NetworkFailure when exception occurs`() = runTest {
        every { mockDocument.get() } returns Tasks.forException(RuntimeException("Network timeout"))

        val result = repository.getSecretSharedList("SL-9999")

        assertThat(result).isInstanceOf(Result.Error::class.java)
        val failure = (result as Result.Error).error
        assertThat(failure).isEqualTo(Failure.CoreFailure.NetworkFailure)
    }

    @Test
    fun `normalizeShareCode normalizes various formats correctly`() {
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("4821")).isEqualTo("SL-4821")
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("sl-4821")).isEqualTo("SL-4821")
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("sl 4821")).isEqualTo("SL-4821")
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("sl4821")).isEqualTo("SL-4821")
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("SL-4821")).isEqualTo("SL-4821")
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("https://showtime.ssverma.in/l/4821")).isEqualTo(
            "SL-4821"
        )
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("https://showtime.ssverma.in/l/SL-4821?ref=share")).isEqualTo(
            "SL-4821"
        )
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("https://showtime.ssverma.in/list/SL-4821")).isEqualTo(
            "SL-4821"
        )
        assertThat(SecretSharedListRepositoryImpl.normalizeShareCode("   ")).isEqualTo("")
    }

    @Test
    fun `getSecretSharedList normalizes raw code before querying document`() = runTest {
        every { mockCollection.document("SL-4821") } returns mockDocument
        every { mockDocument.get() } returns Tasks.forResult(mockSnapshot)
        every { mockSnapshot.exists() } returns true
        every { mockSnapshot.id } returns "SL-4821"
        every { mockSnapshot.getString("title") } returns "Normalized Test"

        val result = repository.getSecretSharedList("4821")

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify { mockCollection.document("SL-4821") }
    }
}
