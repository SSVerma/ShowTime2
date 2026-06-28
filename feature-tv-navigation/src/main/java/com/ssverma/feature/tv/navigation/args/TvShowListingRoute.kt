package com.ssverma.feature.tv.navigation.args

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.navigation.NavType
import com.ssverma.shared.domain.TvDiscoverConfig
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.typeOf

@Serializable
@Parcelize
data class TvShowListingRoute(val args: TvShowListingArgs) : NavKey, Parcelable

@Serializable
sealed interface TvShowListingArgs : Parcelable {
    @get:StringRes
    val titleRes: Int? get() = null
    val title: String? get() = null

    // Simple predefined lists
    @Serializable
    @Parcelize
    data class TrendingToday(override val titleRes: Int) : TvShowListingArgs

    @Serializable
    @Parcelize
    data class Popular(override val titleRes: Int) : TvShowListingArgs

    @Serializable
    @Parcelize
    data class TopRated(override val titleRes: Int) : TvShowListingArgs

    @Serializable
    @Parcelize
    data class AiringToday(override val titleRes: Int) : TvShowListingArgs

    @Serializable
    @Parcelize
    data class OnTheAir(override val titleRes: Int) : TvShowListingArgs

    @Serializable
    @Parcelize
    data class Upcoming(override val titleRes: Int) : TvShowListingArgs

    @Serializable
    @Parcelize
    data class ByGenre(val genreId: Int, override val title: String) : TvShowListingArgs

    @Serializable
    @Parcelize
    data class ByKeyword(val keywordId: Int, override val title: String) : TvShowListingArgs

    // THE POWERHOUSE: Full Discovery
    @Serializable
    @Parcelize
    data class Discovery(
        val initialConfig: @RawValue TvDiscoverConfig,
        @get:StringRes
        override val titleRes: Int? = null,
        override val title: String? = null
    ) : TvShowListingArgs

    companion object {
        val NavType = object : NavType<TvShowListingArgs>(isNullableAllowed = false) {
            override fun get(bundle: Bundle, key: String): TvShowListingArgs? {
                return bundle.getParcelable(key)
            }

            override fun parseValue(value: String): TvShowListingArgs {
                return Json.decodeFromString(Uri.decode(value))
            }

            override fun serializeAsValue(value: TvShowListingArgs): String {
                return Uri.encode(Json.encodeToString(value))
            }

            override fun put(bundle: Bundle, key: String, value: TvShowListingArgs) {
                bundle.putParcelable(key, value)
            }
        }

        val TypeMap = mapOf(
            typeOf<TvShowListingArgs>() to NavType
        )
    }
}
