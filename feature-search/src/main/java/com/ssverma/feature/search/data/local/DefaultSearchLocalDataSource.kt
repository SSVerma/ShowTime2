package com.ssverma.feature.search.data.local

import com.ssverma.feature.search.data.local.db.LocalSearchHistory
import com.ssverma.feature.search.data.local.db.SearchHistoryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultSearchLocalDataSource @Inject constructor(
    private val historyDao: SearchHistoryDao
) : SearchLocalDataSource {
    override fun loadLatestHistoryEntries(limit: Int): Flow<List<LocalSearchHistory>> {
        return historyDao.loadLatest(limit = limit)
    }

    override suspend fun insertHistory(historyEntity: LocalSearchHistory) {
        historyDao.insert(historyEntity)
    }

    override suspend fun deleteHistory(historyEntryId: Int) {
        historyDao.deleteById(id = historyEntryId)
    }
}