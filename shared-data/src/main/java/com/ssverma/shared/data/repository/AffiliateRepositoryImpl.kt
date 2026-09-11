package com.ssverma.shared.data.repository

import com.ssverma.core.ccm.AppConfigProvider
import com.ssverma.shared.domain.model.AffiliateConfig
import com.ssverma.shared.domain.repository.AffiliateRepository
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AffiliateRepositoryImpl @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) : AffiliateRepository {

    companion object {
        private const val KEY_AFFILIATE_CONFIG = "affiliate_config"

        // Known Watch Provider IDs from TMDB
        const val PROVIDER_APPLE_TV = 2
        const val PROVIDER_APPLE_TV_PLUS = 350
        const val PROVIDER_AMAZON_PRIME = 9
        const val PROVIDER_AMAZON_VIDEO = 10
        const val PROVIDER_AMAZON_PRIME_ALT = 119
        const val PROVIDER_NETFLIX = 8
        const val PROVIDER_NETFLIX_ADS = 1796
        const val PROVIDER_DISNEY_PLUS = 337
        const val PROVIDER_HOTSTAR = 122
        const val PROVIDER_HULU = 15
        const val PROVIDER_MAX = 1899
        const val PROVIDER_HBO_MAX = 384
        const val PROVIDER_PARAMOUNT_PLUS = 531
        const val PROVIDER_PEACOCK = 386
        const val PROVIDER_PEACOCK_PREMIUM = 387
        const val PROVIDER_GOOGLE_PLAY = 3
        const val PROVIDER_YOUTUBE = 192
        const val PROVIDER_ZEE5 = 232

        // Known Android Package Names
        private const val PKG_NETFLIX = "com.netflix.mediaclient"
        private const val PKG_AMAZON_PRIME = "com.amazon.avod.thirdpartyclient"
        private const val PKG_APPLE_TV = "com.apple.atve.androidtv.appletv"
        private const val PKG_DISNEY_PLUS = "com.disney.disneyplus"
        private const val PKG_HOTSTAR = "in.startv.hotstar"
        private const val PKG_HULU = "com.hulu.plus"
        private const val PKG_MAX = "com.wbd.stream"
        private const val PKG_PARAMOUNT_PLUS = "com.cbs.app"
        private const val PKG_PEACOCK = "com.peacocktv.peacockandroid"
        private const val PKG_YOUTUBE = "com.google.android.youtube"
        private const val PKG_GOOGLE_PLAY = "com.google.android.videos"
        private const val PKG_ZEE5 = "com.graymatrix.did"

        private const val DEFAULT_JUSTWATCH_URL = "https://www.justwatch.com"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override fun getAffiliateConfig(): AffiliateConfig {
        val configStr = appConfigProvider.getString(KEY_AFFILIATE_CONFIG)
        if (configStr.isBlank()) return AffiliateConfig()

        return try {
            json.decodeFromString<AffiliateConfig>(configStr)
        } catch (e: Exception) {
            AffiliateConfig()
        }
    }

    override fun buildAffiliateUrl(rawUrl: String, providerId: Int, region: String): String {
        val config = getAffiliateConfig()

        return try {
            when (providerId) {
                PROVIDER_APPLE_TV, PROVIDER_APPLE_TV_PLUS -> {
                    if (config.appleTv.enabled && config.appleTv.partnerToken.isNotBlank()) {
                        val delimiter = if (rawUrl.contains("?")) "&" else "?"
                        val sb = StringBuilder(rawUrl).append(delimiter).append("at=")
                            .append(config.appleTv.partnerToken)
                        if (config.appleTv.campaignId.isNotBlank()) {
                            sb.append("&ct=").append(config.appleTv.campaignId)
                        }
                        sb.toString()
                    } else rawUrl
                }

                PROVIDER_AMAZON_PRIME, PROVIDER_AMAZON_VIDEO, PROVIDER_AMAZON_PRIME_ALT -> {
                    if (config.amazonPrime.enabled) {
                        val tag = config.amazonPrime.regionalTags[region.uppercase()]
                            ?: config.amazonPrime.tagDefault

                        if (tag.isNotBlank()) {
                            val delimiter = if (rawUrl.contains("?")) "&" else "?"
                            "$rawUrl${delimiter}tag=$tag"
                        } else rawUrl
                    } else rawUrl
                }

                else -> rawUrl
            }
        } catch (e: Exception) {
            rawUrl
        }
    }

    override fun buildProviderWatchUrl(
        providerId: Int,
        mediaTitle: String,
        fallbackLink: String?,
        region: String
    ): String {
        val encodedTitle = try {
            URLEncoder.encode(mediaTitle.trim(), StandardCharsets.UTF_8.name())
        } catch (e: Exception) {
            mediaTitle.trim().replace(" ", "+")
        }

        val config = getAffiliateConfig()

        return when (providerId) {
            PROVIDER_AMAZON_PRIME, PROVIDER_AMAZON_VIDEO, PROVIDER_AMAZON_PRIME_ALT -> {
                if (config.amazonPrime.enabled) {
                    val tag = config.amazonPrime.regionalTags[region.uppercase()]
                        ?: config.amazonPrime.tagDefault
                    val baseUrl = "https://www.amazon.com/s?k=$encodedTitle&i=instant-video"
                    if (tag.isNotBlank()) "$baseUrl&tag=$tag" else baseUrl
                } else {
                    "https://www.primevideo.com/search/ref=atv_nb_sr?phrase=$encodedTitle"
                }
            }

            PROVIDER_APPLE_TV, PROVIDER_APPLE_TV_PLUS -> {
                val baseUrl = "https://tv.apple.com/search?term=$encodedTitle"
                if (config.appleTv.enabled && config.appleTv.partnerToken.isNotBlank()) {
                    val withToken = "$baseUrl&at=${config.appleTv.partnerToken}"
                    if (config.appleTv.campaignId.isNotBlank()) {
                        "$withToken&ct=${config.appleTv.campaignId}"
                    } else withToken
                } else {
                    baseUrl
                }
            }

            PROVIDER_NETFLIX, PROVIDER_NETFLIX_ADS -> {
                "https://www.netflix.com/search?q=$encodedTitle"
            }

            PROVIDER_DISNEY_PLUS -> {
                "https://www.disneyplus.com/search?q=$encodedTitle"
            }

            PROVIDER_HOTSTAR -> {
                "https://www.hotstar.com/search?q=$encodedTitle"
            }

            PROVIDER_HULU -> {
                "https://www.hulu.com/search?q=$encodedTitle"
            }

            PROVIDER_MAX, PROVIDER_HBO_MAX -> {
                "https://www.max.com/search?q=$encodedTitle"
            }

            PROVIDER_PARAMOUNT_PLUS -> {
                "https://www.paramountplus.com/search/?q=$encodedTitle"
            }

            PROVIDER_PEACOCK, PROVIDER_PEACOCK_PREMIUM -> {
                "https://www.peacocktv.com"
            }

            PROVIDER_GOOGLE_PLAY, PROVIDER_YOUTUBE -> {
                "https://www.youtube.com/results?search_query=$encodedTitle"
            }

            else -> {
                if (!fallbackLink.isNullOrBlank()) {
                    buildAffiliateUrl(fallbackLink, providerId, region)
                } else {
                    buildJustWatchUrl()
                }
            }
        }
    }

    override fun buildProviderHubUrl(providerId: Int, region: String): String {
        val config = getAffiliateConfig()

        return when (providerId) {
            PROVIDER_AMAZON_PRIME, PROVIDER_AMAZON_VIDEO, PROVIDER_AMAZON_PRIME_ALT -> {
                val base = "https://www.primevideo.com"
                if (config.amazonPrime.enabled) {
                    val tag = config.amazonPrime.regionalTags[region.uppercase()]
                        ?: config.amazonPrime.tagDefault
                    if (tag.isNotBlank()) "$base?tag=$tag" else base
                } else base
            }

            PROVIDER_APPLE_TV, PROVIDER_APPLE_TV_PLUS -> {
                val base = "https://tv.apple.com"
                if (config.appleTv.enabled && config.appleTv.partnerToken.isNotBlank()) {
                    val withToken = "$base?at=${config.appleTv.partnerToken}"
                    if (config.appleTv.campaignId.isNotBlank()) {
                        "$withToken&ct=${config.appleTv.campaignId}"
                    } else withToken
                } else base
            }

            PROVIDER_NETFLIX, PROVIDER_NETFLIX_ADS -> "https://www.netflix.com"
            PROVIDER_DISNEY_PLUS -> "https://www.disneyplus.com"
            PROVIDER_HOTSTAR -> "https://www.hotstar.com"
            PROVIDER_HULU -> "https://www.hulu.com"
            PROVIDER_MAX, PROVIDER_HBO_MAX -> "https://www.max.com"
            PROVIDER_PARAMOUNT_PLUS -> "https://www.paramountplus.com"
            PROVIDER_PEACOCK, PROVIDER_PEACOCK_PREMIUM -> "https://www.peacocktv.com"
            PROVIDER_GOOGLE_PLAY, PROVIDER_YOUTUBE -> "https://www.youtube.com"
            PROVIDER_ZEE5 -> "https://www.zee5.com"
            else -> buildJustWatchUrl()
        }
    }

    override fun getProviderPackageName(providerId: Int): String? {
        return when (providerId) {
            PROVIDER_NETFLIX, PROVIDER_NETFLIX_ADS -> PKG_NETFLIX
            PROVIDER_AMAZON_PRIME, PROVIDER_AMAZON_VIDEO, PROVIDER_AMAZON_PRIME_ALT -> PKG_AMAZON_PRIME
            PROVIDER_APPLE_TV, PROVIDER_APPLE_TV_PLUS -> PKG_APPLE_TV
            PROVIDER_DISNEY_PLUS -> PKG_DISNEY_PLUS
            PROVIDER_HOTSTAR -> PKG_HOTSTAR
            PROVIDER_HULU -> PKG_HULU
            PROVIDER_MAX, PROVIDER_HBO_MAX -> PKG_MAX
            PROVIDER_PARAMOUNT_PLUS -> PKG_PARAMOUNT_PLUS
            PROVIDER_PEACOCK, PROVIDER_PEACOCK_PREMIUM -> PKG_PEACOCK
            PROVIDER_GOOGLE_PLAY -> PKG_GOOGLE_PLAY
            PROVIDER_YOUTUBE -> PKG_YOUTUBE
            PROVIDER_ZEE5 -> PKG_ZEE5
            else -> null
        }
    }

    override fun buildJustWatchUrl(rawUrl: String?): String {
        val target = if (rawUrl.isNullOrBlank()) DEFAULT_JUSTWATCH_URL else rawUrl
        val config = getAffiliateConfig()

        if (!config.justWatch.enabled || config.justWatch.partnerToken.isBlank()) {
            return target
        }

        return try {
            val delimiter = if (target.contains("?")) "&" else "?"
            val sb = StringBuilder(target).append(delimiter).append("partner=")
                .append(config.justWatch.partnerToken)
            if (config.justWatch.campaignId.isNotBlank()) {
                sb.append("&campaign=").append(config.justWatch.campaignId)
            }
            sb.toString()
        } catch (e: Exception) {
            target
        }
    }
}

