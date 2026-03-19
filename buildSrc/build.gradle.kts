plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev") }
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap") }
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:9.0.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.3.20")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.20")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.59.2")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.6")
    implementation("com.android.legacy-kapt:com.android.legacy-kapt.gradle.plugin:9.0.0")
    implementation("com.google.gms:google-services:4.4.2")
}
