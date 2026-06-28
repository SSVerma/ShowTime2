plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.core.ui"
}

dependencies {
    implementation(projects.coreImage)
    implementation(projects.sharedDomain)

    api(libs.compose.ui)
    api(libs.compose.foundation)
    api(libs.compose.material3)
    api(libs.compose.tooling)
    api(libs.compose.material.icons.core)
    api(libs.compose.material.icons.extended)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
