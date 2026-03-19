// Plugins are managed by buildSrc convention plugins and module-level blocks.

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
