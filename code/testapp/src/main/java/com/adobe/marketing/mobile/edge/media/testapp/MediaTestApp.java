/*
  Copyright 2021 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.media.testapp;

import android.app.Application;
import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Assurance;
import com.adobe.marketing.mobile.Edge;
import com.adobe.marketing.mobile.LoggingMode;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.edge.identity.Identity;
import com.adobe.marketing.mobile.edge.media.Media;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MediaTestApp extends Application {
    // Replace with your environment file ID from Data Collection UI
    private static final String LAUNCH_ENVIRONMENT_FILE_ID = "PR6ba9115b61c84e93b18edbdf784aa9c4";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set the application and logging mode
        MobileCore.setApplication(this);
        MobileCore.setLogLevel(LoggingMode.VERBOSE);

        // Configure with environment file ID
        MobileCore.configureWithAppID(LAUNCH_ENVIRONMENT_FILE_ID);

        // Set media configuration
        Map<String, Object> mediaConfig = new HashMap<>();
        mediaConfig.put("edgeMedia.channel", "android-v5-testapp");
        mediaConfig.put("edgeMedia.playerName", "android-sample-player");
        mediaConfig.put("edgeMedia.appVersion", "v3.0.0");
        MobileCore.updateConfiguration(mediaConfig);

        // Register extensions in the correct order:
        // 1. Identity (required by Edge)
        // 2. Edge (required by Media)
        // 3. Media
        // 4. Assurance (optional, for debugging)
        MobileCore.registerExtensions(
                Arrays.asList(Identity.EXTENSION, Edge.EXTENSION, Media.EXTENSION, Assurance.EXTENSION),
                o -> {
                    // Extensions are now registered
                });
    }
}
