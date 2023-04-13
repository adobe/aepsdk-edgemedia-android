package com.adobe.media.testappkotlin

import android.app.Application
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.edge.media.Media

class MediaTestApp : Application() {
    // TODO: Set up the preferred Environment File ID from your mobile property configured in Data Collection UI
    private var ENVIRONMENT_FILE_ID: String = ""

    override fun onCreate() {
        super.onCreate()
        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        MobileCore.registerExtensions(listOf(Identity.EXTENSION, Edge.EXTENSION, Media.EXTENSION, Assurance.EXTENSION)) {
            MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID)
        }
    }
}
