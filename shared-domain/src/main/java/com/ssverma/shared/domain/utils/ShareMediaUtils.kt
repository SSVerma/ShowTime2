package com.ssverma.shared.domain.utils

object ShareMediaUtils {
    private const val AppUrlPrefix = "https://play.google.com/store/apps/details?id="

    fun buildShareableMediaText(
        mediaTitle: String,
        mediaTagline: String?,
        mediaOverview: String,
        appPackageName: String
    ): String {
        val builder = StringBuilder()
            .append(mediaTitle)

        if (!mediaTagline.isNullOrBlank()) {
            builder.append("\n")
                .append(mediaTagline)
        }

        builder.append("\n\n")
            .append(mediaOverview)

        val appUrl = AppUrlPrefix + appPackageName

        builder.append("\n\n\n")
            .append(appUrl)

        return builder.toString()
    }

    fun buildShareableListText(
        listTitle: String,
        listDescription: String?,
        authorName: String,
        itemTitles: List<String>,
        appPackageName: String
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

        val appUrl = AppUrlPrefix + appPackageName
        builder.append("\nExplore & Clone in ShowTime:\n")
            .append(appUrl)

        return builder.toString()
    }
}