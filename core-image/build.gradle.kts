plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.core.image"
}

dependencies {
    api(libs.coil.compose)
    implementation(libs.compose.material3)
}
