// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}
buildscript {
    repositories {
        // ...
        google() // Google's Maven repository
    }
    dependencies {
        // ...
        classpath ("com.google.gms:google-services:4.3.10")// Add this line
    }
}