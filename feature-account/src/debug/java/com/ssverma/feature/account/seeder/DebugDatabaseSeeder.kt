package com.ssverma.feature.account.seeder

import com.ssverma.feature.account.domain.seeder.DatabaseSeeder
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.repository.CinemaGameRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugDatabaseSeeder @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val cinemaGameRepository: CinemaGameRepository
) : DatabaseSeeder {

    override suspend fun seedFavorites() {
        libraryRepository.toggleFavorite(
            mediaId = 550,
            mediaType = MediaType.Movie,
            title = "Fight Club",
            posterImageUrl = "/bptfVGEQuv6vDTIMVCHjJ9Dz8PX.jpg",
            backdropImageUrl = "/hZkgoQYus5vegHoetLkCJzb17zJ.jpg",
            voteAvg = 8.433f,
            releaseDate = "1999-10-15"
        )
    }

    override suspend fun seedWatchlist() {
        libraryRepository.toggleWatchlist(
            mediaId = 27205,
            mediaType = MediaType.Movie,
            title = "Inception",
            posterImageUrl = "/oYuLEt3zVCKq57qu2F8dT7NIa6f.jpg",
            backdropImageUrl = "/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg",
            voteAvg = 8.364f,
            releaseDate = "2010-07-15"
        )
    }

    override suspend fun seedHistory() {
        libraryRepository.toggleWatchHistory(
            mediaId = 155,
            mediaType = MediaType.Movie,
            title = "The Dark Knight",
            posterImageUrl = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            voteAvg = 8.512f
        )
    }

    override suspend fun clearDatabase() {
        libraryRepository.deleteFavorite(550)
        libraryRepository.deleteWatchlist(27205)
    }

    override suspend fun resetCinemaGame() {
        cinemaGameRepository.resetGameData()
    }
}
