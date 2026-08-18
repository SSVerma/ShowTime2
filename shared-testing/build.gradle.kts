plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.shared.testing"
}

dependencies {
    api(projects.coreTesting)
    api(projects.sharedDomain)
    api(projects.sharedData)
    api(projects.sharedUi)
}
