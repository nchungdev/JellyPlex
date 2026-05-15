@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

import java.io.File
import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.cmp.runtime)
                implementation(libs.cmp.foundation)
                implementation(libs.cmp.material3)
                implementation(libs.cmp.ui)
                implementation(libs.cmp.components.resources)
                implementation(libs.cmp.components.uiToolingPreview)
                implementation(libs.cmp.materialIconsExtended)

                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.multiplatform.settings)
                implementation(libs.sqldelight.runtime)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor)

                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.activity.compose)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.ktor.network)
                implementation(libs.androidx.media3.exoplayer)
                implementation(libs.androidx.media3.exoplayer.hls)
                implementation(libs.androidx.media3.exoplayer.dash)
                implementation(libs.androidx.media3.ui)
                implementation(libs.androidx.media3.session)
                implementation(libs.androidx.security.crypto)
                implementation(libs.sqldelight.android.driver)
                implementation(libs.koin.android)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.network)
                implementation(libs.sqldelight.sqlite.driver)
            }
        }
    }
}

sqldelight {
    databases {
        create("JellyPlusDatabase") {
            packageName.set("org.jellyplus.client.data.db")
        }
    }
}

android {
    namespace = "org.jellyplus.client"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.jellyplus.client"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    val keystorePropsFile = rootProject.file("keystore.properties")
    val keystoreProps = Properties().apply {
        if (keystorePropsFile.exists()) load(keystorePropsFile.inputStream())
    }

    signingConfigs {
        create("release") {
            storeFile = (keystoreProps.getProperty("KEYSTORE_FILE")
                ?: System.getenv("KEYSTORE_FILE"))
                ?.let { file(it) }
            storePassword = keystoreProps.getProperty("KEYSTORE_PASSWORD")
                ?: System.getenv("KEYSTORE_PASSWORD")
            keyAlias = keystoreProps.getProperty("KEY_ALIAS")
                ?: System.getenv("KEY_ALIAS")
            keyPassword = keystoreProps.getProperty("KEY_PASSWORD")
                ?: System.getenv("KEY_PASSWORD")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            matchingFallbacks += listOf("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (keystorePropsFile.exists() || System.getenv("KEYSTORE_FILE") != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

fun registerAndroidRunTask(
    taskName: String,
    installTaskName: String,
    applicationId: String,
) {
    tasks.register(taskName) {
        group = "install"
        description = "Install and launch the $applicationId Android variant."
        dependsOn(installTaskName)

        doLast {
            val isWindows = System.getProperty("os.name")
                .lowercase()
                .contains("windows")
            val adbName = if (isWindows) "adb.exe" else "adb"
            val sdkDir = providers.environmentVariable("ANDROID_HOME")
                .orElse(providers.environmentVariable("ANDROID_SDK_ROOT"))
                .orNull
            val adb = sdkDir
                ?.let { File(it, "platform-tools/$adbName").absolutePath }
                ?: adbName

            val command = listOf(
                adb,
                "shell",
                "monkey",
                "-p",
                applicationId,
                "-c",
                "android.intent.category.LAUNCHER",
                "1",
            )
            val exitCode = ProcessBuilder(command)
                .inheritIO()
                .start()
                .waitFor()

            if (exitCode != 0) {
                throw GradleException("Failed to launch $applicationId with adb.")
            }
        }
    }
}

registerAndroidRunTask(
    taskName = "runDebug",
    installTaskName = "installDebug",
    applicationId = "org.jellyplus.client.debug",
)
registerAndroidRunTask(
    taskName = "runStaging",
    installTaskName = "installStaging",
    applicationId = "org.jellyplus.client.staging",
)
registerAndroidRunTask(
    taskName = "runRelease",
    installTaskName = "installRelease",
    applicationId = "org.jellyplus.client",
)
