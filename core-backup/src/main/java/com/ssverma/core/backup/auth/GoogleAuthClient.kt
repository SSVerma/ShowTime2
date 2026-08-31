package com.ssverma.core.backup.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.ssverma.core.backup.model.GoogleSignInCancelledException
import com.ssverma.core.backup.model.GoogleUser
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    @param:ApplicationContext private val context: Context,
    keyValueStorageClient: KeyValueStorageClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "google_auth_prefs")
    )

    private val _currentUser = MutableStateFlow<GoogleUser?>(null)
    val currentUser: StateFlow<GoogleUser?> = _currentUser.asStateFlow()

    init {
        scope.launch {
            loadStoredUser()
            ensureAuthenticatedSession()
        }
    }

    suspend fun ensureAuthenticatedSession() {
        try {
            if (firebaseAuth.currentUser == null) {
                firebaseAuth.signInAnonymously().await()
            }
        } catch (_: Exception) {
            // Offline or network unavailable; getEffectiveUserId will use cached fallback
        }
    }

    suspend fun getEffectiveUserId(): String {
        val currentUid = firebaseAuth.currentUser?.uid
        if (!currentUid.isNullOrBlank()) {
            return currentUid
        }

        // Offline fallback
        val cachedLocalUid = storage.data.map { it[KEY_FALLBACK_ANON_UID] }.first()
        if (!cachedLocalUid.isNullOrBlank()) {
            return cachedLocalUid
        }

        val newLocalUid = "anon_${UUID.randomUUID().toString().replace("-", "")}"
        storage.edit { prefs ->
            prefs[KEY_FALLBACK_ANON_UID] = newLocalUid
        }
        return newLocalUid
    }

    private suspend fun loadStoredUser() {
        val user = storage.data.map { prefs ->
            val email = prefs[KEY_EMAIL].orEmpty()
            val displayName = prefs[KEY_DISPLAY_NAME].orEmpty()
            val photoUrl = prefs[KEY_PHOTO_URL]
            val idToken = prefs[KEY_ID_TOKEN].orEmpty()
            val uid = prefs[KEY_UID].orEmpty()

            if (email.isNotBlank() && idToken.isNotBlank()) {
                GoogleUser(
                    email = email,
                    displayName = displayName.ifBlank { email.substringBefore("@") },
                    photoUrl = photoUrl,
                    idToken = idToken,
                    uid = uid.ifBlank { firebaseAuth.currentUser?.uid.orEmpty() }
                )
            } else {
                null
            }
        }.first()
        _currentUser.value = user
    }

    private suspend fun saveUser(user: GoogleUser?) {
        storage.edit { prefs ->
            if (user != null) {
                prefs[KEY_EMAIL] = user.email
                prefs[KEY_DISPLAY_NAME] = user.displayName
                user.photoUrl?.let { prefs[KEY_PHOTO_URL] = it }
                prefs[KEY_ID_TOKEN] = user.idToken
                if (user.uid.isNotBlank()) {
                    prefs[KEY_UID] = user.uid
                }
            } else {
                prefs.remove(KEY_EMAIL)
                prefs.remove(KEY_DISPLAY_NAME)
                prefs.remove(KEY_PHOTO_URL)
                prefs.remove(KEY_ID_TOKEN)
                prefs.remove(KEY_UID)
            }
        }
        _currentUser.value = user
    }

    suspend fun signIn(activity: Activity): Result<GoogleUser> {
        val serverClientId = getServerClientId()
        if (serverClientId.isBlank()) {
            return Result.failure(
                exception = IllegalStateException("Google Web Client ID not configured. Please add your OAuth 2.0 Web Client ID in strings.xml (google_server_client_id) or google-services.json.")
            )
        }

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                context = activity,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Link with current anonymous account or sign in to existing Firebase account
                var firebaseUid = ""
                try {
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val currentFirebaseUser = firebaseAuth.currentUser
                    if (currentFirebaseUser != null && currentFirebaseUser.isAnonymous) {
                        try {
                            val linkResult =
                                currentFirebaseUser.linkWithCredential(authCredential).await()
                            firebaseUid = linkResult.user?.uid.orEmpty()
                        } catch (_: FirebaseAuthUserCollisionException) {
                            val signInResult =
                                firebaseAuth.signInWithCredential(authCredential).await()
                            firebaseUid = signInResult.user?.uid.orEmpty()
                        }
                    } else {
                        val signInResult = firebaseAuth.signInWithCredential(authCredential).await()
                        firebaseUid = signInResult.user?.uid.orEmpty()
                    }
                } catch (_: Exception) {
                    // Non-fatal if Firebase Auth network sync fails; Google credentials are still stored
                }

                val user = GoogleUser(
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName
                        ?: googleIdTokenCredential.id.substringBefore("@"),
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = idToken,
                    uid = firebaseUid.ifBlank { firebaseAuth.currentUser?.uid.orEmpty() }
                )
                saveUser(user)
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type returned"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(GoogleSignInCancelledException(cause = e))
        } catch (e: java.util.concurrent.CancellationException) {
            Result.failure(GoogleSignInCancelledException(cause = e))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e.javaClass.simpleName.contains("Cancel", ignoreCase = true) ||
                e.message?.contains("cancel", ignoreCase = true) == true
            ) {
                Result.failure(GoogleSignInCancelledException(cause = e))
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
            // Ignore
        }
        try {
            firebaseAuth.signOut()
        } catch (_: Exception) {
            // Ignore
        } finally {
            saveUser(null)
            scope.launch {
                ensureAuthenticatedSession()
            }
        }
    }

    private fun getServerClientId(): String {
        val defaultWebClientIdRes =
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (defaultWebClientIdRes != 0) {
            val id = context.getString(defaultWebClientIdRes)
            if (id.isNotBlank()) return id
        }

        val customClientIdRes = context.resources.getIdentifier(
            "google_server_client_id",
            "string",
            context.packageName
        )
        if (customClientIdRes != 0) {
            val id = context.getString(customClientIdRes)
            if (id.isNotBlank()) return id
        }

        return ""
    }

    companion object {
        private val KEY_EMAIL = stringPreferencesKey("google_user_email")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("google_user_display_name")
        private val KEY_PHOTO_URL = stringPreferencesKey("google_user_photo_url")
        private val KEY_ID_TOKEN = stringPreferencesKey("google_user_id_token")
        private val KEY_UID = stringPreferencesKey("google_user_firebase_uid")
        private val KEY_FALLBACK_ANON_UID = stringPreferencesKey("google_user_fallback_anon_uid")
    }
}
