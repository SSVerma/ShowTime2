plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.shared.data"
}

dependencies {
    implementation(projects.sharedDomain)
}
