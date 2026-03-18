plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.core.ccm"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.coreDi)
    api(libs.compose.ui)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.remote.config)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
}
