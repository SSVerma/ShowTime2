import com.ssverma.Modules

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt)
    id("java-library")
}

dependencies {
    implementation(project(Modules.Core.networking))
    implementation(project(Modules.Core.paging))

    /* Dependency Injection */
    implementation(libs.dagger.hilt.core)
    kapt(libs.dagger.hilt.compiler)
}