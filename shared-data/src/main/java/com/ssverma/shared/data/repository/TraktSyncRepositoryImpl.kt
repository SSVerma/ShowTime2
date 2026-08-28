package com.ssverma.shared.data.repository

import com.ssverma.api.service.tmdb.TmdbApiService
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.storage.debug.DebugConfigManager
import com.ssverma.shared.data.local.db.dao.EpisodeWatchHistoryDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.data.remote.TraktIds
import com.ssverma.shared.data.remote.TraktMediaItemIdentifier
import com.ssverma.shared.data.remote.TraktSyncBody
import com.ssverma.shared.data.remote.TraktSyncService
import com.ssverma.shared.domain.model.trakt.TraktSyncResult
import com.ssverma.shared.domain.model.trakt.TraktUpNextEpisode
import com.ssverma.shared.domain.notifier.WidgetSyncNotifier
import com.ssverma.shared.domain.repository.TraktSyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktSyncRepositoryImpl @Inject constructor(
    private val traktSyncService: TraktSyncService,
    private val watchlistDao: WatchlistDao,
    private val watchHistoryDao: WatchHistoryDao,
    private val favoriteDao: FavoriteDao,
    private val episodeWatchHistoryDao: EpisodeWatchHistoryDao,
    private val showWatchProgressDao: ShowWatchProgressDao,
    private val debugConfigManager: DebugConfigManager,
    private val tmdbApiService: TmdbApiService? = null,
    private val widgetSyncNotifier: WidgetSyncNotifier? = null
) : TraktSyncRepository {

    private data class MockShowState(
        val showTmdbId: Int,
        val showTitle: String,
        val showPosterPath: String? = null,
        val totalAired: Int,
        var totalCompleted: Int,
        var seasonNumber: Int,
        var episodeNumber: Int,
        var seasonCompleted: Int = 0,
        var seasonTotalAired: Int = 0,
        val episodeTitles: Map<Pair<Int, Int>, String>
    )

    private val mockShows = mutableMapOf(
        93405 to MockShowState(
            showTmdbId = 93405,
            showTitle = "Severance",
            showPosterPath = "https://image.tmdb.org/t/p/w500/abfJnkhz4c24t1GqSgq2J61z4e2.jpg",
            totalAired = 9,
            totalCompleted = 6,
            seasonNumber = 2,
            episodeNumber = 1,
            seasonCompleted = 6,
            seasonTotalAired = 9,
            episodeTitles = mapOf(
                Pair(2, 1) to "Hello, Innie",
                Pair(2, 2) to "Goodbye, Mrs. Selvig",
                Pair(2, 3) to "Who Is Alive?",
                Pair(2, 4) to "Woe's Hollow",
                Pair(2, 5) to "The Aftermath",
                Pair(2, 6) to "Hide and Seek",
                Pair(2, 7) to "The Grim Barbarity",
                Pair(2, 8) to "Sweet Vitriol",
                Pair(2, 9) to "The We We Are"
            )
        ),
        94997 to MockShowState(
            showTmdbId = 94997,
            showTitle = "House of the Dragon",
            showPosterPath = "https://image.tmdb.org/t/p/w500/7QMsOTMUswlwxJP0rTTZfmz2tX2.jpg",
            totalAired = 8,
            totalCompleted = 4,
            seasonNumber = 2,
            episodeNumber = 5,
            seasonCompleted = 4,
            seasonTotalAired = 8,
            episodeTitles = mapOf(
                Pair(2, 1) to "A Son for a Son",
                Pair(2, 2) to "Rhaenyra the Cruel",
                Pair(2, 3) to "The Burning Mill",
                Pair(2, 4) to "A Dance of Dragons",
                Pair(2, 5) to "Regent",
                Pair(2, 6) to "Smallfolk",
                Pair(2, 7) to "The Red Sowing",
                Pair(2, 8) to "The Queen Who Ever Was"
            )
        ),
        1396 to MockShowState(
            showTmdbId = 1396,
            showTitle = "Breaking Bad",
            showPosterPath = "https://image.tmdb.org/t/p/w500/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
            totalAired = 62,
            totalCompleted = 10,
            seasonNumber = 2,
            episodeNumber = 4,
            seasonCompleted = 3,
            seasonTotalAired = 13,
            episodeTitles = mapOf(
                Pair(2, 1) to "Seven Thirty-Seven",
                Pair(2, 2) to "Grilled",
                Pair(2, 3) to "Bit by a Dead Bee",
                Pair(2, 4) to "Down",
                Pair(2, 5) to "Breakage",
                Pair(2, 6) to "Peekaboo",
                Pair(2, 7) to "Negro y Azul",
                Pair(2, 8) to "Better Call Saul",
                Pair(2, 9) to "4 Days Out",
                Pair(2, 10) to "Over",
                Pair(2, 11) to "Mandala",
                Pair(2, 12) to "Phoenix",
                Pair(2, 13) to "ABQ"
            )
        ),
        1399 to MockShowState(
            showTmdbId = 1399,
            showTitle = "Game of Thrones",
            showPosterPath = "https://image.tmdb.org/t/p/w500/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
            totalAired = 73,
            totalCompleted = 24,
            seasonNumber = 3,
            episodeNumber = 5,
            episodeTitles = mapOf(
                Pair(3, 1) to "Valar Dohaeris",
                Pair(3, 2) to "Dark Wings, Dark Words",
                Pair(3, 3) to "Walk of Punishment",
                Pair(3, 4) to "And Now His Watch Is Ended",
                Pair(3, 5) to "Kissed by Fire",
                Pair(3, 6) to "The Climb",
                Pair(3, 7) to "The Bear and the Maiden Fair",
                Pair(3, 8) to "Second Sons",
                Pair(3, 9) to "The Rains of Castamere",
                Pair(3, 10) to "Mhysa"
            )
        ),
        84958 to MockShowState(
            showTmdbId = 84958,
            showTitle = "Loki",
            showPosterPath = "https://image.tmdb.org/t/p/w500/voHUmluYmKyleFk9a3xgcj1qIgwa.jpg",
            totalAired = 12,
            totalCompleted = 9,
            seasonNumber = 2,
            episodeNumber = 4,
            episodeTitles = mapOf(
                Pair(2, 1) to "Ouroboros",
                Pair(2, 2) to "Breaking Brad",
                Pair(2, 3) to "1893",
                Pair(2, 4) to "Heart of the TVA",
                Pair(2, 5) to "Science/Fiction",
                Pair(2, 6) to "Glorious Purpose"
            )
        ),
        100088 to MockShowState(
            showTmdbId = 100088,
            showTitle = "The Last of Us",
            showPosterPath = "https://image.tmdb.org/t/p/w500/uKvVjHNqB5VmOrdxqAt2V7JMrne.jpg",
            totalAired = 9,
            totalCompleted = 4,
            seasonNumber = 1,
            episodeNumber = 5,
            episodeTitles = mapOf(
                Pair(1, 1) to "When You're Lost in the Darkness",
                Pair(1, 2) to "Infected",
                Pair(1, 3) to "Long, Long Time",
                Pair(1, 4) to "Please Hold to My Hand",
                Pair(1, 5) to "Endure and Survive",
                Pair(1, 6) to "Kin",
                Pair(1, 7) to "Left Behind",
                Pair(1, 8) to "When We Are in Need",
                Pair(1, 9) to "Look for the Light"
            )
        ),
        87108 to MockShowState(
            showTmdbId = 87108,
            showTitle = "Chernobyl",
            showPosterPath = "https://image.tmdb.org/t/p/w500/hlLXt2tOPT6RRnjiUmoxyG1LTFi.jpg",
            totalAired = 5,
            totalCompleted = 2,
            seasonNumber = 1,
            episodeNumber = 3,
            episodeTitles = mapOf(
                Pair(1, 1) to "1:23:45",
                Pair(1, 2) to "Please Remain Calm",
                Pair(1, 3) to "Open Wide, O Earth",
                Pair(1, 4) to "The Happiness of All Mankind",
                Pair(1, 5) to "Vichnaya Pamyat"
            )
        ),
        60625 to MockShowState(
            showTmdbId = 60625,
            showTitle = "Rick and Morty",
            showPosterPath = "https://image.tmdb.org/t/p/w500/gdIrmfZUZZwDd5D99Yv7f3AqlvU.jpg",
            totalAired = 71,
            totalCompleted = 54,
            seasonNumber = 6,
            episodeNumber = 5,
            episodeTitles = mapOf(
                Pair(6, 1) to "Solaricks",
                Pair(6, 2) to "Rick: A Mort Well Lived",
                Pair(6, 3) to "Bethic Twinstinct",
                Pair(6, 4) to "Night Family",
                Pair(6, 5) to "Final DeSmithation",
                Pair(6, 6) to "Juracsic Mort",
                Pair(6, 7) to "Full Meta Jackrick",
                Pair(6, 8) to "Analyze Piss",
                Pair(6, 9) to "A Rick in King Mortur's Court",
                Pair(6, 10) to "Ricktional Mortpoon's Rickmas Mortcation"
            )
        ),
        110492 to MockShowState(
            showTmdbId = 110492,
            showTitle = "Peacemaker",
            showPosterPath = "https://image.tmdb.org/t/p/w500/hE3LRZAY8cuGKUsqN8Zb6esxLHc.jpg",
            totalAired = 8,
            totalCompleted = 4,
            seasonNumber = 1,
            episodeNumber = 5,
            episodeTitles = mapOf(
                Pair(1, 1) to "A Whole New Whirled",
                Pair(1, 2) to "Best Friends, For Never",
                Pair(1, 3) to "Better Goff Dead",
                Pair(1, 4) to "The Choad Less Traveled",
                Pair(1, 5) to "Monkey Dory",
                Pair(1, 6) to "Murn After Reading",
                Pair(1, 7) to "Stop Dragon My Heart Around",
                Pair(1, 8) to "It's Cow or Never"
            )
        ),
        119051 to MockShowState(
            showTmdbId = 119051,
            showTitle = "Wednesday",
            showPosterPath = "https://image.tmdb.org/t/p/w500/9PFonQ921cwhEj6EwegWKMncRmH.jpg",
            totalAired = 8,
            totalCompleted = 3,
            seasonNumber = 1,
            episodeNumber = 4,
            episodeTitles = mapOf(
                Pair(1, 1) to "Wednesday's Child Is Full of Woe",
                Pair(1, 2) to "Woe Is the Loneliest Number",
                Pair(1, 3) to "Friend or Woe",
                Pair(1, 4) to "Woe What a Night",
                Pair(1, 5) to "You Reap What You Woe",
                Pair(1, 6) to "Quid Pro Woe",
                Pair(1, 7) to "If You Don't Woe Me by Now",
                Pair(1, 8) to "A Murder of Woes"
            )
        ),
        66732 to MockShowState(
            showTmdbId = 66732,
            showTitle = "Stranger Things",
            showPosterPath = "https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
            totalAired = 34,
            totalCompleted = 25,
            seasonNumber = 4,
            episodeNumber = 1,
            episodeTitles = mapOf(
                Pair(4, 1) to "The Hellfire Club",
                Pair(4, 2) to "Vecna's Curse",
                Pair(4, 3) to "The Monster and the Superhero",
                Pair(4, 4) to "Dear Billy",
                Pair(4, 5) to "The Nina Project",
                Pair(4, 6) to "The Dive",
                Pair(4, 7) to "The Massacre at Hawkins Lab",
                Pair(4, 8) to "Papa",
                Pair(4, 9) to "The Piggyback"
            )
        ),
        70523 to MockShowState(
            showTmdbId = 70523,
            showTitle = "Dark",
            showPosterPath = "https://image.tmdb.org/t/p/w500/apbrbWs8M9lyOpJYU5WXrpFbk1Z.jpg",
            totalAired = 26,
            totalCompleted = 18,
            seasonNumber = 3,
            episodeNumber = 1,
            episodeTitles = mapOf(
                Pair(3, 1) to "Deja-vu",
                Pair(3, 2) to "The Survivors",
                Pair(3, 3) to "Adam and Eva",
                Pair(3, 4) to "The Origin",
                Pair(3, 5) to "Life and Death",
                Pair(3, 6) to "Light and Shadow",
                Pair(3, 7) to "Between the Time",
                Pair(3, 8) to "The Paradise"
            )
        ),
        85552 to MockShowState(
            showTmdbId = 85552,
            showTitle = "Euphoria",
            showPosterPath = "https://image.tmdb.org/t/p/w500/3Q0hd3heuW4YNiVYWYNkNCUm1AJ.jpg",
            totalAired = 18,
            totalCompleted = 10,
            seasonNumber = 2,
            episodeNumber = 3,
            episodeTitles = mapOf(
                Pair(2, 1) to "Trying to Get to Heaven Before They Close the Door",
                Pair(2, 2) to "Out of Touch",
                Pair(2, 3) to "Ruminations: Big and Little Bullys",
                Pair(2, 4) to "You Who Cannot See, Think of Those Who Can",
                Pair(2, 5) to "Stand Still Like the Hummingbird",
                Pair(2, 6) to "A Thousand Little Trees of Blood",
                Pair(2, 7) to "The Theater and It's Double",
                Pair(2, 8) to "All My Life, My Heart Has Yearned for a Thing I Cannot Name"
            )
        ),
        63247 to MockShowState(
            showTmdbId = 63247,
            showTitle = "Westworld",
            showPosterPath = "https://image.tmdb.org/t/p/w500/y55oBgf69rRIaqEG9IlCDETGmmW.jpg",
            totalAired = 36,
            totalCompleted = 15,
            seasonNumber = 2,
            episodeNumber = 6,
            episodeTitles = mapOf(
                Pair(2, 1) to "Journey into Night",
                Pair(2, 2) to "Reunion",
                Pair(2, 3) to "Virtù e Fortuna",
                Pair(2, 4) to "The Riddle of the Sphinx",
                Pair(2, 5) to "Akane No Mai",
                Pair(2, 6) to "Phase Space",
                Pair(2, 7) to "Les Écorchés",
                Pair(2, 8) to "Kiksuya",
                Pair(2, 9) to "Vanishing Point",
                Pair(2, 10) to "The Passenger"
            )
        ),
        60574 to MockShowState(
            showTmdbId = 60574,
            showTitle = "Peaky Blinders",
            showPosterPath = "https://image.tmdb.org/t/p/w500/vUUqzWa2LnHIVqkaKVlVGkVcZIW.jpg",
            totalAired = 36,
            totalCompleted = 20,
            seasonNumber = 4,
            episodeNumber = 3,
            episodeTitles = mapOf(
                Pair(4, 1) to "The Noose",
                Pair(4, 2) to "Heathens",
                Pair(4, 3) to "Blackbird",
                Pair(4, 4) to "Dangerous",
                Pair(4, 5) to "The Duel",
                Pair(4, 6) to "The Company"
            )
        ),
        82856 to MockShowState(
            showTmdbId = 82856,
            showTitle = "The Mandalorian",
            showPosterPath = "https://image.tmdb.org/t/p/w500/eU1i6eHXlzMOlEq0ku1R07Y87Nu.jpg",
            totalAired = 24,
            totalCompleted = 12,
            seasonNumber = 2,
            episodeNumber = 5,
            episodeTitles = mapOf(
                Pair(2, 1) to "Chapter 9: The Marshal",
                Pair(2, 2) to "Chapter 10: The Passenger",
                Pair(2, 3) to "Chapter 11: The Heiress",
                Pair(2, 4) to "Chapter 12: The Siege",
                Pair(2, 5) to "Chapter 13: The Jedi",
                Pair(2, 6) to "Chapter 14: The Tragedy",
                Pair(2, 7) to "Chapter 15: The Believer",
                Pair(2, 8) to "Chapter 16: The Rescue"
            )
        ),
        95557 to MockShowState(
            showTmdbId = 95557,
            showTitle = "Invincible",
            showPosterPath = "https://image.tmdb.org/t/p/w500/dMOIBxrUBbzg7jyR5k1Hw7sNnlY.jpg",
            totalAired = 16,
            totalCompleted = 7,
            seasonNumber = 1,
            episodeNumber = 8,
            episodeTitles = mapOf(
                Pair(1, 1) to "It's About Time",
                Pair(1, 2) to "Here Goes Nothing",
                Pair(1, 3) to "Who You Calling Ugly?",
                Pair(1, 4) to "Neil Armstrong, Eat Your Heart Out",
                Pair(1, 5) to "That Actually Hurt",
                Pair(1, 6) to "You Look Kinda Dead",
                Pair(1, 7) to "We Need to Talk",
                Pair(1, 8) to "Where I Really Come From"
            )
        ),
        4614 to MockShowState(
            showTmdbId = 4614,
            showTitle = "The Wire",
            showPosterPath = "https://image.tmdb.org/t/p/w500/4lbclFySvugI51fws0S1I29QI00.jpg",
            totalAired = 60,
            totalCompleted = 25,
            seasonNumber = 3,
            episodeNumber = 6,
            episodeTitles = mapOf(
                Pair(3, 1) to "Time After Time",
                Pair(3, 2) to "All Due Respect",
                Pair(3, 3) to "Dead Soldiers",
                Pair(3, 4) to "Hamsterdam",
                Pair(3, 5) to "Straight and True",
                Pair(3, 6) to "Homecoming",
                Pair(3, 7) to "Back Burners",
                Pair(3, 8) to "Moral Midgetry",
                Pair(3, 9) to "Slapstick",
                Pair(3, 10) to "Reformation",
                Pair(3, 11) to "Middle Ground",
                Pair(3, 12) to "Mission Accomplished"
            )
        ),
        1398 to MockShowState(
            showTmdbId = 1398,
            showTitle = "The Sopranos",
            showPosterPath = "https://image.tmdb.org/t/p/w500/57q19Fk4Q1Rz9bU1qK2b4m1C3uM.jpg",
            totalAired = 86,
            totalCompleted = 30,
            seasonNumber = 3,
            episodeNumber = 5,
            episodeTitles = mapOf(
                Pair(3, 1) to "Mr. Ruggerio's Neighborhood",
                Pair(3, 2) to "Proshai, Livushka",
                Pair(3, 3) to "Fortunate Son",
                Pair(3, 4) to "Employee of the Month",
                Pair(3, 5) to "Another Toothpick",
                Pair(3, 6) to "University",
                Pair(3, 7) to "Second Opinion",
                Pair(3, 8) to "He Is Risen",
                Pair(3, 9) to "The Telltale Moozadell",
                Pair(3, 10) to "...To Save Us All from Satan's Power",
                Pair(3, 11) to "Pine Barrens",
                Pair(3, 12) to "Amour Fou",
                Pair(3, 13) to "Army of One"
            )
        ),
        84773 to MockShowState(
            showTmdbId = 84773,
            showTitle = "The Lord of the Rings: The Rings of Power",
            showPosterPath = "https://image.tmdb.org/t/p/w500/mYLOqiStMxDK3fYZuhCwTBch5XM.jpg",
            totalAired = 16,
            totalCompleted = 6,
            seasonNumber = 1,
            episodeNumber = 7,
            episodeTitles = mapOf(
                Pair(1, 1) to "Shadow of the Past",
                Pair(1, 2) to "Adrift",
                Pair(1, 3) to "Adar",
                Pair(1, 4) to "The Great Wave",
                Pair(1, 5) to "Partings",
                Pair(1, 6) to "Udûn",
                Pair(1, 7) to "The Eye",
                Pair(1, 8) to "Alloyed"
            )
        ),
        71446 to MockShowState(
            showTmdbId = 71446,
            showTitle = "Money Heist",
            showPosterPath = "https://image.tmdb.org/t/p/w500/reEMsq1tAnjG8NVgN56vlFIfq5b.jpg",
            totalAired = 41,
            totalCompleted = 28,
            seasonNumber = 4,
            episodeNumber = 2,
            episodeTitles = mapOf(
                Pair(4, 1) to "Game Over",
                Pair(4, 2) to "Berlin's Wedding",
                Pair(4, 3) to "Anatomy Lesson",
                Pair(4, 4) to "Pasodoble",
                Pair(4, 5) to "5 Minutes Earlier",
                Pair(4, 6) to "TKO",
                Pair(4, 7) to "Strike the Tent",
                Pair(4, 8) to "The Paris Plan"
            )
        ),
        88396 to MockShowState(
            showTmdbId = 88396,
            showTitle = "The Falcon and the Winter Soldier",
            showPosterPath = "https://image.tmdb.org/t/p/w500/6kbAMLteGO8yyewYau6bJ683sw7.jpg",
            totalAired = 6,
            totalCompleted = 3,
            seasonNumber = 1,
            episodeNumber = 4,
            episodeTitles = mapOf(
                Pair(1, 1) to "New World Order",
                Pair(1, 2) to "The Star-Spangled Man",
                Pair(1, 3) to "Power Broker",
                Pair(1, 4) to "The Whole World Is Watching",
                Pair(1, 5) to "Truth",
                Pair(1, 6) to "One World, One People"
            )
        ),
        75014 to MockShowState(
            showTmdbId = 75014,
            showTitle = "The Boys",
            showPosterPath = "https://image.tmdb.org/t/p/w500/7Ns6tO3aYjppI5LoFSTurkWv052.jpg",
            totalAired = 32,
            totalCompleted = 21,
            seasonNumber = 3,
            episodeNumber = 6,
            episodeTitles = mapOf(
                Pair(3, 1) to "Payback",
                Pair(3, 2) to "The Only Man In The Sky",
                Pair(3, 3) to "Barbary Coast",
                Pair(3, 4) to "Glorious Five Year Plan",
                Pair(3, 5) to "The Last Time to Look on This World of Lies",
                Pair(3, 6) to "Herogasm",
                Pair(3, 7) to "Here Comes a Candle to Light You to Bed",
                Pair(3, 8) to "The Instant White-Hot Wild"
            )
        ),
        92685 to MockShowState(
            showTmdbId = 92685,
            showTitle = "The White Lotus",
            showPosterPath = "https://image.tmdb.org/t/p/w500/gH5M325v94h2061g0fB10s2j5Hk.jpg",
            totalAired = 13,
            totalCompleted = 8,
            seasonNumber = 2,
            episodeNumber = 3,
            episodeTitles = mapOf(
                Pair(2, 1) to "Ciao",
                Pair(2, 2) to "Italian Dream",
                Pair(2, 3) to "Bull Elephants",
                Pair(2, 4) to "In the Sandbox",
                Pair(2, 5) to "That's Amore",
                Pair(2, 6) to "Abductions",
                Pair(2, 7) to "Arrivederci"
            )
        ),
        85937 to MockShowState(
            showTmdbId = 85937,
            showTitle = "Demon Slayer: Kimetsu no Yaiba",
            showPosterPath = "https://image.tmdb.org/t/p/w500/xUfRZu2mi8jH6SzQEJGP6tjBuYj.jpg",
            totalAired = 63,
            totalCompleted = 45,
            seasonNumber = 4,
            episodeNumber = 2,
            episodeTitles = mapOf(
                Pair(4, 1) to "To Defeat Muzan Kibutsuji",
                Pair(4, 2) to "Water Hashira Giyu Tomioka's Pain",
                Pair(4, 3) to "Fully Recovered Tanjiro Joins the Hashira Training!!",
                Pair(4, 4) to "To Bring a Smile to One's Face",
                Pair(4, 5) to "I Even Ate Demons...",
                Pair(4, 6) to "The Strongest of the Demon Slayer Corps",
                Pair(4, 7) to "Stone Hashira Gyomei Himejima",
                Pair(4, 8) to "The Hashira Unite"
            )
        ),
        60735 to MockShowState(
            showTmdbId = 60735,
            showTitle = "The Flash",
            showPosterPath = "https://image.tmdb.org/t/p/w500/lJA2RCMfsWoskqlQhXPSLFQGXbY.jpg",
            totalAired = 184,
            totalCompleted = 80,
            seasonNumber = 5,
            episodeNumber = 12,
            episodeTitles = mapOf(
                Pair(5, 1) to "Nora",
                Pair(5, 2) to "Blocked",
                Pair(5, 3) to "The Death of Vibe",
                Pair(5, 4) to "News Flash",
                Pair(5, 5) to "All Doll'd Up",
                Pair(5, 6) to "The Icicle Cometh",
                Pair(5, 7) to "O Come, All Ye Thankful",
                Pair(5, 8) to "What's Past Is Prologue",
                Pair(5, 9) to "Elseworlds, Part 1",
                Pair(5, 10) to "The Flash & The Furious",
                Pair(5, 11) to "Seeing Red",
                Pair(5, 12) to "Memorabilia"
            )
        ),
        67195 to MockShowState(
            showTmdbId = 67195,
            showTitle = "Legacies",
            showPosterPath = "https://image.tmdb.org/t/p/w500/3G6Wz1bfeWb7F78r04W5nN7b9hZ.jpg",
            totalAired = 68,
            totalCompleted = 32,
            seasonNumber = 3,
            episodeNumber = 1,
            episodeTitles = mapOf(
                Pair(3, 1) to "We're Not Worthy",
                Pair(3, 2) to "Goodbyes Sure Do Suck",
                Pair(3, 3) to "Hold on Tight",
                Pair(3, 4) to "Hold on Tight",
                Pair(3, 5) to "This Is What It Takes"
            )
        ),
        80752 to MockShowState(
            showTmdbId = 80752,
            showTitle = "See",
            showPosterPath = "https://image.tmdb.org/t/p/w500/lKDIhc9UQNU5QxwQI70uh5vL8oT.jpg",
            totalAired = 24,
            totalCompleted = 10,
            seasonNumber = 2,
            episodeNumber = 3,
            episodeTitles = mapOf(
                Pair(2, 1) to "Brothers and Sisters",
                Pair(2, 2) to "Forever",
                Pair(2, 3) to "The Compass",
                Pair(2, 4) to "The Witchfinder",
                Pair(2, 5) to "The Dinner Party",
                Pair(2, 6) to "The Truth About Unicorns",
                Pair(2, 7) to "The Queen's Speech",
                Pair(2, 8) to "Rock-a-Bye"
            )
        ),
        79744 to MockShowState(
            showTmdbId = 79744,
            showTitle = "The Rookie",
            showPosterPath = "https://image.tmdb.org/t/p/w500/t9UPeZzB7c4Nl3a9ZlQ42s7jK5c.jpg",
            totalAired = 108,
            totalCompleted = 45,
            seasonNumber = 3,
            episodeNumber = 8,
            episodeTitles = mapOf(
                Pair(3, 1) to "Consequences",
                Pair(3, 2) to "In Justice",
                Pair(3, 3) to "La Fiera",
                Pair(3, 4) to "Sabotage",
                Pair(3, 5) to "Lockdown",
                Pair(3, 6) to "Revelations",
                Pair(3, 7) to "True Crime",
                Pair(3, 8) to "Bad Blood"
            )
        ),
        82883 to MockShowState(
            showTmdbId = 82883,
            showTitle = "Sex Education",
            showPosterPath = "https://image.tmdb.org/t/p/w500/8j1207bZqc894m94Hq37A2cE8Yj.jpg",
            totalAired = 32,
            totalCompleted = 18,
            seasonNumber = 3,
            episodeNumber = 3,
            episodeTitles = mapOf(
                Pair(3, 1) to "Episode 1",
                Pair(3, 2) to "Episode 2",
                Pair(3, 3) to "Episode 3",
                Pair(3, 4) to "Episode 4",
                Pair(3, 5) to "Episode 5",
                Pair(3, 6) to "Episode 6",
                Pair(3, 7) to "Episode 7",
                Pair(3, 8) to "Episode 8"
            )
        ),
        76331 to MockShowState(
            showTmdbId = 76331,
            showTitle = "Succession",
            showPosterPath = "https://image.tmdb.org/t/p/w500/7TafgV4rYpZp3B2E9V5bY5q4N.jpg",
            totalAired = 39,
            totalCompleted = 37,
            seasonNumber = 4,
            episodeNumber = 9,
            episodeTitles = mapOf(
                Pair(4, 9) to "Church and State",
                Pair(4, 10) to "With Open Eyes"
            )
        )
    )

    private fun getActiveClientId(): String {
        return debugConfigManager.customTraktClientId.value.ifBlank {
            "38848a60debb2652b41295b9588ebbf45b14f6bdf6a22f77ffbcad5ce29aaeb5"
        }
    }

    override fun getUpNextQueueFlow(accessToken: String): Flow<List<TraktUpNextEpisode>> {
        return showWatchProgressDao.getUpNextQueueFlow().map { progressList ->
            if (debugConfigManager.isMockTraktEnabled.value) {
                mockShows.values
                    .filter { it.totalCompleted < it.totalAired }
                    .mapNotNull { show ->
                        val epTitle =
                            show.episodeTitles[Pair(show.seasonNumber, show.episodeNumber)]
                        TraktUpNextEpisode(
                            showTmdbId = show.showTmdbId,
                            showTitle = show.showTitle,
                            showPosterPath = show.showPosterPath,
                            seasonNumber = show.seasonNumber,
                            episodeNumber = show.episodeNumber,
                            episodeTitle = epTitle,
                            seasonCompleted = show.seasonCompleted,
                            seasonTotalAired = show.seasonTotalAired,
                            totalCompleted = show.totalCompleted,
                            totalAired = show.totalAired
                        )
                    }
            } else {
                progressList.map { progress ->
                    TraktUpNextEpisode(
                        showTmdbId = progress.showId,
                        showTitle = progress.showTitle,
                        showPosterPath = progress.showPosterPath,
                        seasonNumber = progress.seasonNumber,
                        episodeNumber = progress.episodeNumber,
                        episodeTitle = progress.episodeTitle,
                        seasonCompleted = progress.seasonCompleted,
                        seasonTotalAired = progress.seasonTotalAired,
                        totalCompleted = progress.totalCompleted,
                        totalAired = progress.totalAired
                    )
                }
            }
        }
    }

    override fun getWatchedEpisodesFlow(showId: Int, seasonNumber: Int): Flow<Set<Int>> {
        return episodeWatchHistoryDao.getWatchedEpisodeNumbersFlow(showId, seasonNumber)
            .map { it.toSet() }
    }

    override fun getWatchedSeasonsFlow(showId: Int): Flow<Set<Int>> {
        return episodeWatchHistoryDao.getWatchedSeasonsFlow(showId)
            .map { it.toSet() }
    }

    override fun getSeasonWatchCountsFlow(showId: Int): Flow<Map<Int, Int>> {
        return episodeWatchHistoryDao.getSeasonWatchCountsFlow(showId).map { counts ->
            counts.associate { it.seasonNumber to it.watchedCount }
        }
    }

    override fun isEpisodeWatchedFlow(
        showId: Int,
        seasonNumber: Int,
        episodeNumber: Int
    ): Flow<Boolean> {
        return episodeWatchHistoryDao.isEpisodeWatchedFlow(showId, seasonNumber, episodeNumber)
    }

    override suspend fun syncLibrary(accessToken: String): Result<TraktSyncResult> =
        withContext(Dispatchers.IO) {
            try {
                if (debugConfigManager.isMockTraktEnabled.value) {
                    // Mock Sync: Add 3 mock watchlist items and 2 history items to Room DB
                    val mockWatchlist = listOf(
                        WatchlistEntity(
                            mediaId = 93405,
                            mediaType = "Tv",
                            title = "Severance",
                            posterImageUrl = "https://image.tmdb.org/t/p/w500/abfJnkhz4c24t1GqSgq2J61z4e2.jpg",
                            backdropImageUrl = "",
                            voteAvg = 8.4f,
                            releaseDate = "2022-02-18"
                        ),
                        WatchlistEntity(
                            mediaId = 157336,
                            mediaType = "Movie",
                            title = "Interstellar",
                            posterImageUrl = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                            backdropImageUrl = "",
                            voteAvg = 8.6f,
                            releaseDate = "2014-11-05"
                        ),
                        WatchlistEntity(
                            mediaId = 94997,
                            mediaType = "Tv",
                            title = "House of the Dragon",
                            posterImageUrl = "https://image.tmdb.org/t/p/w500/7QMsOTMUswlwxJP0rTTZfmz2tX2.jpg",
                            backdropImageUrl = "",
                            voteAvg = 8.4f,
                            releaseDate = "2022-08-21"
                        )
                    )
                    val mockHistory = listOf(
                        WatchHistoryEntity(
                            mediaId = 550,
                            mediaType = "Movie",
                            title = "Fight Club",
                            posterImageUrl = "",
                            voteAvg = 8.4f
                        ),
                        WatchHistoryEntity(
                            mediaId = 1396,
                            mediaType = "Tv",
                            title = "Breaking Bad",
                            posterImageUrl = "",
                            voteAvg = 8.9f
                        )
                    )
                    watchlistDao.insertAll(mockWatchlist)
                    watchHistoryDao.insertAll(mockHistory)

                    widgetSyncNotifier?.notifyWidgetDataChanged()

                    return@withContext Result.success(
                        TraktSyncResult(
                            itemsImportedToWatchlist = mockWatchlist.size,
                            itemsImportedToHistory = mockHistory.size,
                            itemsExportedToTrakt = 2
                        )
                    )
                }

                val clientId = getActiveClientId()
                val bearer = "Bearer $accessToken"
                var importedWatchlist = 0
                var importedHistory = 0
                var exportedItems = 0

                // 1. Sync Watchlist (Trakt -> Room DB)
                when (val watchlistRes = traktSyncService.getWatchlist(bearer, clientId)) {
                    is ApiResponse.Success -> {
                        val traktWatchlist = watchlistRes.body
                        val entitiesToInsert = mutableListOf<WatchlistEntity>()

                        traktWatchlist.forEach { item ->
                            val tmdbId = item.movie?.ids?.tmdb ?: item.show?.ids?.tmdb
                            val title = item.movie?.title ?: item.show?.title ?: ""
                            val mediaType = if (item.movie != null) "Movie" else "Tv"

                            if (tmdbId != null && tmdbId > 0) {
                                entitiesToInsert.add(
                                    WatchlistEntity(
                                        mediaId = tmdbId,
                                        mediaType = mediaType,
                                        title = title,
                                        posterImageUrl = "",
                                        backdropImageUrl = "",
                                        voteAvg = 0f,
                                        releaseDate = "",
                                        addedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        }

                        if (entitiesToInsert.isNotEmpty()) {
                            watchlistDao.insertAll(entitiesToInsert)
                            importedWatchlist = entitiesToInsert.size
                        }

                        // Export local watchlist items not yet in Trakt
                        val localWatchlist = watchlistDao.getAllWatchlist()
                        val remoteIds =
                            traktWatchlist.mapNotNull { it.movie?.ids?.tmdb ?: it.show?.ids?.tmdb }
                                .toSet()
                        val missingMovies =
                            localWatchlist.filter { it.mediaType == "Movie" && it.mediaId !in remoteIds }
                                .map {
                                    TraktMediaItemIdentifier(
                                        ids = TraktIds(tmdb = it.mediaId),
                                        title = it.title
                                    )
                                }
                        val missingShows =
                            localWatchlist.filter { it.mediaType == "Tv" && it.mediaId !in remoteIds }
                                .map {
                                    TraktMediaItemIdentifier(
                                        ids = TraktIds(tmdb = it.mediaId),
                                        title = it.title
                                    )
                                }

                        if (missingMovies.isNotEmpty() || missingShows.isNotEmpty()) {
                            traktSyncService.addToWatchlist(
                                bearerToken = bearer,
                                clientId = clientId,
                                payload = TraktSyncBody(
                                    movies = missingMovies,
                                    shows = missingShows
                                )
                            )
                            exportedItems += (missingMovies.size + missingShows.size)
                        }
                    }

                    else -> {}
                }

                // 2. Sync History (Trakt -> Room DB)
                when (val historyRes = traktSyncService.getHistory(bearer, clientId, limit = 100)) {
                    is ApiResponse.Success -> {
                        val traktHistory = historyRes.body
                        val historyToInsert = mutableListOf<WatchHistoryEntity>()

                        traktHistory.forEach { item ->
                            val tmdbId = item.movie?.ids?.tmdb ?: item.show?.ids?.tmdb
                            val title = item.movie?.title ?: item.show?.title ?: ""
                            val mediaType = if (item.movie != null) "Movie" else "Tv"

                            if (tmdbId != null && tmdbId > 0) {
                                historyToInsert.add(
                                    WatchHistoryEntity(
                                        mediaId = tmdbId,
                                        mediaType = mediaType,
                                        title = title,
                                        posterImageUrl = "",
                                        voteAvg = 0f,
                                        watchedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                        }

                        if (historyToInsert.isNotEmpty()) {
                            watchHistoryDao.insertAll(historyToInsert)
                            importedHistory = historyToInsert.size
                        }
                    }

                    else -> {}
                }

                widgetSyncNotifier?.notifyWidgetDataChanged()

                Result.success(
                    TraktSyncResult(
                        itemsImportedToWatchlist = importedWatchlist,
                        itemsImportedToHistory = importedHistory,
                        itemsExportedToTrakt = exportedItems
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getUpNextQueue(accessToken: String): Result<List<TraktUpNextEpisode>> =
        withContext(Dispatchers.IO) {
            try {
                if (debugConfigManager.isMockTraktEnabled.value) {
                    val mockQueue = mockShows.values
                        .filter { it.totalCompleted < it.totalAired }
                        .mapNotNull { show ->
                            val epTitle =
                                show.episodeTitles[Pair(show.seasonNumber, show.episodeNumber)]
                            TraktUpNextEpisode(
                                showTmdbId = show.showTmdbId,
                                showTitle = show.showTitle,
                                showPosterPath = show.showPosterPath,
                                seasonNumber = show.seasonNumber,
                                episodeNumber = show.episodeNumber,
                                episodeTitle = epTitle,
                                seasonCompleted = show.seasonCompleted,
                                seasonTotalAired = show.seasonTotalAired,
                                totalCompleted = show.totalCompleted,
                                totalAired = show.totalAired
                            )
                        }
                    return@withContext Result.success(mockQueue)
                }

                if (accessToken.isNotBlank()) {
                    val clientId = getActiveClientId()
                    val bearer = "Bearer $accessToken"
                    when (val res = traktSyncService.getWatchedShowProgress(bearer, clientId)) {
                        is ApiResponse.Success -> {
                            val upNextList = res.body.mapNotNull { showProgress ->
                                val showTmdbId = showProgress.show.ids.tmdb
                                val nextEp = showProgress.nextEpisode

                                if (showTmdbId != null && nextEp != null) {
                                    TraktUpNextEpisode(
                                        showTmdbId = showTmdbId,
                                        showTitle = showProgress.show.title,
                                        showPosterPath = null,
                                        seasonNumber = nextEp.season,
                                        episodeNumber = nextEp.number,
                                        episodeTitle = nextEp.title,
                                        seasonCompleted = showProgress.completed,
                                        seasonTotalAired = showProgress.aired,
                                        totalAired = showProgress.aired,
                                        totalCompleted = showProgress.completed
                                    )
                                } else null
                            }
                            return@withContext Result.success(upNextList)
                        }

                        is ApiResponse.Error -> {
                            // Fall back to local DB if Trakt network error
                        }
                    }
                }

                // Production: 100% dynamic Up Next queue from SQLite Room DB
                val localQueue = showWatchProgressDao.getUpNextQueue().map { progress ->
                    TraktUpNextEpisode(
                        showTmdbId = progress.showId,
                        showTitle = progress.showTitle,
                        showPosterPath = progress.showPosterPath,
                        seasonNumber = progress.seasonNumber,
                        episodeNumber = progress.episodeNumber,
                        episodeTitle = progress.episodeTitle,
                        seasonCompleted = progress.seasonCompleted,
                        seasonTotalAired = progress.seasonTotalAired,
                        totalCompleted = progress.totalCompleted,
                        totalAired = progress.totalAired
                    )
                }
                Result.success(localQueue)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun markEpisodeWatched(
        accessToken: String?,
        showTmdbId: Int,
        season: Int,
        episode: Int,
        showTitle: String,
        showPosterPath: String?,
        episodeTitle: String?,
        totalAired: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val isCurrentlyWatched =
                episodeWatchHistoryDao.isEpisodeWatched(showTmdbId, season, episode)
            if (isCurrentlyWatched) {
                episodeWatchHistoryDao.deleteEpisode(showTmdbId, season, episode)
            } else {
                episodeWatchHistoryDao.insertEpisode(
                    EpisodeWatchHistoryEntity(
                        showId = showTmdbId,
                        seasonNumber = season,
                        episodeNumber = episode,
                        watchedAt = System.currentTimeMillis()
                    )
                )
            }

            val currentProgress = showWatchProgressDao.getProgress(showTmdbId)
            val watchedEpisodes = episodeWatchHistoryDao.getAllWatchedEpisodes(showTmdbId)
            val watchedSet = watchedEpisodes.map { Pair(it.seasonNumber, it.episodeNumber) }.toSet()
            val actualCompleted = watchedSet.size

            val actualTitle = if (showTitle.isNotBlank()) {
                if (showTitle.matches(Regex("Season \\d+.*", RegexOption.IGNORE_CASE))) {
                    currentProgress?.showTitle.orEmpty().ifBlank { showTitle }
                } else {
                    showTitle
                }
            } else {
                currentProgress?.showTitle.orEmpty().ifBlank { "TV Show" }
            }
            val actualPoster = showPosterPath ?: currentProgress?.showPosterPath

            if (actualCompleted == 0) {
                showWatchProgressDao.deleteByShowId(showTmdbId)
            } else {
                val resolution = resolveNextEpisodeResolution(
                    showTmdbId = showTmdbId,
                    watchedSet = watchedSet,
                    currentProgress = currentProgress,
                    passedSeason = season,
                    passedTotalAired = totalAired
                )
                val nextEpTitle = resolveEpisodeTitle(
                    showTmdbId = showTmdbId,
                    seasonNumber = resolution.seasonNumber,
                    episodeNumber = resolution.episodeNumber,
                    explicitTitle = episodeTitle
                )

                showWatchProgressDao.insertOrUpdate(
                    ShowWatchProgressEntity(
                        showId = showTmdbId,
                        showTitle = actualTitle,
                        showPosterPath = actualPoster,
                        seasonNumber = resolution.seasonNumber,
                        episodeNumber = resolution.episodeNumber,
                        episodeTitle = nextEpTitle ?: "Episode ${resolution.episodeNumber}",
                        seasonCompleted = resolution.seasonCompleted,
                        seasonTotalAired = resolution.seasonTotalAired,
                        totalCompleted = resolution.totalCompleted,
                        totalAired = resolution.totalAired,
                        lastWatchedAt = currentProgress?.lastWatchedAt ?: System.currentTimeMillis()
                    )
                )
            }

            if (debugConfigManager.isMockTraktEnabled.value) {
                val show = mockShows[showTmdbId]
                if (show != null) {
                    if (isCurrentlyWatched) {
                        show.totalCompleted = (show.totalCompleted - 1).coerceAtLeast(0)
                        show.episodeNumber = (show.episodeNumber - 1).coerceAtLeast(1)
                    } else {
                        show.totalCompleted =
                            (show.totalCompleted + 1).coerceAtMost(show.totalAired)
                        show.episodeNumber += 1
                    }
                }
            }

            if (!accessToken.isNullOrBlank() && !debugConfigManager.isMockTraktEnabled.value) {
                val clientId = getActiveClientId()
                val bearer = "Bearer $accessToken"
                val payload = TraktSyncBody(
                    shows = listOf(
                        TraktMediaItemIdentifier(ids = TraktIds(tmdb = showTmdbId))
                    )
                )
                traktSyncService.addToHistory(bearer, clientId, payload)
            }

            widgetSyncNotifier?.notifyWidgetDataChanged()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markSeasonWatched(
        accessToken: String?,
        showTmdbId: Int,
        season: Int,
        episodeNumbers: List<Int>,
        showTitle: String,
        showPosterPath: String?,
        totalAired: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentProgress = showWatchProgressDao.getProgress(showTmdbId)
            val actualTitle = if (showTitle.isNotBlank()) {
                if (showTitle.matches(Regex("Season \\d+.*", RegexOption.IGNORE_CASE))) {
                    currentProgress?.showTitle.orEmpty().ifBlank { showTitle }
                } else {
                    showTitle
                }
            } else {
                currentProgress?.showTitle.orEmpty().ifBlank { "TV Show" }
            }
            val actualPoster = showPosterPath ?: currentProgress?.showPosterPath

            if (episodeNumbers.isEmpty()) {
                // Unmark season
                episodeWatchHistoryDao.deleteSeason(showTmdbId, season)
                val remainingWatched = episodeWatchHistoryDao.getAllWatchedEpisodes(showTmdbId)
                if (remainingWatched.isEmpty()) {
                    showWatchProgressDao.deleteByShowId(showTmdbId)
                } else {
                    val watchedSet =
                        remainingWatched.map { Pair(it.seasonNumber, it.episodeNumber) }.toSet()
                    val resolution = resolveNextEpisodeResolution(
                        showTmdbId = showTmdbId,
                        watchedSet = watchedSet,
                        currentProgress = currentProgress,
                        passedSeason = season,
                        passedTotalAired = totalAired
                    )
                    val nextEpTitle = resolveEpisodeTitle(
                        showTmdbId = showTmdbId,
                        seasonNumber = resolution.seasonNumber,
                        episodeNumber = resolution.episodeNumber,
                        explicitTitle = null
                    )

                    showWatchProgressDao.insertOrUpdate(
                        ShowWatchProgressEntity(
                            showId = showTmdbId,
                            showTitle = actualTitle,
                            showPosterPath = actualPoster,
                            seasonNumber = resolution.seasonNumber,
                            episodeNumber = resolution.episodeNumber,
                            episodeTitle = nextEpTitle ?: "Episode ${resolution.episodeNumber}",
                            seasonCompleted = resolution.seasonCompleted,
                            seasonTotalAired = resolution.seasonTotalAired,
                            totalCompleted = resolution.totalCompleted,
                            totalAired = resolution.totalAired,
                            lastWatchedAt = System.currentTimeMillis()
                        )
                    )
                }
            } else {
                // Mark all episodes in season
                val entities = episodeNumbers.map { epNum ->
                    EpisodeWatchHistoryEntity(
                        showId = showTmdbId,
                        seasonNumber = season,
                        episodeNumber = epNum,
                        watchedAt = System.currentTimeMillis()
                    )
                }
                episodeWatchHistoryDao.insertAll(entities)

                val watchedEpisodes = episodeWatchHistoryDao.getAllWatchedEpisodes(showTmdbId)
                val watchedSet =
                    watchedEpisodes.map { Pair(it.seasonNumber, it.episodeNumber) }.toSet()
                val resolution = resolveNextEpisodeResolution(
                    showTmdbId = showTmdbId,
                    watchedSet = watchedSet,
                    currentProgress = currentProgress,
                    passedSeason = season,
                    passedTotalAired = totalAired,
                    isSeasonCompleteAction = true
                )
                val nextEpTitle = resolveEpisodeTitle(
                    showTmdbId = showTmdbId,
                    seasonNumber = resolution.seasonNumber,
                    episodeNumber = resolution.episodeNumber,
                    explicitTitle = null
                )

                showWatchProgressDao.insertOrUpdate(
                    ShowWatchProgressEntity(
                        showId = showTmdbId,
                        showTitle = actualTitle,
                        showPosterPath = actualPoster,
                        seasonNumber = resolution.seasonNumber,
                        episodeNumber = resolution.episodeNumber,
                        episodeTitle = nextEpTitle ?: "Episode ${resolution.episodeNumber}",
                        seasonCompleted = resolution.seasonCompleted,
                        seasonTotalAired = resolution.seasonTotalAired,
                        totalCompleted = resolution.totalCompleted,
                        totalAired = resolution.totalAired,
                        lastWatchedAt = System.currentTimeMillis()
                    )
                )
            }

            if (debugConfigManager.isMockTraktEnabled.value) {
                val show = mockShows[showTmdbId]
                if (show != null) {
                    if (episodeNumbers.isEmpty()) {
                        show.totalCompleted = (show.totalCompleted - 10).coerceAtLeast(0)
                        show.episodeNumber = 1
                    } else {
                        show.totalCompleted =
                            (show.totalCompleted + episodeNumbers.size).coerceAtMost(show.totalAired)
                        show.seasonNumber += 1
                        show.episodeNumber = 1
                    }
                }
            }

            if (!accessToken.isNullOrBlank() && !debugConfigManager.isMockTraktEnabled.value) {
                val clientId = getActiveClientId()
                val bearer = "Bearer $accessToken"
                val payload = TraktSyncBody(
                    shows = listOf(
                        TraktMediaItemIdentifier(ids = TraktIds(tmdb = showTmdbId))
                    )
                )
                traktSyncService.addToHistory(bearer, clientId, payload)
            }

            widgetSyncNotifier?.notifyWidgetDataChanged()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMovieWatched(
        accessToken: String?,
        movieTmdbId: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (accessToken.isNullOrBlank() || debugConfigManager.isMockTraktEnabled.value) {
                return@withContext Result.success(Unit)
            }

            val clientId = getActiveClientId()
            val bearer = "Bearer $accessToken"
            val payload = TraktSyncBody(
                movies = listOf(
                    TraktMediaItemIdentifier(ids = TraktIds(tmdb = movieTmdbId))
                )
            )
            traktSyncService.addToHistory(bearer, clientId, payload)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private data class NextEpisodeResolution(
        val seasonNumber: Int,
        val episodeNumber: Int,
        val seasonCompleted: Int,
        val seasonTotalAired: Int,
        val totalCompleted: Int,
        val totalAired: Int
    )

    private suspend fun resolveNextEpisodeResolution(
        showTmdbId: Int,
        watchedSet: Set<Pair<Int, Int>>,
        currentProgress: ShowWatchProgressEntity?,
        passedSeason: Int,
        passedTotalAired: Int,
        isSeasonCompleteAction: Boolean = false
    ): NextEpisodeResolution {
        if (watchedSet.isEmpty()) {
            return NextEpisodeResolution(
                seasonNumber = 1,
                episodeNumber = 1,
                seasonCompleted = 0,
                seasonTotalAired = 1,
                totalCompleted = 0,
                totalAired = maxOf(passedTotalAired, 1)
            )
        }

        // 1. Fetch real season metadata from TMDB if available
        val remoteShow = try {
            val res = tmdbApiService?.getTvShowDetails(showTmdbId, emptyMap())
            (res as? ApiResponse.Success)?.body
        } catch (e: Exception) {
            null
        }

        val remoteSeasons: List<Pair<Int, Int>> = remoteShow?.seasons
            ?.filter { it.seasonNumber > 0 }
            ?.sortedBy { it.seasonNumber }
            ?.map { Pair(it.seasonNumber, it.episodeCount) }
            .orEmpty()

        val minSeason = watchedSet.minOf { it.first }
        val maxSeason = maxOf(watchedSet.maxOf { it.first }, passedSeason)

        // 2. Build complete seasons list combining remote TMDB, watched set, and passed params
        val allSeasonNumbers = if (remoteSeasons.isNotEmpty()) {
            remoteSeasons.map { it.first }
        } else {
            (minSeason..maxSeason).toList()
        }

        val totalShowAired = remoteShow?.episodeCount?.takeIf { it > 0 }
            ?: maxOf(
                currentProgress?.totalAired ?: 0,
                passedTotalAired,
                watchedSet.size
            )

        val seasonMetaList = allSeasonNumbers.map { sNum ->
            val remoteCount = remoteSeasons.firstOrNull { it.first == sNum }?.second ?: 0
            val maxWatchedInSeason =
                watchedSet.filter { it.first == sNum }.maxOfOrNull { it.second } ?: 1
            val seasonCount = when {
                remoteCount > 0 -> remoteCount
                currentProgress?.seasonNumber == sNum && currentProgress.seasonTotalAired > 0 -> currentProgress.seasonTotalAired
                sNum == passedSeason && passedTotalAired > 0 && passedTotalAired < totalShowAired -> passedTotalAired
                sNum == passedSeason && isSeasonCompleteAction -> maxWatchedInSeason
                allSeasonNumbers.size > 1 && sNum < maxSeason -> maxWatchedInSeason
                else -> maxOf(maxWatchedInSeason + 1, 1)
            }
            Pair(sNum, maxOf(seasonCount, maxWatchedInSeason, 1))
        }

        // 3. Find earliest unwatched episode across seasons (Option A: Continuous Chronological Watch Front)
        var targetSeason = seasonMetaList.firstOrNull()?.first ?: minSeason
        var targetEpisode = 1
        var targetSeasonCompleted = 0
        var targetSeasonTotalAired = seasonMetaList.firstOrNull()?.second ?: 1
        var foundUnwatched = false

        for ((sNum, sCount) in seasonMetaList) {
            val sCompleted = watchedSet.count { it.first == sNum }
            for (e in 1..sCount) {
                if (Pair(sNum, e) !in watchedSet) {
                    targetSeason = sNum
                    targetEpisode = e
                    targetSeasonCompleted = sCompleted
                    targetSeasonTotalAired = sCount
                    foundUnwatched = true
                    break
                }
            }
            if (foundUnwatched) break
        }

        if (!foundUnwatched) {
            if (watchedSet.size < totalShowAired) {
                // Advance to next season episode 1
                targetSeason = maxSeason + 1
                targetEpisode = 1
                targetSeasonCompleted = 0
                targetSeasonTotalAired =
                    remoteSeasons.firstOrNull { it.first == targetSeason }?.second ?: 10
            } else {
                // All episodes across all seasons completed
                val lastSeason = seasonMetaList.lastOrNull() ?: Pair(passedSeason, 1)
                targetSeason = lastSeason.first
                targetEpisode = lastSeason.second
                targetSeasonCompleted = lastSeason.second
                targetSeasonTotalAired = lastSeason.second
            }
        }

        return NextEpisodeResolution(
            seasonNumber = targetSeason,
            episodeNumber = targetEpisode,
            seasonCompleted = targetSeasonCompleted,
            seasonTotalAired = targetSeasonTotalAired,
            totalCompleted = watchedSet.size,
            totalAired = maxOf(totalShowAired, watchedSet.size)
        )
    }

    private suspend fun resolveEpisodeTitle(
        showTmdbId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        explicitTitle: String? = null
    ): String? {
        if (!explicitTitle.isNullOrBlank()) {
            return explicitTitle
        }
        val mockTitle =
            mockShows[showTmdbId]?.episodeTitles?.get(Pair(seasonNumber, episodeNumber))
        if (!mockTitle.isNullOrBlank()) {
            return mockTitle
        }
        return try {
            val res = tmdbApiService?.getTvSeason(showTmdbId, seasonNumber, emptyMap())
            if (res is ApiResponse.Success) {
                res.body.episodes?.find { it.episodeNumber == episodeNumber }?.title
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
