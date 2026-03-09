plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    implementation(libs.okhttpLoggingInterceptor)

    implementation(libs.coroutines.core)

    implementation(libs.dagger.hilt.core)
    kapt(libs.dagger.hilt.compiler)
}
