plugins {
    id("aep-library")
}

val mavenCoreVersion: String by project
val mavenEdgeVersion: String by project

aepLibrary {
    namespace = "com.adobe.marketing.mobile.edge.media"
    enableSpotless = true
    enableCheckStyle = true
    enableDokkaDoc = true
    publishing {
        gitRepoName = "aepsdk-edgemedia-android"
        addCoreDependency(mavenCoreVersion)
        addEdgeDependency(mavenEdgeVersion)
    }
}

dependencies {
    // Stop using snapshots after core, edge and edge identity release.
    implementation("com.adobe.marketing.mobile:core:$mavenCoreVersion-SNAPSHOT")
    implementation("com.adobe.marketing.mobile:edge:$mavenEdgeVersion-SNAPSHOT"){
        exclude(group = "com.adobe.marketing.mobile", module = "core")
    }

    androidTestImplementation("com.adobe.marketing.mobile:edge:$mavenEdgeVersion-SNAPSHOT")
    // Update it to 3.+ after release
    androidTestImplementation("com.adobe.marketing.mobile:edgeidentity:3.0.0-SNAPSHOT")
    androidTestImplementation("com.fasterxml.jackson.core:jackson-databind:2.12.7")
}

