package com.ssverma.core.backup.contributor

import com.google.gson.JsonElement
import com.google.gson.JsonObject

interface BackupContributor {
    val featureKey: String

    suspend fun exportData(): JsonElement?

    suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject)

    suspend fun getEntityCount(): Int

    suspend fun getDetailedCounts(): Map<String, Int> = mapOf(featureKey to getEntityCount())
}
