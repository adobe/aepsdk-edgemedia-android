/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

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
    private val ENVIRONMENT_FILE_ID: String = "94f571f308d5/e0549f176247/launch-dcba224e4bc5-development"

    override fun onCreate() {
        super.onCreate()
        MobileCore.setApplication(this)
        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        MobileCore.updateConfiguration(
            mapOf(
                "edge.configId" to "05d4a30a-f0b5-4452-b7a0-3bafefd691c0",
                "__dev__edge.configId" to "05d4a30a-f0b5-4452-b7a0-3bafefd691c0",
                "experienceCloud.org" to "6D9FE18C5536A5E90A4C98A6@AdobeOrg",
                "edge.domain" to "edge.adobedc.net"
            )
        )

        MobileCore.registerExtensions(listOf(Identity.EXTENSION, Edge.EXTENSION, Media.EXTENSION, Assurance.EXTENSION)) {
            MobileCore.configureWithAppID(ENVIRONMENT_FILE_ID)
        }
    }
}
