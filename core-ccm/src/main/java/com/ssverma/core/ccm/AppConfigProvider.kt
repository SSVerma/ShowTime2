package com.ssverma.core.ccm

import kotlinx.coroutines.flow.Flow

interface AppConfigProvider {
    /**
     * One-shot reads for when you just need the current value.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    fun getString(key: String, defaultValue: String = ""): String

    fun getLong(key: String, defaultValue: Long = 0L): Long

    fun getDouble(key: String, defaultValue: Double = 0.0): Double

    /**
     * Reactive streams for values that might change at runtime (Kill-switches, dynamic UI).
     */
    fun observeBoolean(key: String, defaultValue: Boolean = false): Flow<Boolean>

    fun observeString(key: String, defaultValue: String = ""): Flow<String>

    fun observeLong(key: String, defaultValue: Long = 0L): Flow<Long>

    fun observeDouble(key: String, defaultValue: Double = 0.0): Flow<Double>

    /**
     * Triggers a network fetch and activates the new config.
     * Usually called once during app startup or foregrounding.
     */
    fun fetchAndActivate()
}
