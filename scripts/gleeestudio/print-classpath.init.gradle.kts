allprojects {
    afterEvaluate {
        if (project.name == "composeApp") {
            tasks.register("printJvmExportClasspath") {
                dependsOn("compileKotlinJvm", "jvmProcessResources")
                doLast {
                    val kotlinCompile = tasks.named("compileKotlinJvm").get()
                    val processRes = tasks.named("jvmProcessResources").get()
                    val runtimeConfig = configurations.findByName("jvmRuntimeClasspath")
                    val pathEntries = linkedSetOf<String>()
                    kotlinCompile.outputs.files.files.forEach { file ->
                        if (file.exists()) pathEntries.add(file.absolutePath)
                    }
                    processRes.outputs.files.files.forEach { file ->
                        if (file.exists()) pathEntries.add(file.absolutePath)
                    }
                    runtimeConfig?.files?.forEach { file ->
                        if (file.exists()) pathEntries.add(file.absolutePath)
                    }
                    println("CLASSPATH_START=" + pathEntries.joinToString(File.pathSeparator))
                }
            }
        }
    }
}
