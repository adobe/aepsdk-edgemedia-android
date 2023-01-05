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

package com.adobe.marketing.mobile.media;

import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.HashMap;
import java.util.Map;

class MediaCollectionHitGenerator {
    private static final String LOG_TAG = "MediaCollectionHitGenerator";
    private static final long DEFAULT_OFFLINE_PING_INTERVAL = 50000; // 50 secs in ms
    private static final long DEFAULT_ONLINE_PING_INTERVAL = 10000; // 10 secs in ms for online
    private static final long GRANULAR_AD_PING_INTERVAL = 1000; // 1 sec in ms for online ad

    private final MediaContext mediaContext;
    private final MediaHitProcessor mediaHitProcessor;
    private final Map<String, Object> mediaConfig;
    private final boolean downloadedContent;
    private Map<String, Object> lastQOEData;
    private String sessionID;
    private boolean isTracking;
    private long interval;
    private long refTS;
    private MediaPlayBackState previousState;
    private long previousStateTS;
    private final String refSessionId;

    MediaCollectionHitGenerator(
            final MediaContext context,
            final MediaHitProcessor hitProcessor,
            final Map<String, Object> config,
            final long refTS,
            final String refSessionId) {
        mediaContext = context;
        mediaHitProcessor = hitProcessor;
        mediaConfig = config;

        this.refTS = refTS;
        this.refSessionId = refSessionId;
        previousState = MediaPlayBackState.Init;
        previousStateTS = refTS;
        lastQOEData = new HashMap<>();
        downloadedContent =
                DataReader.optBoolean(
                        mediaConfig,
                        MediaInternalConstants.EventDataKeys.Config.DOWNLOADED_CONTENT,
                        false);

        interval = downloadedContent ? DEFAULT_OFFLINE_PING_INTERVAL : DEFAULT_ONLINE_PING_INTERVAL;

        startTrackingSession();
    }

    void processMediaStart() {
        processMediaStart(false);
    }

    void processMediaStart(final boolean forceResume) {
        Map<String, Object> params = MediaCollectionHelper.extractMediaParams(mediaContext);

        if (forceResume) {
            params.put(MediaCollectionConstants.Media.RESUME.key, true);
        }

        params.put(MediaCollectionConstants.Media.DOWNLOADED.key, downloadedContent);

        // Params to link client generated session id with backend generated id.
        params.put(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID, refSessionId);

        String channel =
                DataReader.optString(
                        mediaConfig, MediaInternalConstants.EventDataKeys.Config.CHANNEL, null);
        if (channel != null) {
            params.put(MediaCollectionConstants.Media.CHANNEL.key, channel);
        }

        Map<String, String> customMetadata =
                MediaCollectionHelper.extractMediaMetadata(mediaContext);

        generateHit(MediaCollectionConstants.EventType.SESSION_START, params, customMetadata);
    }

    void processMediaComplete() {
        generateHit(MediaCollectionConstants.EventType.SESSION_COMPLETE);

        endTrackingSession();
    }

    void processMediaSkip() {
        generateHit(MediaCollectionConstants.EventType.SESSION_END);

        endTrackingSession();
    }

    void processAdBreakStart() {
        Map<String, Object> params = MediaCollectionHelper.extractAdBreakParams(mediaContext);
        generateHit(MediaCollectionConstants.EventType.ADBREAK_START, params, new HashMap<>());
    }

    void processAdBreakComplete() {
        generateHit(MediaCollectionConstants.EventType.ADBREAK_COMPLETE);
    }

    void processAdBreakSkip() {
        generateHit(MediaCollectionConstants.EventType.ADBREAK_COMPLETE);
    }

