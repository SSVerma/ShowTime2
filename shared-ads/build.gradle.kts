plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.shared.ads"
}

dependencies {
    api(projects.coreAds)
    implementation(projects.coreUi)
    implementation(projects.coreImage)
    implementation(projects.sharedUi)
    implementation(projects.sharedDomain)

    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
}
