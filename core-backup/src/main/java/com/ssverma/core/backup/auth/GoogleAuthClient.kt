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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context,
    keyValueStorageClient: KeyValueStorageClient
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val credentialManager = CredentialManager.create(context)
    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "google_auth_prefs")
    )

    private val _currentUser = MutableStateFlow<GoogleUser?>(null)
    val currentUser: StateFlow<GoogleUser?> = _currentUser.asStateFlow()

    init {
        scope.launch {
            loadStoredUser()
        }
    }

    private suspend fun loadStoredUser() {
        val user = storage.data.map { prefs ->
            val email = prefs[KEY_EMAIL].orEmpty()
            val displayName = prefs[KEY_DISPLAY_NAME].orEmpty()
            val photoUrl = prefs[KEY_PHOTO_URL]
            val idToken = prefs[KEY_ID_TOKEN].orEmpty()

            if (email.isNotBlank() && idToken.isNotBlank()) {
                GoogleUser(
                    email = email,
                    displayName = displayName.ifBlank { email.substringBefore("@") },
                    photoUrl = photoUrl,
                    idToken = idToken
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
            } else {
                prefs.remove(KEY_EMAIL)
                prefs.remove(KEY_DISPLAY_NAME)
                prefs.remove(KEY_PHOTO_URL)
                prefs.remove(KEY_ID_TOKEN)
            }
        }
        _currentUser.value = user
    }

    suspend fun signIn(activity: Activity): Result<GoogleUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId("624515598179-showtime-mock.apps.googleusercontent.com")
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
                val user = GoogleUser(
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id.substringBefore("@"),
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    idToken = googleIdTokenCredential.idToken
                )
                saveUser(user)
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type returned"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            // Ignore
        } finally {
            saveUser(null)
        }
    }

    companion object {
        private val KEY_EMAIL = stringPreferencesKey("google_user_email")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("google_user_display_name")
        private val KEY_PHOTO_URL = stringPreferencesKey("google_user_photo_url")
        private val KEY_ID_TOKEN = stringPreferencesKey("google_user_id_token")
    }
}
