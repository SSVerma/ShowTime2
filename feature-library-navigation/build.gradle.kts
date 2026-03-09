plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.feature.library.navigation"
}

dependencies {
    implementation(projects.coreNavigation)
}
