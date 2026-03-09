plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.shared.ui"
}

dependencies {
    implementation(projects.coreUi)
    implementation(projects.sharedDomain)
    implementation(projects.sharedData)
    implementation(projects.coreUiPaging)
    implementation(projects.coreImage)
    implementation(projects.coreDi)

    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.constraintlayout)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
