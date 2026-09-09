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
    implementation(projects.coreImage)
    implementation(projects.coreDi)
    implementation(projects.coreBilling)
    implementation(projects.coreBackup)
    implementation(projects.sharedAds)

    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.constraintlayout)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.hilt.navigation.compose)

    testImplementation(libs.junit)
    testImplementation(projects.coreTesting)
    testImplementation(projects.sharedTesting)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
}
