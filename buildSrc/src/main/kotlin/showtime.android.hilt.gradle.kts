plugins {
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(libs.findLibrary("dagger-hilt-android").get())
    "kapt"(libs.findLibrary("dagger-hilt-compiler").get())
}
