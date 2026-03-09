plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.common.ui"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.coreUi)
    implementation(projects.sharedUi)
    implementation(projects.sharedDomain)
    implementation(projects.sharedData)
    implementation(projects.coreImage)

    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.constraintlayout)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
