plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.sqldelight) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }

    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:2.1.0")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.0")
            force("org.jetbrains.kotlin:kotlin-reflect:2.1.0")
        }
    }
}
