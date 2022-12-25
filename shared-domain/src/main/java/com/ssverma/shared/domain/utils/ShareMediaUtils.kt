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
}