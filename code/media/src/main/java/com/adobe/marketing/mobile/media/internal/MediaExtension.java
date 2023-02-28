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
import java.util.HashMap;
import java.util.Map;

public class MediaExtension extends Extension {

    private static final String LOG_TAG = "MediaExtension";

    final Map<String, MediaTrackerInterface> trackers;
    MediaState mediaState;
    MediaRealTimeService mediaRealTimeService;
    MediaOfflineService mediaOfflineService;

    MediaExtension(final ExtensionApi extensionApi) {
        super(extensionApi);
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
        mediaState = new MediaState();
        MediaSessionCreatedDispatcher mediaSessionCreatedDispatcher =
                new MediaSessionCreatedDispatcher(getApi());
        mediaOfflineService = new MediaOfflineService(mediaState, mediaSessionCreatedDispatcher);
        mediaRealTimeService = new MediaRealTimeService(mediaState, mediaSessionCreatedDispatcher);

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

    @Override
    protected void onUnregistered() {
        mediaOfflineService.destroy();
        mediaOfflineService = null;
        mediaRealTimeService.destroy();
        mediaRealTimeService = null;
    }

    void handleMediaTrackerRequestEvent(final Event event) {
        if (event.getEventData() == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "handleMediaTrackerRequestEvent - Failed to process tracker request event"
                            + " (data was null).");
            return;
        }

        String trackerId =
                DataReader.optString(
                        event.getEventData(),
                        MediaInternalConstants.EventDataKeys.Tracker.ID,
                        null);
        if (trackerId == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "handleMediaTrackerRequestEvent - Tracker id missing in event data");
            return;
        }

        createTracker(trackerId, event);
    }

    void handleMediaTrackEvent(final Event event) {
        if (event.getEventData() == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "handleMediaTrackEvent - Failed to process media track event (data was null)");
            return;
        }

        String trackerId =
                DataReader.optString(
                        event.getEventData(),
                        MediaInternalConstants.EventDataKeys.Tracker.ID,
                        null);
        if (trackerId == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "handleMediaTrackEvent - Tracker id missing in event data");
            return;
        }

        trackMedia(trackerId, event);
    }

    void handleResetIdentities(final Event event) {
        if (event == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "handleResetIdentities - Ignoring null event");
            return;
        }

        mediaOfflineService.reset();
        mediaRealTimeService.reset();
    }

    void createTracker(final String trackerId, final Event event) {
        Map<String, Object> trackerConfig =
                DataReader.optTypedMap(
                        Object.class,
                        event.getEventData(),
                        MediaInternalConstants.EventDataKeys.Tracker.EVENT_PARAM,
                        null);
        boolean isDownloadedContent = false;
        if (trackerConfig != null) {
            isDownloadedContent =
                    DataReader.optBoolean(
                            trackerConfig,
                            MediaInternalConstants.EventDataKeys.Config.DOWNLOADED_CONTENT,
                            false);
        }

        MediaHitProcessor hitProcessor =
                isDownloadedContent ? mediaOfflineService : mediaRealTimeService;
        MediaTrackerInterface tracker = new MediaCollectionTracker(hitProcessor, trackerConfig);
        trackers.put(trackerId, tracker);
    }

    boolean trackMedia(final String trackerId, final Event event) {
        if (!trackers.containsKey(trackerId)) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "trackMedia - Tracker missing in store for id %s",
                    trackerId);
            return false;
        }

        MediaTrackerInterface tracker = trackers.get(trackerId);
        tracker.track(event);
        return true;
    }
}
