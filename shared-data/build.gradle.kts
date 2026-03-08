import com.ssverma.AndroidConfig
import com.ssverma.Modules

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.ssverma.shared.data"

    compileSdk = AndroidConfig.CompileSdk

    defaultConfig {
        minSdk = AndroidConfig.MinSdk
        targetSdk = AndroidConfig.TargetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(Modules.Shared.domain))
    implementation(project(Modules.Core.networking))
    implementation(project(Modules.Core.storage))
    implementation(project(Modules.ApiService.tmdb))

    implementation(libs.core.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.datastore.preferences)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
}
