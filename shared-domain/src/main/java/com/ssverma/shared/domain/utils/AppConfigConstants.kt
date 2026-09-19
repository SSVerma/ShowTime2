package com.ssverma.shared.domain.utils

object AppConfigConstants {
    const val APP_PACKAGE_NAME = "com.ssverma.showtime"
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=$APP_PACKAGE_NAME"
    const val GITHUB_REPO_URL = "https://github.com/SSVerma/ShowTime2"
    const val PRIVACY_POLICY_URL = "https://showtime.ssverma.in/privacy"
    const val PRIVACY_POLICY_FALLBACK_URL =
        "https://github.com/SSVerma/ShowTime2/blob/main/PRIVACY_POLICY.md"
    const val TERMS_OF_SERVICE_URL =
        "https://github.com/SSVerma/ShowTime2/blob/main/TERMS_OF_SERVICE.md"
    const val TERMS_OF_SERVICE_FALLBACK_URL =
        "https://github.com/SSVerma/ShowTime2/blob/main/TERMS_OF_SERVICE.md"
    const val CONTACT_EMAIL = "ssvermahmh@gmail.com"
    const val DEVELOPER_TWITTER_URL = "https://x.com/ssverma1916"
    const val DEVELOPER_AVATAR_URL =
        "https://pbs.twimg.com/profile_images/1807349302164934656/xELoSQEH_400x400.jpg"
    const val PLAY_STORE_MARKET_URI = "market://details?id=$APP_PACKAGE_NAME"

    // Remote Config Keys: App Update
    const val KEY_MIN_SUPPORTED_VERSION_CODE = "min_supported_version_code"
    const val KEY_LATEST_VERSION_CODE = "latest_version_code"
    const val KEY_UPDATE_TITLE = "update_title"
    const val KEY_UPDATE_MESSAGE = "update_message"
}
