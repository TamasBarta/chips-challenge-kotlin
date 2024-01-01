import CreateXcAssets.FileAction.DATA
import CreateXcAssets.FileAction.IMAGE
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
plugins {
    kotlin("multiplatform")
    alias(libs.plugins.com.danielgergely.createxcassets)
}

group = "com.danielgergely.kgl"
version = "1.0-SNAPSHOT"
val frameworkName = "KompIos"
val bundleId = "$group.$frameworkName"

kotlin {
    val xcFramework = XCFramework(frameworkName)

    ios {
        binaries {
            framework {
                baseName = frameworkName
                binaryOption("bundleId", bundleId)
                xcFramework.add(this)
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.org.jetbrains.kotlinx.coroutines.core)
            }
        }
        val iosArm64Main by getting { }
        val iosX64Main by getting { }
        val iosMain by getting {
            iosArm64Main.dependsOn(this)
            iosX64Main.dependsOn(this)

            dependencies {
                implementation(libs.komp.ios.metal.utils)

                api(project(":app"))
            }
        }
    }
}

tasks.register<CreateXcAssets>("createXcAssets") {
    tasks
        .filter { it.name.startsWith("assemble") && it.name.endsWith("XCFramework") }
        .forEach {
            this.dependsOn(it)
        }

    resourcesDirectory = File(project.rootDir, "resources")
    fileFilter { file ->
        when {
            file.extension.equals("png", true) -> IMAGE
            else -> DATA
        }
    }
    assetName {
        relativePath
            .replace("_", "_u")
            .replace("/", "_s")
    }

    doLast {
        File(project.buildDir, "XCFrameworks")
            .walkTopDown()
            .asIterable()
            .filter { it.isDirectory && it.name.endsWith(".framework") }
            .forEach { frameworkDir ->
                carFile.copyTo(File(frameworkDir, carFile.name), overwrite = true)
            }
    }
}.apply {
    tasks.getByName("build").dependsOn(get())
}
