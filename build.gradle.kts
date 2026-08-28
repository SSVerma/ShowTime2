// Plugins are managed by buildSrc convention plugins and module-level blocks.

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

tasks.register<Exec>("installGitHooks") {
    description = "Installs and configures Git pre-commit hooks for code quality and reformatting."
    group = "verification"
    commandLine("git", "config", "core.hooksPath", ".githooks")
    doLast {
        println("[ShowTime] Git hooks path configured to .githooks successfully.")
    }
}

tasks.register<Exec>("verifyCodeQuality") {
    description = "Audits codebase against docs/CODE_QUALITY_AND_SECURITY_GUIDE.md."
    group = "verification"
    commandLine("./scripts/verify-code-quality.sh")
}
