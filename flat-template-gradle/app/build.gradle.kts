plugins {
    kotlin("multiplatform")
}

group = "com.danielgergely"

kotlin {
    /* Targets configuration omitted.
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

    linuxX64()
    mingwX64()

    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        macosX64()
        iosArm64()
        iosX64()
    }

    jvm()

    js {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.org.jetbrains.kotlinx.coroutines.core)

                implementation(libs.komp.komp.contract)
                implementation(libs.komp.komp.utils)
            }
        }
    }
}
