// Top-level build file where you can add configuration options common to all sub-projects/modules.
// This file only declares plugins — it does NOT add dependencies.
// Dependencies go in app/build.gradle.kts instead.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
