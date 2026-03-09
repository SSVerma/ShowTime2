plugins {
    id("showtime.android.library")
    id("showtime.android.compose")
    id("showtime.android.hilt")
    id("org.jetbrains.kotlin.plugin.parcelize")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    "implementation"(project(":core-ui"))
    "implementation"(project(":shared-ui"))
    "implementation"(project(":common-ui"))
    "implementation"(project(":shared-data"))
    "implementation"(project(":core-ui-paging"))
    "implementation"(project(":core-navigation"))
    "implementation"(project(":shared-domain"))
    "implementation"(project(":core-networking"))
    "implementation"(project(":core-paging"))
    "implementation"(project(":core-di"))
    "implementation"(project(":core-image"))

    "implementation"(libs.findLibrary("hilt-navigation-compose").get())
}
