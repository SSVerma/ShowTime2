package com.ssverma.feature.tv.navigation.args

import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.navigation.NavType
import com.ssverma.shared.domain.TvDiscoverConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Serializable
data class TvShowListingRoute(val args: TvShowListingArgs)

@Serializable
sealed interface TvShowListingArgs {
    @get:StringRes
    val titleRes: Int? get() = null
    val title: String? get() = null

    @Serializable
    data class TrendingToday(@get:StringRes override val titleRes: Int) : TvShowListingArgs

    @Serializable
    data class Popular(@get:StringRes override val titleRes: Int) : TvShowListingArgs

    @Serializable
    data class TopRated(@get:StringRes override val titleRes: Int) : TvShowListingArgs

    @Serializable
    data class NowAiring(@get:StringRes override val titleRes: Int) : TvShowListingArgs

    @Serializable
    data class TodayAiring(@get:StringRes override val titleRes: Int) : TvShowListingArgs

    @Serializable
    data class Upcoming(@get:StringRes override val titleRes: Int) : TvShowListingArgs

    @Serializable
    data class ByGenre(val genreId: Int, override val title: String) : TvShowListingArgs

    @Serializable
    data class ByKeyword(val keywordId: Int, override val title: String) : TvShowListingArgs

    @Serializable
    data class Discovery(
        val initialConfig: TvDiscoverConfig,
        @get:StringRes
        override val titleRes: Int? = null,
        override val title: String? = null
    ) : TvShowListingArgs

    companion object {
        val NavType = object : NavType<TvShowListingArgs>(isNullableAllowed = false) {
            override fun get(bundle: Bundle, key: String): TvShowListingArgs? {
                return bundle.getString(key)?.let { Json.decodeFromString(it) }
            }

            override fun parseValue(value: String): TvShowListingArgs {
                return Json.decodeFromString(Uri.decode(value))
            }

            override fun serializeAsValue(value: TvShowListingArgs): String {
                return Uri.encode(Json.encodeToString(value))
            }

            override fun put(bundle: Bundle, key: String, value: TvShowListingArgs) {
                bundle.putString(key, Json.encodeToString(value))
            }
        }

        val TypeMap = mapOf(
            typeOf<TvShowListingArgs>() to NavType
        )
    }
}
