// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
    }
    dependencies {
        val navVersion = "2.9.6"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
    }
}

plugins {
    id("com.android.application") version "9.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.3.6" apply false
}