    void processAdStart() {

        if (downloadedContent) {
            interval = DEFAULT_OFFLINE_PING_INTERVAL;
        } else if (mediaContext.getMediaInfo().isGranularAdTrackingEnabled()) {
            interval = GRANULAR_AD_PING_INTERVAL;
        } else {
            interval = DEFAULT_ONLINE_PING_INTERVAL;
        }

        Map<String, Object> params = MediaCollectionHelper.extractAdParams(mediaContext);
        Map<String, String> metadata = MediaCollectionHelper.extractAdMetadata(mediaContext);

        generateHit(MediaCollectionConstants.EventType.AD_START, params, metadata);
    }

    void processAdComplete() {
        interval = downloadedContent ? DEFAULT_OFFLINE_PING_INTERVAL : DEFAULT_ONLINE_PING_INTERVAL;
        generateHit(MediaCollectionConstants.EventType.AD_COMPLETE);
    }

    void processAdSkip() {
        interval = downloadedContent ? DEFAULT_OFFLINE_PING_INTERVAL : DEFAULT_ONLINE_PING_INTERVAL;
        generateHit(MediaCollectionConstants.EventType.AD_SKIP);
    }

    void processChapterStart() {
        Map<String, Object> params = MediaCollectionHelper.extractChapterParams(mediaContext);
        Map<String, String> metadata = MediaCollectionHelper.extractChapterMetadata(mediaContext);

        generateHit(MediaCollectionConstants.EventType.CHAPTER_START, params, metadata);
    }

    void processChapterComplete() {
        generateHit(MediaCollectionConstants.EventType.CHAPTER_COMPLETE);
    }

    void processChapterSkip() {
        generateHit(MediaCollectionConstants.EventType.CHAPTER_SKIP);
    }

    /** End media session after 24 hr timeout or idle timeout(30 mins). */
    void processSessionAbort() {
        processMediaSkip();
    }

    /** Restart session again after 24 hr timeout or idle timeout recovered. */
    void processSessionRestart() {
        previousState = MediaPlayBackState.Init;
        previousStateTS = refTS;

        lastQOEData.clear();

        sessionID = mediaHitProcessor.startSession();
        isTracking = true;

        processMediaStart(true);

        if (mediaContext.isInChapter()) {
            processChapterStart();
        }

        if (mediaContext.isInAdBreak()) {
            processAdBreakStart();
        }

        if (mediaContext.isInAd()) {
            processAdStart();
        }

        for (StateInfo state : mediaContext.getActiveTrackedStates()) {
            processStateStart(state);
        }

        processPlayback(true);
    }

