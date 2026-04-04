/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.lineageos.tv.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.lineageos.tv.launcher"
        minSdk = 31
        targetSdk = 34
        versionCode = 140001
        versionName = "14.0.1"
    }

     signingConfigs {
         create("platform") {
             storeFile = file("platform.jks")
             storePassword = "android"
             keyAlias = "platform"
             keyPassword = "android"
         }
     }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("platform")
        }
        debug {
            applicationIdSuffix = ".dev"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    compileOnly(files("../libs/android.jar", "../libs/SettingsLib.jar"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.leanback:leanback:1.2.0-alpha04")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.tvprovider:tvprovider:1.0.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("io.coil-kt:coil:2.6.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
}
