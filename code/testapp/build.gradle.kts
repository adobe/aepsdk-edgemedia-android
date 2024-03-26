/*
 * Copyright 2024 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
import com.adobe.marketing.mobile.gradle.BuildConstants
plugins {
    id("com.android.application")
    id("com.diffplug.spotless")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    java {
        target("src/*/java/**/*.java")
        googleJavaFormat(BuildConstants.Versions.GOOGLE_JAVA_FORMAT).aosp().reflowLongStrings()
        importOrder()
        removeUnusedImports()
        endWithNewline()
        formatAnnotations()
        licenseHeader(BuildConstants.ADOBE_LICENSE_HEADER)
    }
}

android {
    namespace = "com.adobe.marketing.mobile.edge.media.testapp"

    defaultConfig {
        minSdk = BuildConstants.Versions.MIN_SDK_VERSION
        compileSdk = BuildConstants.Versions.COMPILE_SDK_VERSION
        targetSdk = BuildConstants.Versions.TARGET_SDK_VERSION

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName(BuildConstants.BuildTypes.RELEASE) {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = BuildConstants.Versions.JAVA_SOURCE_COMPATIBILITY
        targetCompatibility = BuildConstants.Versions.JAVA_TARGET_COMPATIBILITY
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation(project(":edgemedia"))
    implementation("com.adobe.marketing.mobile:core:3.0.0-SNAPSHOT")
    implementation("com.adobe.marketing.mobile:edge:3.0.0-SNAPSHOT") {
        exclude(group = "com.adobe.marketing.mobile", module = "core")
        exclude(group = "com.adobe.marketing.mobile", module = "edgeidentity")

    }
    implementation("com.adobe.marketing.mobile:edgeidentity:3.0.0-SNAPSHOT") {
        exclude(group = "com.adobe.marketing.mobile", module = "core")
    }
    implementation("com.adobe.marketing.mobile:assurance:3.0.0-SNAPSHOT") {
        exclude(group = "com.adobe.marketing.mobile", module = "core")
    }
}