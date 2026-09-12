plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.shared.ads"
}

dependencies {
    api(projects.coreAds)
    implementation(projects.coreUi)
    implementation(projects.coreImage)
    implementation(projects.sharedUi)
    implementation(projects.sharedDomain)
    implementation(projects.coreCcm)
    implementation(projects.coreStorage)
    implementation(projects.coreBilling)
    implementation(libs.datastore.preferences)

    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(projects.coreTesting)
    testImplementation(projects.sharedTesting)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
}
