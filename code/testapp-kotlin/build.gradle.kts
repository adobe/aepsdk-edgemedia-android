import com.adobe.marketing.mobile.gradle.BuildConstants

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.diffplug.spotless")
}


android {
    namespace = "com.adobe.marketing.mobile.edge.media.testapp.kotlin"

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

    kotlinOptions {
        jvmTarget = BuildConstants.Versions.KOTLIN_JVM_TARGET
        languageVersion = BuildConstants.Versions.KOTLIN_LANGUAGE_VERSION
        apiVersion = BuildConstants.Versions.KOTLIN_API_VERSION
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("com.google.android.material:material:1.8.0")

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