package com.ssverma.shared.domain.utils

import java.util.Locale

object ShareMediaUtils {
    const val DeepLinkDomain = "showtime.ssverma.in"
    const val UniversalWebBaseUrl = "https://$DeepLinkDomain"
    private const val PlayStoreUrlPrefix = "https://play.google.com/store/apps/details?id="

    fun buildMediaUrl(mediaType: String, mediaId: Int): String {
        return "$UniversalWebBaseUrl/$mediaType/$mediaId"
    }

    fun buildListUrl(listId: String): String {
        return "$UniversalWebBaseUrl/lists/$listId"
    }

    fun buildShareableMediaText(
        mediaTitle: String,
        mediaTagline: String?,
        mediaOverview: String,
        appPackageName: String,
        mediaType: String? = null,
        mediaId: Int? = null
    ): String {
        val builder = StringBuilder()
            .append(mediaTitle)

        if (!mediaTagline.isNullOrBlank()) {
            builder.append("\n")
                .append(mediaTagline)
        }

        builder.append("\n\n")
            .append(mediaOverview)

        val targetUrl = if (mediaType != null && mediaId != null) {
            buildMediaUrl(mediaType, mediaId)
        } else {
            PlayStoreUrlPrefix + appPackageName
        }

        builder.append("\n\n\n")
            .append(targetUrl)

        return builder.toString()
    }

    fun buildShareableListText(
        listTitle: String,
        listDescription: String?,
        authorName: String,
        itemTitles: List<String>,
        appPackageName: String,
        listId: String? = null
    ): String {
        val builder = StringBuilder()
            .append("🍿 Cinephile Collection: \"$listTitle\"")
            .append("\nCurated by $authorName • ${itemTitles.size} Titles\n")

        if (!listDescription.isNullOrBlank()) {
            builder.append("\n\"$listDescription\"\n")
        }

        if (itemTitles.isNotEmpty()) {
            builder.append("\nFeaturing:\n")
            itemTitles.take(5).forEachIndexed { index, title ->
                builder.append("${index + 1}. $title\n")
            }
            if (itemTitles.size > 5) {
                builder.append("... and ${itemTitles.size - 5} more!\n")
            }
        }

        val shareUrl = if (!listId.isNullOrBlank()) {
            buildListUrl(listId)
        } else {
            PlayStoreUrlPrefix + appPackageName
        }

        builder.append("\nExplore & Clone in ShowTime:\n")
            .append(shareUrl)

        return builder.toString()
    }

    fun buildSecretListUrl(shareCode: String): String {
        return "$UniversalWebBaseUrl/l/$shareCode"
    }

    fun buildFormattedSecretListMarkdown(
        title: String,
        description: String?,
        authorName: String,
        shareCode: String,
        itemTitlesWithRating: List<Pair<String, Float>>
    ): String {
        val builder = StringBuilder()
            .append("🎬 Cinephile Collection: \"$title\"\n")
            .append("Curated by $authorName • ${itemTitlesWithRating.size} Titles\n")

        if (!description.isNullOrBlank()) {
            builder.append("\n\"$description\"\n")
        }

        if (itemTitlesWithRating.isNotEmpty()) {
            builder.append("\nTitles:\n")
            itemTitlesWithRating.forEachIndexed { index, pair ->
                val ratingStr = if (pair.second > 0f) " — ${
                    String.format(
                        Locale.US,
                        "%.1f",
                        pair.second
                    )
                } ★" else ""
                builder.append("${index + 1}. ${pair.first}$ratingStr\n")
            }
        }

        builder.append("\nView, Clone or Co-Curate in ShowTime:\n")
            .append(buildSecretListUrl(shareCode))

        return builder.toString()
    }

    fun normalizeSecretShareCode(rawCode: String): String {
        var cleaned = rawCode.trim()
        if (cleaned.contains("/l/")) {
            cleaned = cleaned.substringAfter("/l/")
        } else if (cleaned.contains("/list/")) {
            cleaned = cleaned.substringAfter("/list/")
        } else if (cleaned.contains("/secret_list/")) {
            cleaned = cleaned.substringAfter("/secret_list/")
        } else if (cleaned.contains("/shared_list/")) {
            cleaned = cleaned.substringAfter("/shared_list/")
        }
        cleaned = cleaned.substringBefore("?").substringBefore("/").substringBefore("#").trim()
            .uppercase()
            .replace(" ", "").replace("-", "")
        if (cleaned.isBlank()) return ""
        return if (cleaned.startsWith("SL")) {
            val suffix = cleaned.removePrefix("SL")
            if (suffix.isBlank()) "SL" else "SL-$suffix"
        } else {
            "SL-$cleaned"
        }
    }
}