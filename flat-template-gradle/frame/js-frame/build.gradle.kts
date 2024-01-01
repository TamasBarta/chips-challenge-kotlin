plugins {
    id("org.jetbrains.kotlin.js")
}

group = "com.danielgergely.komp"
version = "1.0-SNAPSHOT"

kotlin {
    js(IR) {
        binaries.executable()
        browser {

        }
    }
}

dependencies {
    implementation(libs.org.jetbrains.kotlinx.coroutines.core)
    implementation(project(":app"))

    implementation(libs.komp.js.utils)
    implementation(libs.komp.flat.to.gl.adapter)
}

tasks.register<Copy>("copyToProcessedResources") {
    from(rootProject.ext["resourcesDir"]!!.toString())
    into(File(File(File(File(project.buildDir, "processedResources"), "js"), "main"), "resources").apply { mkdirs() })
}

tasks.getByName("browserDevelopmentRun").dependsOn("copyToProcessedResources")
tasks.getByName("browserProductionRun").dependsOn("copyToProcessedResources")
