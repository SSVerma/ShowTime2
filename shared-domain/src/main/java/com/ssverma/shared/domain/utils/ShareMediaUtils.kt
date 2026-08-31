package com.ssverma.shared.domain.utils

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
}