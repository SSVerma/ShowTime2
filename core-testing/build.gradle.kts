plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.core.testing"
}

dependencies {
    api(projects.coreUi)
    api(projects.sharedDomain)
    api(projects.coreBilling)
    api(projects.coreAds)
    api(projects.coreNetworking)
    api(projects.coreCcm)

    api(libs.junit)
    api(libs.androidx.junit)
    api(libs.coroutines.test)
    api(libs.turbine)
    api(libs.truth)
    api(libs.mockk)

    api(libs.compose.ui.test.junit4)
    debugApi(libs.compose.ui.test.manifest)
}
