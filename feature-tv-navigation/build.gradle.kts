plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.ssverma.feature.tv.navigation"
}

dependencies {
    implementation(projects.coreNavigation)
}
