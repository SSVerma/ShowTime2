package com.ssverma.core.storage.keyvalue

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

typealias KeyValueStorage = DataStore<Preferences>

interface KeyValueStorageClient {
    fun createKeyValueStorage(
        context: Context,
        config: KeyValueStorageConfig
    ): KeyValueStorage
}

suspend fun <T> KeyValueStorage.write(key: Preferences.Key<T>, value: T) {
    edit { storage ->
        storage[key] = value
    }
}

suspend fun <T> KeyValueStorage.read(key: Preferences.Key<T>): T? {
    return data.map { storage ->
        storage[key]
    }.first()
}

suspend fun <T> KeyValueStorage.read(key: Preferences.Key<T>, default: T): T {
    return data.map { storage ->
        storage[key]
    }.first() ?: default
}

fun <T> KeyValueStorage.observe(key: Preferences.Key<T>): Flow<T?> {
    return data.map { storage ->
        storage[key]
    }
}

fun <T> KeyValueStorage.observe(key: Preferences.Key<T>, default: T): Flow<T> {
    return data.map { storage ->
        storage[key] ?: default
    }
}