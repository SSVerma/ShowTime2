plugins {
    id("showtime.android.library")
    id("showtime.android.hilt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.ssverma.core.storage"
}

dependencies {
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore.preferences)
}
