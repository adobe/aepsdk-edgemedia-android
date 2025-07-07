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

plugins {
    id("aep-library")
}

val mavenCoreVersion: String by project
val mavenEdgeVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.edge.media"
    enableSpotless = true
    enableSpotlessPrettierForJava = true
 
    publishing {
        gitRepoName = "aepsdk-edgemedia-android"
        addCoreDependency(mavenCoreVersion)
    }
}

dependencies {
    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion")
    implementation("com.adobe.marketing.mobile:edge:$mavenEdgeVersion")
    testImplementation("com.github.adobe:aepsdk-testutils-android:0bfafc7296")
    androidTestImplementation("com.github.adobe:aepsdk-testutils-android:0bfafc7296")
    
    // Adobe Core and Edge dependencies for tests
    testImplementation("com.adobe.marketing.mobile:core:$mavenCoreVersion")
    testImplementation("com.adobe.marketing.mobile:edge:$mavenEdgeVersion")
    
    // JUnit dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.0")
    
    // Mockito dependencies
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    
    // Kotlin reflection dependencies
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
}

