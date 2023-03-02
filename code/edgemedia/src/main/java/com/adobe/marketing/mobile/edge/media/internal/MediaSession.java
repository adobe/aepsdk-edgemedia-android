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

import com.adobe.marketing.mobile.services.HttpMethod;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.services.NetworkRequest;
import com.adobe.marketing.mobile.services.ServiceProvider;
import java.util.HashMap;
import java.util.LinkedList;

class MediaSession {
    private static final String LOG_TAG = "MediaSession";
    private static final int HTTP_TIMEOUT_SEC = 5;
    private static final int HTTP_OK = 200;
    private static final int HTTP_MULTIPLE_CHOICES = 300;
    private static final int RETRY_COUNT = 2;
    private static final long MAX_ALLOWED_DURATION_BETWEEN_HITS_MS = 60000;

    private final Object mutex;
    private final MediaState mediaState;
    private final LinkedList<MediaHit> hits;

    private String sessionID;
    private boolean isSessionActive;
    private boolean isSendingHit;
    private int sessionStartRetryCount;
    private long lastRefTS;
    private final MediaSessionCreatedDispatcher dispatcher;

    MediaSession(final MediaState mediaState, final MediaSessionCreatedDispatcher dispatcher) {
        this.mediaState = mediaState;
        this.dispatcher = dispatcher;
        hits = new LinkedList<>();
        mutex = new Object();

        sessionID = null;
        isSessionActive = true;
        isSendingHit = false;
        sessionStartRetryCount = 0;
        lastRefTS = 0;
    }

    void queueHit(final MediaHit hit) {
        synchronized (mutex) {
            if (!isSessionActive) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "queueHit - Cannot add hit %s to the queue as the session has ended.",
                        hit.getEventType());
                return;
            }

