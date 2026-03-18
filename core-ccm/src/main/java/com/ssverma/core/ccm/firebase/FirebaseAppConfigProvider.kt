package com.ssverma.core.ccm.firebase

import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.ssverma.core.ccm.AppConfigProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAppConfigProvider @Inject constructor() : AppConfigProvider {

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            // Set fetch interval: 0 for Debug (instant), 12 hours for Prod
            val fetchInterval = if (BuildConfig.DEBUG) 0L else 43200L

            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(fetchInterval)
                .build()

            setConfigSettingsAsync(configSettings)
        }
    }

    override fun fetchAndActivate() {
        remoteConfig.fetchAndActivate()
    }

    // --- One-Shot Reads ---

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return try {
            remoteConfig.getBoolean(key)
        } catch (e: Exception) {
            defaultValue
        }
    }

    override fun getString(key: String, defaultValue: String): String {
        return try {
            remoteConfig.getString(key).takeIf { it.isNotEmpty() } ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return try {
            remoteConfig.getLong(key)
        } catch (e: Exception) {
            defaultValue
        }
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return try {
            remoteConfig.getDouble(key)
        } catch (e: Exception) {
            defaultValue
        }
    }

    // --- Reactive Streams ---

    override fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean> =
        observeKey(key) { getBoolean(key, defaultValue) }

    override fun observeString(key: String, defaultValue: String): Flow<String> =
        observeKey(key) { getString(key, defaultValue) }

    override fun observeLong(key: String, defaultValue: Long): Flow<Long> =
        observeKey(key) { getLong(key, defaultValue) }

    override fun observeDouble(key: String, defaultValue: Double): Flow<Double> =
        observeKey(key) { getDouble(key, defaultValue) }

    /**
     * Core reactive engine. Emits initial value, then listens for Firebase realtime updates.
     */
    private inline fun <T> observeKey(
        key: String,
        crossinline getter: () -> T
    ): Flow<T> = callbackFlow {
        // 1. Emit the current value immediately
        trySend(getter())

        // 2. Register Firebase Realtime Listener
        val listener = remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                // Only react if THIS specific key was updated
                if (configUpdate.updatedKeys.contains(key)) {
                    remoteConfig.activate().addOnCompleteListener {
                        trySend(getter())
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                // Silently ignore. The flow remains alive with the last known good value.
            }
        })

        // 3. Clean up the listener if the flow collection is cancelled
        awaitClose { listener.remove() }
    }.distinctUntilChanged() // Prevent emitting the same value twice
}
