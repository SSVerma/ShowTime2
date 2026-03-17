plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.core.analytics"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.coreDi)
    api(libs.compose.ui)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
}
