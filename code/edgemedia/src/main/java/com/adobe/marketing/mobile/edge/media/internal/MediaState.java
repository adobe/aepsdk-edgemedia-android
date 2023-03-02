/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.edge.media.internal;

import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.MapUtils;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.Map;

/** Holds the Media configuration state variables. */
class MediaState {
    private static final String SOURCE_TAG = "MediaState";
    private final Object mutex = new Object();

    // Media Config
    private String mediaChannel;
    private String mediaPlayerName;
    private String mediaAppVersion;

    @Nullable public String getMediaChannel() {
        synchronized (mutex) {
            return mediaChannel;
        }
    }

    @Nullable public String getMediaPlayerName() {
        synchronized (mutex) {
            return mediaPlayerName;
        }
    }

    @Nullable public String getMediaAppVersion() {
        synchronized (mutex) {
            return mediaAppVersion;
        }
    }

    /**
     * Updates this state's configuration variables.
     *
     * @param data Map containing the Media configuration variables
     */
    public void updateState(final Map<String, Object> data) {
        if (MapUtils.isNullOrEmpty(data)) {
            Log.trace(
                    MediaInternalConstants.LOG_TAG,
                    SOURCE_TAG,
                    "updateState - Failed to extract configuration data (event data was nil).");
            return;
        }

        synchronized (mutex) {
            mediaChannel =
                    DataReader.optString(
                            data, MediaInternalConstants.Configuration.MEDIA_CHANNEL, null);
            mediaPlayerName =
                    DataReader.optString(
                            data, MediaInternalConstants.Configuration.MEDIA_PLAYER_NAME, null);
            mediaAppVersion =
                    DataReader.optString(
                            data, MediaInternalConstants.Configuration.MEDIA_APP_VERSION, null);
        }
    }

    /**
     * Checks the state variables to determine if this state is valid.
     *
     * @return true if this state has a valid configuration.
     */
    public boolean isValid() {
        synchronized (mutex) {
            return !(StringUtils.isNullOrEmpty(mediaChannel)
                    || StringUtils.isNullOrEmpty(mediaPlayerName));
        }
    }
}
