package com.ssverma.feature.movie.navigation.args

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.navigation.NavType
import com.ssverma.shared.domain.MovieDiscoverConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Serializable
data class MovieListingRoute(val args: MovieListingArgs)

@Serializable
sealed interface MovieListingArgs {
    @get:StringRes
    val titleRes: Int? get() = null
    val title: String? get() = null

    // Simple predefined lists
    @Serializable
    data class TrendingToday(override val titleRes: Int) : MovieListingArgs

    @Serializable
    data class Popular(override val titleRes: Int) : MovieListingArgs

    @Serializable
    data class TopRated(override val titleRes: Int) : MovieListingArgs

    @Serializable
    data class NowInCinemas(override val titleRes: Int) : MovieListingArgs

    @Serializable
    data class Upcoming(override val titleRes: Int) : MovieListingArgs

    @Serializable
    data class ByGenre(val genreId: Int, override val title: String) : MovieListingArgs

    @Serializable
    data class ByKeyword(val keywordId: Int, override val title: String) : MovieListingArgs

    // THE POWERHOUSE: Full Discovery
    @Serializable
    data class Discovery(
        val initialConfig: MovieDiscoverConfig,
        @get:StringRes
        override val titleRes: Int? = null,
        override val title: String? = null
    ) : MovieListingArgs

    companion object {
        val NavType = object : NavType<MovieListingArgs>(isNullableAllowed = false) {
            override fun get(bundle: Bundle, key: String): MovieListingArgs? {
                return bundle.getString(key)?.let { Json.decodeFromString(it) }
            }

            override fun parseValue(value: String): MovieListingArgs {
                return Json.decodeFromString(Uri.decode(value))
            }

            override fun serializeAsValue(value: MovieListingArgs): String {
                return Uri.encode(Json.encodeToString(value))
            }

            override fun put(bundle: Bundle, key: String, value: MovieListingArgs) {
                bundle.putString(key, Json.encodeToString(value))
            }
        }

        val TypeMap = mapOf(
            typeOf<MovieListingArgs>() to NavType
        )
    }
}
