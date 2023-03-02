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

package com.adobe.marketing.mobile.media.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.EventSource;
import com.adobe.marketing.mobile.EventType;
import com.adobe.marketing.mobile.Extension;
import com.adobe.marketing.mobile.ExtensionApi;
import com.adobe.marketing.mobile.Media;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MediaExtension extends Extension {

    private static final String SOURCE_TAG = "MediaExtension";

    final Map<String, MediaTrackerInterface> trackers;
    MediaState mediaState;

    MediaExtension(final ExtensionApi extensionApi) {
        super(extensionApi);
        mediaState = new MediaState();
        trackers = new HashMap<>();
    }

    @NonNull @Override
    protected String getName() {
        return MediaInternalConstants.Media.SHARED_STATE_NAME;
    }

    @Nullable @Override
    protected String getFriendlyName() {
        return MediaInternalConstants.FRIENDLY_NAME;
    }

    @Nullable @Override
    protected String getVersion() {
        return Media.extensionVersion();
    }

    @Override
    protected void onRegistered() {

        getApi().registerEventListener(
                        EventType.GENERIC_IDENTITY,
                        EventSource.REQUEST_RESET,
                        this::handleResetIdentities);
        getApi().registerEventListener(
                        EventType.MEDIA,
                        MediaInternalConstants.Media.EVENT_SOURCE_TRACKER_REQUEST,
                        this::handleMediaTrackerRequestEvent);
        getApi().registerEventListener(
                        EventType.MEDIA,
                        MediaInternalConstants.Media.EVENT_SOURCE_TRACK_MEDIA,
                        this::handleMediaTrackEvent);
    }

    void handleMediaTrackerRequestEvent(@NonNull final Event event) {
        String trackerId =
                DataReader.optString(
                        event.getEventData(),
                        MediaInternalConstants.EventDataKeys.Tracker.ID,
                        null);
        if (StringUtils.isNullOrEmpty(trackerId)) {
            Log.debug(
                    MediaInternalConstants.LOG_TAG,
                    SOURCE_TAG,
                    "handleMediaTrackerRequestEvent - Public tracker ID is invalid, unable to create internal tracker.");
            return;
        }

        Map<String, Object> trackerConfig = DataReader.optTypedMap(Object.class,
                event.getEventData(),
                MediaInternalConstants.EventDataKeys.Tracker.EVENT_PARAM,
                Collections.<String, Object>emptyMap()
        );

        Log.debug(
                MediaInternalConstants.LOG_TAG,
                SOURCE_TAG,
                "handleMediaTrackerRequestEvent - Creating an internal tracker with tracker ID: %s.", trackerId);

        // TODO create MediaEventTracker
        //trackers.put(trackerId, new MediaEventTracker(mediaEventProcessor, trackerConfig));
    }

    void handleMediaTrackEvent(@NonNull final Event event) {
        String trackerId =
                DataReader.optString(
                        event.getEventData(),
                        MediaInternalConstants.EventDataKeys.Tracker.ID,
                        null);
        if (StringUtils.isNullOrEmpty(trackerId)) {
            Log.debug(
                    MediaInternalConstants.LOG_TAG,
                    SOURCE_TAG,
                    "handleMediaTrackEvent - Public tracker ID is invalid, unable to get internal tracker.");
            return;
        }

        MediaTrackerInterface tracker = trackers.get(trackerId);

        if (tracker == null) {
            Log.debug(
                    MediaInternalConstants.LOG_TAG,
                    SOURCE_TAG,
                    "handleMediaTrackEvent - Unable to find internal tracker for the given tracker ID: %s",
                    trackerId);
            return;
        }

        tracker.track(event);
    }

    void handleResetIdentities(@NonNull final Event event) {
        Log.debug(
                MediaInternalConstants.LOG_TAG,
                SOURCE_TAG,
                "handleResetIdentities - Clearing all tracking sessions.");

        trackers.clear();
    }
}
