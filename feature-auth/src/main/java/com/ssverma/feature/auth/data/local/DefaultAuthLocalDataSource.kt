package com.ssverma.feature.auth.data.local

import androidx.datastore.preferences.core.stringPreferencesKey
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.read
import com.ssverma.core.storage.keyvalue.write
import javax.inject.Inject
import javax.inject.Named

class DefaultAuthLocalDataSource @Inject constructor(
    @Named("auth")
    private val keyValueStorage: KeyValueStorage
) : AuthLocalDataSource {
    override suspend fun persistAccessToken(accessToken: String) {
        keyValueStorage.write(key = AuthKeyValueStorageKeys.AccessTokenKey, value = accessToken)
    }

    override suspend fun loadAccessToken(): String {
        return keyValueStorage.read(key = AuthKeyValueStorageKeys.AccessTokenKey).orEmpty()
    }

    override suspend fun clearAccessToken() {
        keyValueStorage.write(key = AuthKeyValueStorageKeys.AccessTokenKey, value = "")
    }

    override suspend fun persistRequestToken(requestToken: String) {
        keyValueStorage.write(
            key = AuthKeyValueStorageKeys.RequestTokenKey,
            value = requestToken
        )
    }

    override suspend fun loadRequestToken(): String {
        return keyValueStorage.read(key = AuthKeyValueStorageKeys.RequestTokenKey).orEmpty()
    }

    override suspend fun clearRequestToken() {
        keyValueStorage.write(
            key = AuthKeyValueStorageKeys.RequestTokenKey,
            value = ""
        )
    }

    override suspend fun persistSessionId(sessionId: String) {
        keyValueStorage.write(
            key = AuthKeyValueStorageKeys.SessionIdKey,
            value = sessionId
        )
    }

    override suspend fun loadSessionId(): String {
        return keyValueStorage.read(key = AuthKeyValueStorageKeys.SessionIdKey).orEmpty()
    }

    override suspend fun clearSessionId() {
        keyValueStorage.write(
            key = AuthKeyValueStorageKeys.SessionIdKey,
            value = ""
        )
    }
}

private object AuthKeyValueStorageKeys {
    val AccessTokenKey = stringPreferencesKey("access_token")
    val RequestTokenKey = stringPreferencesKey("request_token")
    val SessionIdKey = stringPreferencesKey("session_id")
}