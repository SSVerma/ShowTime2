plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.core.ads"
}

dependencies {
    implementation(projects.coreDi)
}
