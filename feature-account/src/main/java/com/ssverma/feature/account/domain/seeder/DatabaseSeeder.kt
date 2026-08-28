package com.ssverma.feature.account.domain.seeder

interface DatabaseSeeder {
    suspend fun seedFavorites()
    suspend fun seedWatchlist()
    suspend fun seedHistory()
    suspend fun clearDatabase()
    suspend fun resetCinemaGame()

    object NoOp : DatabaseSeeder {
        override suspend fun seedFavorites() {}
        override suspend fun seedWatchlist() {}
        override suspend fun seedHistory() {}
        override suspend fun clearDatabase() {}
        override suspend fun resetCinemaGame() {}
    }
}
