import com.ssverma.AndroidConfig
import com.ssverma.Modules

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.ssverma.feature.account"

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
    implementation(project(Modules.Core.image))
    implementation(project(Modules.Core.storage))
    implementation(project(Modules.ApiService.tmdb))
    implementation(project(Modules.Feature.auth))
    api(project(Modules.Feature.accountNavigation))

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)

    implementation(libs.hilt.navigation.compose)
}

protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
