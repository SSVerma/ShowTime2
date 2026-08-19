package com.ssverma.feature.movie.navigation.args

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.navigation.NavType
import androidx.navigation3.runtime.NavKey
import com.ssverma.shared.domain.MovieDiscoverConfig
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Serializable
@Parcelize
data class MovieListingRoute(val args: MovieListingArgs) : NavKey, Parcelable

@Serializable
sealed interface MovieListingArgs : Parcelable {
    @get:StringRes
    val titleRes: Int? get() = null
    val title: String? get() = null

    // Simple predefined lists
    @Serializable
    @Parcelize
    data class TrendingToday(override val titleRes: Int) : MovieListingArgs

    @Serializable
    @Parcelize
    data class Popular(override val titleRes: Int) : MovieListingArgs

    @Serializable
    @Parcelize
    data class TopRated(override val titleRes: Int) : MovieListingArgs

    @Serializable
    @Parcelize
    data class NowInCinemas(override val titleRes: Int) : MovieListingArgs

    @Serializable
    @Parcelize
    data class Upcoming(override val titleRes: Int) : MovieListingArgs

    @Serializable
    @Parcelize
    data class ByGenre(val genreId: Int, override val title: String) : MovieListingArgs

    @Serializable
    @Parcelize
    data class ByKeyword(val keywordId: Int, override val title: String) : MovieListingArgs

    // THE POWERHOUSE: Full Discovery
    @Serializable
    @Parcelize
    data class Discovery(
        val initialConfig: @RawValue MovieDiscoverConfig,
        @get:StringRes
        override val titleRes: Int? = null,
        override val title: String? = null
    ) : MovieListingArgs

    companion object {
        val NavType = object : NavType<MovieListingArgs>(isNullableAllowed = false) {
            override fun get(bundle: Bundle, key: String): MovieListingArgs? {
                return bundle.getParcelable(key)
            }

            override fun parseValue(value: String): MovieListingArgs {
                return Json.decodeFromString(Uri.decode(value))
            }

            override fun serializeAsValue(value: MovieListingArgs): String {
                return Uri.encode(Json.encodeToString(value))
            }

            override fun put(bundle: Bundle, key: String, value: MovieListingArgs) {
                bundle.putParcelable(key, value)
            }
        }

        val TypeMap = mapOf(
            typeOf<MovieListingArgs>() to NavType
        )
    }
}
