plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.github.johnrengelman.shadow").version("7.1.0")
    id("java")
    id("application")
}

group = "com.danielgergely.komp"
version = "1.0-SNAPSHOT"

val frameMainClass = "com.danielgergely.komp.LwjglProgramKt"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(libs.org.jetbrains.kotlinx.coroutines.core)
    implementation(project(":app"))

    implementation(libs.komp.lwjgl.utils)
    implementation(libs.komp.flat.to.gl.adapter)

    val libraries = listOf(
        libs.org.lwjgl.lwjgl,
        libs.org.lwjgl.assimp,
        libs.org.lwjgl.glfw,
        libs.org.lwjgl.openal,
        libs.org.lwjgl.opengl,
        libs.org.lwjgl.stb,
    )

    val platformClassifiers = listOf(
        "natives-linux",
        "natives-windows",
        "natives-macos",
        "natives-macos-arm64"
    )

    libraries.forEach { library ->
        implementation(library)
        platformClassifiers.forEach { platformClassifier ->
            runtimeOnly(variantOf(library) { classifier(platformClassifier) })
        }
    }
}

val shadowJar: com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar by tasks

shadowJar.apply {
    manifest.attributes["Main-Class"] = frameMainClass
}

application {
    mainClassName = frameMainClass

    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
    }
}

java {
    sourceSets.getByName("main").resources.srcDir(rootProject.ext["resourcesDir"]!!.toString())
}
