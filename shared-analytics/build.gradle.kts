plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.shared.analytics"
}

dependencies {
    implementation(projects.sharedDomain)
    implementation(projects.coreAnalytics)
    implementation(projects.coreNetworking)
}
