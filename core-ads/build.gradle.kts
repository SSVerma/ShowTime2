import java.util.Properties

plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.core.ads"

    buildFeatures {
        buildConfig = true
    }

    val releaseAdsProps by lazy {
        Properties().apply {
            val file = file("../release-ads.properties")
            if (file.canRead()) {
                file.inputStream().use { load(it) }
            }
        }
    }

    buildTypes {
        release {
            val appId = releaseAdsProps.getProperty("admobAppId", "")
            val bannerId = releaseAdsProps.getProperty("admobBannerId", "")
            val interstitialId = releaseAdsProps.getProperty("admobInterstitialId", "")

            manifestPlaceholders["admobAppId"] = appId
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$bannerId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$interstitialId\"")
        }

        debug {
            // Google Test App ID
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            val testBannerId = "ca-app-pub-3940256099942544/6300978111"
            val testInterstitialId = "ca-app-pub-3940256099942544/1033173712"

            buildConfigField("String", "ADMOB_BANNER_ID", "\"$testBannerId\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$testInterstitialId\"")
        }
    }
}

dependencies {
    implementation(projects.coreAnalytics)
    implementation(projects.coreDi)
    implementation(libs.compose.ui)

    implementation(libs.play.services.ads)
    implementation(libs.compose.activity)
}
