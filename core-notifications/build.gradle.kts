plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.core.notifications"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.coreDi)
    api(libs.compose.ui)
    implementation(libs.core.ktx)
    implementation(libs.coil)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Coroutines
    implementation(libs.coroutines.core)
}
