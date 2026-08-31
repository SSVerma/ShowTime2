plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.ssverma.feature.payment.navigation"
}

dependencies {
    implementation(projects.coreNavigation)
}
