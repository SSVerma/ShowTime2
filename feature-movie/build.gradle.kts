import com.ssverma.AndroidConfig
import com.ssverma.Modules

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.ssverma.feature.movie"

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
    implementation(project(Modules.Common.ui))
    implementation(project(Modules.Shared.data))
    implementation(project(Modules.Core.uiPaging))
    implementation(project(Modules.Core.navigation))
    implementation(project(Modules.Shared.domain))
    implementation(project(Modules.Core.networking))
    implementation(project(Modules.Core.paging))
    implementation(project(Modules.Core.di))
    implementation(project(Modules.Core.image))
    implementation(project(Modules.ApiService.tmdb))

    api(project(Modules.Feature.movieNavigation))
    implementation(project(Modules.Feature.personNavigation))
    implementation(project(Modules.Feature.searchNavigation))

    implementation(project(Modules.Feature.filter))
    implementation(project(Modules.Feature.account))

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
}
