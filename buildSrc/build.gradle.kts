plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.13.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.0")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.56.2")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.0-2.0.2")
    implementation("com.google.gms:google-services:4.4.2")
}
