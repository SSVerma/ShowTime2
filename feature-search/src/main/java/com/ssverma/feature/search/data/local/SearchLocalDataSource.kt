package com.ssverma.feature.search.data.local

import com.ssverma.feature.search.data.local.db.LocalSearchHistory
import kotlinx.coroutines.flow.Flow

interface SearchLocalDataSource {
    fun loadLatestHistoryEntries(limit: Int): Flow<List<LocalSearchHistory>>

    suspend fun insertHistory(historyEntity: LocalSearchHistory)

    suspend fun deleteHistory(historyEntryId: Int)
}