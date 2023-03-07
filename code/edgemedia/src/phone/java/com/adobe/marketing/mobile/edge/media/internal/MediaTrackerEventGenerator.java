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

import static com.adobe.marketing.mobile.edge.media.internal.MediaInternalConstants.EventDataKeys;
import static com.adobe.marketing.mobile.edge.media.internal.MediaInternalConstants.LOG_TAG;

import androidx.annotation.VisibleForTesting;
import com.adobe.marketing.mobile.AdobeCallback;
import com.adobe.marketing.mobile.Event;
import com.adobe.marketing.mobile.edge.media.Media;
import com.adobe.marketing.mobile.edge.media.MediaTracker;
import com.adobe.marketing.mobile.services.Log;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MediaTrackerEventGenerator implements MediaTracker {
    private static final String SOURCE_TAG = "MediaTrackerEventGenerator";

    private static final int TICK_INTERVAL_MS = 750;
    private static final int EVENT_TIMEOUT_MS = 500;
    private final AdobeCallback<Event> eventConsumer;
    private final String trackerId;
    private String sessionId;
    private boolean inSession;
    private Timer timer;
    private long lastEventTS;
    private Map<String, Object> lastPlayheadParams;

    @VisibleForTesting
    MediaTrackerEventGenerator(final String trackerId, final AdobeCallback<Event> eventConsumer) {
        this.eventConsumer = eventConsumer;
        this.trackerId = trackerId;
        this.sessionId = getUniqueId();
        this.inSession = false;
    }

    public static MediaTrackerEventGenerator create(
            final Map<String, Object> config, final AdobeCallback<Event> eventConsumer) {
        final String trackerId = getUniqueId();

        final Map<String, Object> cleanedConfig = new HashMap<>();

        if (config != null) {
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Boolean || value instanceof String) {
                    cleanedConfig.put(entry.getKey(), entry.getValue());
                } else {
                    // we just expect String and boolean config params
                    Log.debug(
                            LOG_TAG,
                            SOURCE_TAG,
                            "create - Unsupported config key:%s valueType:%s",
                            entry.getKey(),
                            entry.getValue().getClass().toString());
                }
            }
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EventDataKeys.Tracker.ID, trackerId);
        eventData.put(EventDataKeys.Tracker.EVENT_PARAM, cleanedConfig);
        Event event =
                new Event.Builder(
                                "Edge Media CreateTrackerRequest",
                                MediaInternalConstants.Media.EVENT_TYPE_EDGE_MEDIA,
                                MediaInternalConstants.Media.EVENT_SOURCE_TRACKER_REQUEST)
                        .setEventData(eventData)
                        .build();

        eventConsumer.call(event);
        Log.debug(LOG_TAG, SOURCE_TAG, "create - Tracker request event was sent to event hub.");

        // We have sent a request to media extension to create a tracker.
        // We can now return MediaTrackerCore which sends all the tracker events to the event hub.
        return new MediaTrackerEventGenerator(trackerId, eventConsumer);
    }

    private static synchronized String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    public void trackSessionStart(
            final Map<String, Object> info, final Map<String, String> metadata) {
        trackInternal(EventDataKeys.MediaEventName.SESSION_START, info, metadata);
    }

    public void trackPlay() {
        trackInternal(EventDataKeys.MediaEventName.PLAY);
    }

    public void trackPause() {
        trackInternal(EventDataKeys.MediaEventName.PAUSE);
    }

    public void trackComplete() {
        trackInternal(EventDataKeys.MediaEventName.COMPLETE);
    }

    public void trackSessionEnd() {
        trackInternal(EventDataKeys.MediaEventName.SESSION_END);
    }

    public void trackError(final String errorId) {
        Map<String, Object> params = new HashMap<>();
        params.put(EventDataKeys.ErrorInfo.ID, errorId);
        trackInternal(EventDataKeys.MediaEventName.ERROR, params, null);
    }

    public void trackEvent(
            final Media.Event event,
            final Map<String, Object> info,
            final Map<String, String> metadata) {
        trackInternal(eventToString(event), info, metadata);
    }

    public void updateCurrentPlayhead(final double playheadValue) {
        Map<String, Object> params = new HashMap<>();
        params.put(EventDataKeys.Tracker.PLAYHEAD, playheadValue);
        trackInternal(EventDataKeys.MediaEventName.PLAYHEAD_UPDATE, params, null);
    }

    public void updateQoEObject(final Map<String, Object> qoeInfo) {
        trackInternal(EventDataKeys.MediaEventName.QOE_UPDATE, qoeInfo, null);
    }

    void trackInternal(final String eventName) {
        trackInternal(eventName, null, null, false);
    }

    void trackInternal(
            final String eventName,
            final Map<String, Object> params,
            final Map<String, String> metadata) {
        trackInternal(eventName, params, metadata, false);
    }

    synchronized void trackInternal(
            final String eventName,
            final Map<String, Object> params,
            final Map<String, String> metadata,
            final boolean internalEvent) {

        if (eventName == null) {
            return;
        }

        // Internal Tracker starts a new session only when we are not in an active session and we
        // follow the same.
        if (eventName.equals(EventDataKeys.MediaEventName.SESSION_START) && params != null) {
            boolean isValidSessionStart = MediaObject.isValidMediaInfo(params);

            if (!inSession && isValidSessionStart) {
                sessionId = getUniqueId();
                inSession = true;
                startTimer();
            }
        } else if (eventName.equals(EventDataKeys.MediaEventName.SESSION_END)
                || eventName.equals(EventDataKeys.MediaEventName.COMPLETE)) {
            inSession = false;
            stopTimer();
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put(EventDataKeys.Tracker.ID, trackerId);
        eventData.put(EventDataKeys.Tracker.SESSION_ID, sessionId);
        eventData.put(EventDataKeys.Tracker.EVENT_NAME, eventName);
        eventData.put(EventDataKeys.Tracker.EVENT_INTERNAL, internalEvent);

        if (params != null) {
            eventData.put(EventDataKeys.Tracker.EVENT_PARAM, params);
        }

        if (metadata != null) {
            eventData.put(EventDataKeys.Tracker.EVENT_METADATA, metadata);
        }

        long ts = getCurrentTimestamp();
        eventData.put(EventDataKeys.Tracker.EVENT_TIMESTAMP, ts);

        Event event =
                new Event.Builder(
                                "Edge Media TrackMedia",
                                MediaInternalConstants.Media.EVENT_TYPE_EDGE_MEDIA,
                                MediaInternalConstants.Media.EVENT_SOURCE_TRACK_MEDIA)
                        .setEventData(eventData)
                        .build();
        eventConsumer.call(event);

        lastEventTS = ts;

        if (eventName.equals(EventDataKeys.MediaEventName.PLAYHEAD_UPDATE) && params != null) {
            lastPlayheadParams = new HashMap<>(params);
        }
    }

    long getCurrentTimestamp() {
        return Calendar.getInstance().getTimeInMillis();
    }

    protected synchronized void tick() {
        long currentTS = getCurrentTimestamp();

        if ((currentTS - lastEventTS) > EVENT_TIMEOUT_MS) {
            // We have not got any public api call for 500 ms.
            // We manually send an event to keep our internal processing alive (idle tracking / ping
            // processing).
            trackInternal(
                    EventDataKeys.MediaEventName.PLAYHEAD_UPDATE, lastPlayheadParams, null, true);
        }
    }

    protected void startTimer() {
        if (timer != null) {
            return;
        }

        TimerTask tickTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        tick();
                    }
                };

        timer = new Timer();
        timer.scheduleAtFixedRate(tickTask, 0, TICK_INTERVAL_MS);
    }

    protected void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private String eventToString(final Media.Event event) {
        switch (event) {
            case AdBreakStart:
                return EventDataKeys.MediaEventName.ADBREAK_START;

            case AdBreakComplete:
                return EventDataKeys.MediaEventName.ADBREAK_COMPLETE;

            case AdStart:
                return EventDataKeys.MediaEventName.AD_START;

            case AdComplete:
                return EventDataKeys.MediaEventName.AD_COMPLETE;

            case AdSkip:
                return EventDataKeys.MediaEventName.AD_SKIP;

            case ChapterStart:
                return EventDataKeys.MediaEventName.CHAPTER_START;

            case ChapterComplete:
                return EventDataKeys.MediaEventName.CHAPTER_COMPLETE;

            case ChapterSkip:
                return EventDataKeys.MediaEventName.CHAPTER_SKIP;

            case SeekStart:
                return EventDataKeys.MediaEventName.SEEK_START;

            case SeekComplete:
                return EventDataKeys.MediaEventName.SEEK_COMPLETE;

            case BufferStart:
                return EventDataKeys.MediaEventName.BUFFER_START;

            case BufferComplete:
                return EventDataKeys.MediaEventName.BUFFER_COMPLETE;

            case BitrateChange:
                return EventDataKeys.MediaEventName.BITRATE_CHANGE;

            case StateStart:
                return EventDataKeys.MediaEventName.STATE_START;

            case StateEnd:
                return EventDataKeys.MediaEventName.STATE_END;

            default:
                return "";
        }
    }
}