    void processBitrateChange() {
        Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);
        generateHit(
                MediaCollectionConstants.EventType.BITRATE_CHANGE,
                new HashMap<>(),
                new HashMap<>(),
                qoeData);
    }

    void processError(final String errorId) {
        Map<String, Object> params = new HashMap<>();

        Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);
        qoeData.put(MediaCollectionConstants.QoE.ERROR_ID.key, errorId);
        qoeData.put(
                MediaCollectionConstants.QoE.ERROR_SOURCE.key,
                MediaCollectionConstants.QoE.ERROR_SOURCE_PLAYER.key);

        generateHit(MediaCollectionConstants.EventType.ERROR, params, new HashMap<>(), qoeData);
    }

    void processPlayback(final boolean doFlush) {
        if (!isTracking) {
            return;
        }

        MediaPlayBackState currentState = getPlaybackState();

        if (previousState != currentState || doFlush) {
            String eventType = getMediaCollectionEvent(currentState);
            generateHit(eventType);

            previousState = currentState;
            previousStateTS = refTS;
        } else if (previousState == currentState && (refTS - previousStateTS) >= interval) {

            // if the ts difference is more than interval we need to send it as multiple pings
            generateHit(MediaCollectionConstants.EventType.PING);
            previousStateTS = refTS;
        }
    }

    void setRefTS(final long ts) {
        refTS = ts;
    }

    void generateHit(final String eventType) {
        generateHit(eventType, new HashMap<>(), new HashMap<>());
    }

    void generateHit(
            final String eventType,
            final Map<String, Object> params,
            final Map<String, String> metadata) {
        Map<String, Object> qoeData = MediaCollectionHelper.extractQoEData(mediaContext);
        boolean qoeInfoUpdated = !lastQOEData.equals(qoeData);

        if (qoeInfoUpdated) {
            generateHit(eventType, params, metadata, qoeData);
        } else {
            generateHit(eventType, params, metadata, new HashMap<>());
        }
    }

    void generateHit(
            final String eventType,
            final Map<String, Object> params,
            final Map<String, String> metadata,
            final Map<String, Object> qoeData) {

        // Update the lastQueData so we don't resend it with the next ping
        if (!qoeData.isEmpty()) {
            lastQOEData = qoeData;
        }

        if (!isTracking) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "generateHit - Dropping hit as we have internally stopped tracking");
            return;
        }

        double playhead = mediaContext.getPlayhead();
        long ts = refTS;

        MediaHit hit = new MediaHit(eventType, params, metadata, qoeData, playhead, ts);
        mediaHitProcessor.processHit(sessionID, hit);
    }

    void processStateStart(final StateInfo stateInfo) {
        Map<String, Object> params = new HashMap<>();

        params.put(MediaCollectionConstants.State.STATE_NAME.key, stateInfo.getStateName());

        final Map<String, String> metadata = new HashMap<>();

        generateHit(
                MediaCollectionConstants.EventType.STATE_START, params, metadata, new HashMap<>());
    }

    void processStateEnd(final StateInfo stateInfo) {
        Map<String, Object> params = new HashMap<>();

        params.put(MediaCollectionConstants.State.STATE_NAME.key, stateInfo.getStateName());

        final Map<String, String> metadata = new HashMap<>();

        generateHit(
                MediaCollectionConstants.EventType.STATE_END, params, metadata, new HashMap<>());
    }

    void startTrackingSession() {
        sessionID = mediaHitProcessor.startSession();
        isTracking = sessionID != null;
        if (sessionID == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "Unable to create a tracking session.");
        } else {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "Started a new session with id (%s).",
                    sessionID);
        }
    }

    void endTrackingSession() {
        if (isTracking) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "Ending the session with id (%s).",
                    sessionID);
            mediaHitProcessor.endSession(sessionID);
            isTracking = false;
        }
    }

    MediaPlayBackState getPlaybackState() {
        if (mediaContext.isInState(MediaPlayBackState.Buffer)) {
            return MediaPlayBackState.Buffer;
        } else if (mediaContext.isInState(MediaPlayBackState.Seek)) {
            return MediaPlayBackState.Seek;
        } else if (mediaContext.isInState(MediaPlayBackState.Play)) {
            return MediaPlayBackState.Play;
        } else if (mediaContext.isInState(MediaPlayBackState.Pause)) {
            return MediaPlayBackState.Pause;
        } else if (mediaContext.isInState(MediaPlayBackState.Stall)) {
            return MediaPlayBackState.Stall;
        } else {
            return MediaPlayBackState.Init;
        }
    }

    String getMediaCollectionEvent(final MediaPlayBackState state) {
        if (state == MediaPlayBackState.Buffer) {
            return MediaCollectionConstants.EventType.BUFFER_START;
        } else if (state == MediaPlayBackState.Seek) {
            return MediaCollectionConstants.EventType.PAUSE_START;
        } else if (state == MediaPlayBackState.Play) {
            return MediaCollectionConstants.EventType.PLAY;
        } else if (state == MediaPlayBackState.Pause) {
            return MediaCollectionConstants.EventType.PAUSE_START;
        } else if (state == MediaPlayBackState.Stall) {
            // Stall not supported by backend we just send Play event for it
            return MediaCollectionConstants.EventType.PLAY;
        } else if (state == MediaPlayBackState.Init) {
            // We should never hit this condition as there is not event to denote init.
            // Ping without any previous playback state denotes init.
            return MediaCollectionConstants.EventType.PING;
        } else {
            return "";
        }
    }
}
