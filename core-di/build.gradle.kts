plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
}

android {
    namespace = "com.ssverma.core.di"
}

dependencies {
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
}
