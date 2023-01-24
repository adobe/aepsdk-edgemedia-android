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

import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.services.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

class MediaRealTimeService implements MediaHitProcessor {
    private static final String LOG_TAG = "MediaRealTimeService";
    private static final String TICK_TIMER = "MediaRealTimeServiceTickTimer";
    private static final int TICK_TIMER_INTERVAL_MS = 250;

    private final Object mutex;
    private Timer tickTimer;
    private boolean timerActive;

    private final MediaState mediaState;
    private final MediaSessionCreatedDispatcher dispatcher;
    private final Map<String, MediaSession> sessionsMap;

    MediaRealTimeService(
            final MediaState mediaState, final MediaSessionCreatedDispatcher dispatcher) {
        mutex = new Object();
        timerActive = false;
        this.mediaState = mediaState;
        this.dispatcher = dispatcher;
        sessionsMap = new HashMap<>();
        startTickTimer();
    }

    public void notifyMobileStateChanges() {
        synchronized (mutex) {
            if (mediaState.getPrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "notifyMobileStateChanges - Privacy switched to opt_out, aborting existing"
                                + " sessions");
                abortAllSessions();
            }
        }
    }

    public void reset() {
        Log.trace(
                MediaInternalConstants.EXTENSION_LOG_TAG,
                LOG_TAG,
                "reset - Aborting all existing sessions");
        abortAllSessions();
    }

    protected void startTickTimer() {
        if (timerActive) {
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "startTickTimer - TickTimer is already active and running.");
            return;
        }

        try {
            TimerTask tickTask =
                    new TimerTask() {
                        @Override
                        public void run() {
                            processSession();
                        }
                    };
            tickTimer = new Timer(TICK_TIMER);
            tickTimer.scheduleAtFixedRate(tickTask, 0, TICK_TIMER_INTERVAL_MS);
            timerActive = true;
        } catch (Exception e) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "startTickTimer - Error starting timer %s",
                    e.getMessage());
        }
    }

    protected void stopTickTimer() {
        if (tickTimer != null) {
            tickTimer.cancel();
            timerActive = false;
        }
    }

    @Override
    public String startSession() {
        synchronized (mutex) {
            if (mediaState.getPrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "startSession - Cannot start session as privacy is opted-out.");
                return null;
            }

            String internalSessionID = UUID.randomUUID().toString();
            MediaSession sessionObj = new MediaSession(mediaState, dispatcher);
            sessionsMap.put(internalSessionID, sessionObj);
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "startSession - Session (%s) started successfully.",
                    internalSessionID);
            return internalSessionID;
        }
    }

    @Override
    public void processHit(final String sessionId, final MediaHit hit) {
        synchronized (mutex) {
            if (sessionId == null) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "processHit - Session id is null");
                return;
            }

            if (hit == null) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "processHit - Session (%s) hit is null.",
                        sessionId);
                return;
            }

            if (!sessionsMap.containsKey(sessionId)) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "processHit - Session (%s) missing in store.",
                        sessionId);
                return;
            }

            MediaSession sessionObj = sessionsMap.get(sessionId);
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "processHit - Session (%s) Queueing hit %s.",
                    sessionId,
                    hit.getEventType());
            sessionObj.queueHit(hit);
        }
    }

    @Override
    public void endSession(final String sessionId) {
        synchronized (mutex) {
            if (sessionId == null) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "endSession - Session id is null");
                return;
            }

            if (!sessionsMap.containsKey(sessionId)) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "endSession - Session (%s) missing in store.",
                        sessionId);
                return;
            }

            MediaSession session = sessionsMap.get(sessionId);
            session.end();
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "endSession - Session (%s) ended.",
                    sessionId);
        }
    }

    private void processSession() {
        synchronized (mutex) {
            Iterator<Map.Entry<String, MediaSession>> iter = sessionsMap.entrySet().iterator();

            while (iter.hasNext()) {
                Map.Entry<String, MediaSession> entry = iter.next();
                MediaSession session = entry.getValue();
                session.process();

                if (session.finishedProcessing()) {
                    Log.trace(
                            MediaInternalConstants.EXTENSION_LOG_TAG,
                            LOG_TAG,
                            "processSession - Session (%s) has finished processing. Removing it"
                                    + " from store.",
                            entry.getKey());
                    iter.remove();
                }
            }
        }
    }

    private void abortAllSessions() {
        for (Map.Entry<String, MediaSession> entry : sessionsMap.entrySet()) {
            MediaSession session = entry.getValue();
            session.abort();
        }
    }

    public void destroy() {
        synchronized (mutex) {
            stopTickTimer();
        }
    }
}
