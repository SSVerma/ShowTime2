plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.core.analytics"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.coreDi)
    api(libs.compose.ui)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}
