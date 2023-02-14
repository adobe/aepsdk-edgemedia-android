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

package com.adobe.marketing.mobile.edgemedia.internal;

import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.util.*;

class MediaOfflineService implements MediaHitProcessor {
    private static final String LOG_TAG = "MediaOfflineService";
    private static final String FLUSH_TIMER = "MediaOfflineServiceFlushTimer";
    private static final int FLUSH_TIMER_INTERVAL_MS = 60000; // 1 min
    private static final int HTTP_TIMEOUT_SEC = 5;
    private static final int HTTP_OK = 200;
    private static final int HTTP_MULTIPLE_CHOICES = 300;

    private final MediaState mediaState;
    private final MediaSessionCreatedDispatcher dispatcher;
    private final MediaDBService mediaDBService;
    private final MediaSessionIDManager mediasessionIDManager;
    private boolean isReportingSession;
    private String currentReportingSession;
    private Timer flushTimer;
    private final Object mutex;

    MediaOfflineService(
            final MediaState mediaState, final MediaSessionCreatedDispatcher dispatcher) {
        this(new MediaDBServiceImpl(), mediaState, dispatcher, true);
    }

    MediaOfflineService(
            final MediaDBService mediaDBService,
            final MediaState mediaState,
            final MediaSessionCreatedDispatcher dispatcher,
            final boolean enableFlushTimer) {
        this.mediaDBService = mediaDBService;
        this.mediaState = mediaState;
        this.dispatcher = dispatcher;

        Set<String> persistedSessions = mediaDBService.getSessionIDs();
        mediasessionIDManager = new MediaSessionIDManager(persistedSessions);

        isReportingSession = false;
        currentReportingSession = null;

        mutex = new Object();

        if (enableFlushTimer) {
            startFlushTimer();
        }
    }

    void destroy() {
        stopFlushTimer();
    }

    void startFlushTimer() {
        synchronized (mutex) {
            try {
                TimerTask timerTask =
                        new TimerTask() {
                            @Override
                            public void run() {
                                reportCompletedSessions();
                            }
                        };

                flushTimer = new Timer(FLUSH_TIMER);
                flushTimer.scheduleAtFixedRate(timerTask, 0, FLUSH_TIMER_INTERVAL_MS);
            } catch (Exception e) {
                Log.error(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "startFlushTimer - Error starting timer %s",
                        e.getMessage());
            }
        }
    }

    void stopFlushTimer() {
        synchronized (mutex) {
            if (flushTimer != null) {
                flushTimer.cancel();
            }
        }
    }

