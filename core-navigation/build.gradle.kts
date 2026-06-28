plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.ssverma.core.navigation"
}

dependencies {
    api(libs.compose.navigation)
    implementation(libs.hilt.navigation.compose)

    api(libs.androidx.navigation3.runtime)
    api(libs.androidx.navigation3.ui)
    api(libs.androidx.lifecycle.viewmodel.navigation3)
    api(libs.androidx.navigationevent.android)
    api(libs.androidx.navigationevent.compose)
    implementation(libs.kotlinx.serialization.json)
}
