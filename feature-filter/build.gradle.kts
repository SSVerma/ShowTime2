import com.ssverma.Modules
import com.ssverma.AndroidConfig

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.ssverma.feature.filter"

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

    buildFeatures {
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(Modules.Core.ui))
    implementation(project(Modules.Shared.ui))
    implementation(project(Modules.Shared.data))
    implementation(project(Modules.Core.uiPaging))
    implementation(project(Modules.Core.navigation))
    implementation(project(Modules.Shared.domain))
    implementation(project(Modules.Core.networking))
    implementation(project(Modules.Core.paging))
    implementation(project(Modules.Core.di))
    implementation(project(Modules.ApiService.tmdb))
}