    public void notifyMobileStateChanges() {
        synchronized (mutex) {
            if (mediaState.getPrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "notifyMobileStateChanges - Privacy set to opt_out, clearing persisted"
                                + " media sessions");
                abortAllSessions();
            } else {
                reportCompletedSessionsAsync();
            }
        }
    }

    private void abortAllSessions() {
        mediasessionIDManager.clear();
        mediaDBService.deleteAllHits();
        isReportingSession = false;
    }

    public void reset() {
        Log.trace(
                MediaInternalConstants.EXTENSION_LOG_TAG,
                LOG_TAG,
                "reset - Aborting persisted media sessions");

        synchronized (mutex) {
            abortAllSessions();
        }
    }

    @Override
    public String startSession() {
        synchronized (mutex) {
            // If opt-out don't start a session
            if (mediaState.getPrivacyStatus() == MobilePrivacyStatus.OPT_OUT) {
                return null;
            }

            String sessionId = mediasessionIDManager.startActiveSession();
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "startSession - Session (%s) started successfully.",
                    sessionId);
            return sessionId;
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

            if (mediasessionIDManager.isSessionActive(sessionId)) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "processHit - Session (%s) Queueing hit %s.",
                        sessionId,
                        hit.getEventType());
                mediaDBService.persistHit(sessionId, hit);
            } else {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "processHit - Session (%s) missing in store.",
                        sessionId);
            }
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

            if (mediasessionIDManager.isSessionActive(sessionId)) {
                mediasessionIDManager.updateSessionState(
                        sessionId, MediaSessionIDManager.MediaSessionState.Complete);
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "endSession - Session (%s) ended.",
                        sessionId);
                reportCompletedSessionsAsync();
            } else {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "endSession - Session (%s) missing in store.",
                        sessionId);
            }
        }
    }

    void addTask(final TimerTask task) {
        try {
            flushTimer.schedule(task, 0);
        } catch (Exception e) {
            Log.warning(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "addTask - Failed with exception " + e.getMessage());
        }
    }

    synchronized void reportCompletedSessionsAsync() {
        TimerTask timerTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        reportCompletedSessions();
                    }
                };

        addTask(timerTask);
    }

    boolean reportCompletedSessions() {
        String url;
        String body;

        synchronized (mutex) {
            if (isReportingSession) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "ReportCompletedSessions - Exiting as we are currently sending session"
                                + " report.");
                return false;
            }

            final String sessionID = mediasessionIDManager.getSessionToReport();

            if (sessionID == null) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "ReportCompletedSessions - Exiting as we have no pending sessions to"
                                + " report.");
                return false;
            }

            if (!MediaReportHelper.isReadyToSendHit(
                    ServiceProvider.getInstance().getDeviceInfoService(), mediaState)) {
                return false;
            }

            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "ReportCompletedSessions - Reporting Session %s.",
                    sessionID);
            List<MediaHit> hits = mediaDBService.getHits(sessionID);

            url = MediaReportHelper.getTrackingURL(mediaState.getMediaCollectionServer());
            body = MediaReportHelper.generateDownloadReport(mediaState, hits);

            if (body == null || body.length() == 0) {
                Log.warning(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "ReportCompletedSessions - Could not generate downloaded content report"
                                + " from persisted hits for session %s. Clearing persisted pings.",
                        sessionID);
                mediasessionIDManager.updateSessionState(
                        sessionID, MediaSessionIDManager.MediaSessionState.Invalid);

                if (mediasessionIDManager.shouldClearSession(sessionID)) {
                    mediaDBService.deleteHits(sessionID);
                }

                return false;
            }

            if (url == null || url.length() == 0) {
                Log.warning(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "ReportCompletedSessions - Could not generate url for reporting downloaded"
                                + " content report for session %s",
                        sessionID);
                return false;
            }

            isReportingSession = true;
            currentReportingSession = sessionID;
        }

        HashMap<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(
                MediaInternalConstants.Networking.HTTP_HEADER_KEY_CONTENT_TYPE,
                MediaInternalConstants.Networking.HTTP_HEADER_CONTENT_TYPE_JSON_APPLICATION);

        // Disable sending assurance integration id till backend returns generated session id.
        // String assuranceIntegrationId = mediaState.getAssuranceIntegrationId();
        // if (assuranceIntegrationId != null) {
        //	requestHeaders.put(MediaCoreConstants.Networking.HEADER_KEY_AEP_VALIDATION_TOKEN,
        // assuranceIntegrationId);
        // }

        NetworkRequest request =
                new NetworkRequest(
                        url,
                        HttpMethod.POST,
                        body.getBytes(),
                        requestHeaders,
                        HTTP_TIMEOUT_SEC,
                        HTTP_TIMEOUT_SEC);
        ServiceProvider.getInstance()
                .getNetworkService()
                .connectAsync(
                        request,
                        connection -> {
                            boolean success = false;

                            synchronized (mutex) {
                                if (connection == null) {
                                    Log.debug(
                                            MediaInternalConstants.EXTENSION_LOG_TAG,
                                            LOG_TAG,
                                            "ReportCompletedSessions - Http request error,"
                                                    + " connection was null");
                                } else {
                                    int respCode = connection.getResponseCode();

                                    Log.trace(
                                            MediaInternalConstants.EXTENSION_LOG_TAG,
                                            LOG_TAG,
                                            "ReportCompletedSessions - Http request completed for"
                                                    + " session %s with status code %s.",
                                            currentReportingSession,
                                            respCode);

                                    success =
                                            respCode >= HTTP_OK && respCode < HTTP_MULTIPLE_CHOICES;

                                    MediaSessionIDManager.MediaSessionState sessionState =
                                            success
                                                    ? MediaSessionIDManager.MediaSessionState
                                                            .Reported
                                                    : MediaSessionIDManager.MediaSessionState
                                                            .Failed;

                                    mediasessionIDManager.updateSessionState(
                                            currentReportingSession, sessionState);

                                    if (mediasessionIDManager.shouldClearSession(
                                            currentReportingSession)) {
                                        Log.trace(
                                                MediaInternalConstants.EXTENSION_LOG_TAG,
                                                LOG_TAG,
                                                "ReportCompletedSessions - Clearing persisted"
                                                        + " pings for session %s.",
                                                currentReportingSession);
                                        mediaDBService.deleteHits(currentReportingSession);
                                    }
                                }

                                isReportingSession = false;
                                currentReportingSession = null;
                            }

                            if (connection != null) {
                                connection.close();
                            }

                            // Note :- If http request succeeds, we can try sending next available
                            // sessions.
                            // If http fails, it does not make sense to retry it now and we will
                            // handle during next timer tick.
                            // Todo :- Once backend returns session id, we should dispatch a
                            // sessionCreate event.
                            if (success) {
                                reportCompletedSessionsAsync();
                            }
                        });

        return true;
    }
}
