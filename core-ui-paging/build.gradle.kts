plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
}

android {
    namespace = "com.ssverma.core.ui.paging"
}

dependencies {
    implementation(projects.coreUi)

    api(libs.compose.paging)
}