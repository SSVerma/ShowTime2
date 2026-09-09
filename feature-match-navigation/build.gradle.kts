plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.ssverma.feature.match.navigation"
}

dependencies {
    implementation(projects.coreNavigation)
}