            hits.add(hit);
        }
    }

    void process() {
        synchronized (mutex) {
            trySendHit();
        }
    }

    void end() {
        synchronized (mutex) {
            if (!isSessionActive) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "end - Session has already ended.");
                return;
            }

            isSessionActive = false;
        }
    }

    void abort() {
        synchronized (mutex) {
            if (!isSessionActive) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "abort - Session is not active.");
                return;
            }

            isSessionActive = false;
            hits.clear();
        }
    }

    boolean finishedProcessing() {
        synchronized (mutex) {
            return (!isSessionActive && !isSendingHit && hits.isEmpty());
        }
    }

    private void trySendHit() {
        if (hits.isEmpty()) {
            return;
        }

        if (isSendingHit) {
            return;
        }

        if (!MediaReportHelper.isReadyToSendHit(
                ServiceProvider.getInstance().getDeviceInfoService(), mediaState)) {
            return;
        }

        final MediaHit hit = hits.getFirst();
        final String hitEventType = hit.getEventType();
        final boolean hitIsSessionStart =
                hitEventType.equals(MediaCollectionConstants.EventType.SESSION_START);

        // The first hit we process should be Session Start and should only proceed if we have a
        // valid session id from MC backend.
        // If Session Start fails and we don't have a valid sessionId, drop all hits.
        if (!hitIsSessionStart && sessionID == null) {
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "trySendHit - (%s) Dropping as session id is unavailable.",
                    hitEventType);

            removeHit();
            return;
        }

        if (hitIsSessionStart) {
            lastRefTS = hit.getTimeStamp();
        }

        final String clientSessionId = MediaReportHelper.extractClientSessionId(hit);

        // We currently just lof the error and don't do any error correction.
        // This should never happen. Might happen in some devices if app goes to sleep and timer
        // stops ticking.
        long currRefTs = hit.getTimeStamp();
        long diff = currRefTs - lastRefTS;

        if (diff >= MAX_ALLOWED_DURATION_BETWEEN_HITS_MS) {
            Log.warning(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "trySendHit - (%s) TS difference from previous hit is (%f) greater than 60"
                            + " seconds.",
                    hitEventType,
                    diff);
        }

        lastRefTS = currRefTs;

        String urlString;

        if (hitIsSessionStart) {
            urlString = MediaReportHelper.getTrackingURL(mediaState.getMediaCollectionServer());
        } else {
            urlString =
                    MediaReportHelper.getTrackingUrlForEvents(
                            mediaState.getMediaCollectionServer(), sessionID);
        }

        final String url = urlString;
        final String body = MediaReportHelper.generateHitReport(mediaState, hit);

        Log.debug(
                MediaInternalConstants.EXTENSION_LOG_TAG,
                LOG_TAG,
                "trySendHit - (%s) Generated url %s",
                hitEventType,
                url);

        isSendingHit = true;

        HashMap<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put(
                MediaInternalConstants.Networking.HTTP_HEADER_KEY_CONTENT_TYPE,
                MediaInternalConstants.Networking.HTTP_HEADER_CONTENT_TYPE_JSON_APPLICATION);
        String assuranceIntegrationId = mediaState.getAssuranceIntegrationId();
        if (assuranceIntegrationId != null) {
            requestHeaders.put(
                    MediaInternalConstants.Networking.HEADER_KEY_AEP_VALIDATION_TOKEN,
                    assuranceIntegrationId);
        }

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
                            String mcSessionId = null;

                            do {
                                if (connection == null) {
                                    Log.debug(
                                            MediaInternalConstants.EXTENSION_LOG_TAG,
                                            LOG_TAG,
                                            "trySendHit - (%s) Http request error, connection was"
                                                    + " null",
                                            hitEventType);
                                    break;
                                }

                                int respCode = connection.getResponseCode();

                                if (!(respCode >= HTTP_OK && respCode < HTTP_MULTIPLE_CHOICES)) {
                                    Log.debug(
                                            MediaInternalConstants.EXTENSION_LOG_TAG,
                                            LOG_TAG,
                                            "trySendHit - (%s) Http failed with response code %d ",
                                            hitEventType,
                                            respCode);
                                    break;
                                }

                                if (!hitIsSessionStart) {
                                    break;
                                }

                                String sessionResponseFragment =
                                        connection.getResponsePropertyValue("Location");

                                if (sessionResponseFragment == null) {
                                    Log.trace(
                                            MediaInternalConstants.EXTENSION_LOG_TAG,
                                            LOG_TAG,
                                            "trySendHit - (%s) Media collection endpoint returned"
                                                    + " null location header",
                                            hitEventType);
                                    break;
                                }

                                mcSessionId =
                                        MediaReportHelper.extractSessionID(sessionResponseFragment);
                                Log.trace(
                                        MediaInternalConstants.EXTENSION_LOG_TAG,
                                        LOG_TAG,
                                        "trySendHit - (%s) Media collection endpoint created"
                                                + " internal session : %s",
                                        hitEventType,
                                        mcSessionId);

                                dispatcher.dispatchSessionCreatedEvent(
                                        clientSessionId, mcSessionId);
                            } while (false);

                            if (connection != null) {
                                connection.close();
                            }

                            Log.debug(
                                    MediaInternalConstants.EXTENSION_LOG_TAG,
                                    LOG_TAG,
                                    "trySendHit - (%s) Finished http connection",
                                    hitEventType);

                            synchronized (mutex) {
                                boolean shouldRetry = false;

                                if (hitIsSessionStart
                                        && mcSessionId != null
                                        && mcSessionId.length() > 0) {
                                    sessionID = mcSessionId;
                                } else if (hitIsSessionStart) {
                                    shouldRetry = sessionStartRetryCount < RETRY_COUNT;
                                    sessionStartRetryCount++;
                                }

                                isSendingHit = false;

                                if (!shouldRetry) {
                                    removeHit();
                                }
                            }
                        });
    }

    private void removeHit() {
        if (!hits.isEmpty()) {
            hits.removeFirst();
        }
    }

    // Testing methods
    int getQueueSize() {
        return hits.size();
    }

    String getSessionId() {
        return sessionID;
    }
}
