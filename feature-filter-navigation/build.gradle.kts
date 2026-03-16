plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.feature.filter.navigation"
}

dependencies {
    implementation(projects.coreNavigation)
}
