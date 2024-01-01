plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.danielgergely.komp.sample.flat"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.danielgergely.komp.sample.flat"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release")  {
            isMinifyEnabled = false
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    sourceSets.getByName("main") {
        assets.srcDir(rootProject.ext["resourcesDir"]!!.toString())
    }
}


dependencies {
    implementation(libs.androidx.appcompat)
    implementation(project(":app"))

    implementation(libs.komp.android.flat.utils)
}
