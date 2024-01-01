buildscript {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.com.android.tools.build.gradle)
        classpath(libs.org.jetbrains.kotlin.gradle.plugin)
    }
}

rootProject.ext["resourcesDir"] = rootProject.file("resources").absolutePath

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.danielgergely.com/snapshots")
        maven("https://maven.danielgergely.com/releases")
        google()
    }
}
