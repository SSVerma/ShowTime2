plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.feature.search.navigation"
}

dependencies {
    implementation(projects.coreNavigation)
}
