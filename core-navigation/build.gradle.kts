plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.core.navigation"
}

dependencies {
    api(libs.compose.navigation)
    implementation(libs.hilt.navigation.compose)
}