plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    api(libs.retrofit)
    implementation(libs.retrofitConverterGson)
    api(libs.okhttpLoggingInterceptor)

    implementation(libs.coroutines.core)

    implementation(libs.dagger.hilt.core)
    ksp(libs.dagger.hilt.compiler)
}
