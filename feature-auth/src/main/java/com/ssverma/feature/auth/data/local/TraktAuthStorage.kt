package com.ssverma.feature.auth.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.feature.auth.domain.model.TraktTokenResponse
import com.ssverma.feature.auth.domain.model.TraktUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktAuthStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    keyValueStorageClient: KeyValueStorageClient
) {
    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "trakt_auth_prefs")
    )

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("trakt_access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("trakt_refresh_token")
        val KEY_EXPIRES_AT = longPreferencesKey("trakt_expires_at")
        val KEY_USERNAME = stringPreferencesKey("trakt_username")
        val KEY_DISPLAY_NAME = stringPreferencesKey("trakt_display_name")
        val KEY_AVATAR_URL = stringPreferencesKey("trakt_avatar_url")
        val KEY_IS_VIP = booleanPreferencesKey("trakt_is_vip")
    }

    val accessTokenFlow: Flow<String?> = storage.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]
    }

    val traktUserFlow: Flow<TraktUser?> = storage.data.map { prefs ->
        val username = prefs[KEY_USERNAME]
        if (!username.isNullOrBlank()) {
            TraktUser(
                username = username,
                displayName = prefs[KEY_DISPLAY_NAME] ?: username,
                isVip = prefs[KEY_IS_VIP] ?: false,
                avatarUrl = prefs[KEY_AVATAR_URL]
            )
        } else {
            null
        }
    }

    suspend fun getAccessToken(): String? {
        return accessTokenFlow.firstOrNull()
    }

    suspend fun saveTokens(tokenResponse: TraktTokenResponse) {
        val expiresAt = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000)
        storage.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = tokenResponse.accessToken
            prefs[KEY_REFRESH_TOKEN] = tokenResponse.refreshToken
            prefs[KEY_EXPIRES_AT] = expiresAt
        }
    }

    suspend fun saveUserProfile(user: TraktUser) {
        storage.edit { prefs ->
            prefs[KEY_USERNAME] = user.username
            prefs[KEY_DISPLAY_NAME] = user.displayName
            prefs[KEY_IS_VIP] = user.isVip
            user.avatarUrl?.let { prefs[KEY_AVATAR_URL] = it }
        }
    }

    suspend fun clear() {
        storage.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs.remove(KEY_EXPIRES_AT)
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_DISPLAY_NAME)
            prefs.remove(KEY_AVATAR_URL)
            prefs.remove(KEY_IS_VIP)
        }
    }
}
