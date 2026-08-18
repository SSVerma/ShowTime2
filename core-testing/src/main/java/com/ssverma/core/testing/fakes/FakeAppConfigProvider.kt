package com.ssverma.core.testing.fakes

import com.ssverma.core.ccm.AppConfigProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAppConfigProvider(
    private val booleanConfigs: MutableMap<String, Boolean> = mutableMapOf(),
    private val stringConfigs: MutableMap<String, String> = mutableMapOf(),
    private val longConfigs: MutableMap<String, Long> = mutableMapOf(),
    private val doubleConfigs: MutableMap<String, Double> = mutableMapOf()
) : AppConfigProvider {

    private val booleanFlows = mutableMapOf<String, MutableStateFlow<Boolean>>()
    private val stringFlows = mutableMapOf<String, MutableStateFlow<String>>()
    private val longFlows = mutableMapOf<String, MutableStateFlow<Long>>()
    private val doubleFlows = mutableMapOf<String, MutableStateFlow<Double>>()

    fun setBoolean(key: String, value: Boolean) {
        booleanConfigs[key] = value
        booleanFlows[key]?.value = value
    }

    fun setString(key: String, value: String) {
        stringConfigs[key] = value
        stringFlows[key]?.value = value
    }

    fun setLong(key: String, value: Long) {
        longConfigs[key] = value
        longFlows[key]?.value = value
    }

    fun setDouble(key: String, value: Double) {
        doubleConfigs[key] = value
        doubleFlows[key]?.value = value
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return booleanConfigs[key] ?: defaultValue
    }

    override fun getString(key: String, defaultValue: String): String {
        return stringConfigs[key] ?: defaultValue
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return longConfigs[key] ?: defaultValue
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return doubleConfigs[key] ?: defaultValue
    }

    override fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean> {
        return booleanFlows.getOrPut(key) {
            MutableStateFlow(getBoolean(key, defaultValue))
        }.asStateFlow()
    }

    override fun observeString(key: String, defaultValue: String): Flow<String> {
        return stringFlows.getOrPut(key) {
            MutableStateFlow(getString(key, defaultValue))
        }.asStateFlow()
    }

    override fun observeLong(key: String, defaultValue: Long): Flow<Long> {
        return longFlows.getOrPut(key) {
            MutableStateFlow(getLong(key, defaultValue))
        }.asStateFlow()
    }

    override fun observeDouble(key: String, defaultValue: Double): Flow<Double> {
        return doubleFlows.getOrPut(key) {
            MutableStateFlow(getDouble(key, defaultValue))
        }.asStateFlow()
    }

    override fun fetchAndActivate() {
        // No-op for tests
    }
}
