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

import com.adobe.marketing.mobile.Media;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.VisitorID;
import com.adobe.marketing.mobile.services.DeviceInforming;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import com.adobe.marketing.mobile.util.URLBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

class MediaReportHelper {
    static final String LOG_TAG = "MediaReportHelper";

    MediaReportHelper() {}

    static boolean isReadyToSendHit(
            final DeviceInforming deviceInforming, final MediaState mediaState) {
        if (mediaState.getPrivacyStatus() != MobilePrivacyStatus.OPT_IN) {
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "isReadyToSendHit - Exiting as privacy status is not optin.");
            return false;
        }

        if (!hasNetworkConnection(deviceInforming)) {
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "isReadyToSendHit - Exiting as we have no network connection..");
            return false;
        }

        ReturnTuple ret = hasTrackingParams(mediaState);

        if (!ret.isSuccess()) {
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "isReadyToSendHit - Exiting as we have not yet received required tracking"
                            + " configuration - missing config for \"%s\" .",
                    ret.getError());
            return false;
        }

        return true;
    }

    static Map<String, Object> mediaHitToEventData(
            final MediaState mediaState, final MediaHit hit) {
        Map<String, Object> eventData = new HashMap<>();

        String eventType = hit.getEventType();
        eventData.put(MediaCollectionConstants.Report.EVENT_TYPE.key, eventType);

        Map<String, String> customMetadata = hit.getCustomMetadata();

        if (customMetadata.size() > 0) {
            eventData.put(MediaCollectionConstants.Report.CUSTOM_METADATA.key, customMetadata);
        }

        Map<String, Object> qoeData = hit.getQoEData();

        if (qoeData.size() > 0) {
            eventData.put(MediaCollectionConstants.Report.QoE.key, qoeData);
        }

        Map<String, Object> playerTime = new HashMap<>();
        playerTime.put(MediaCollectionConstants.PlayerTime.TS.key, hit.getTimeStamp());
        playerTime.put(MediaCollectionConstants.PlayerTime.PLAYHEAD.key, hit.getPlayhead());
        eventData.put(MediaCollectionConstants.Report.PLAYER_TIME.key, playerTime);

        Map<String, Object> params = hit.getParams();

        if (eventType.equals(MediaCollectionConstants.EventType.SESSION_START)) {
            params.put(
                    MediaCollectionConstants.Session.ANALYTICS_TRACKING_SERVER.key,
                    mediaState.getAnalyticsTrackingServer());

            params.put(MediaCollectionConstants.Session.ANALYTICS_SSL.key, mediaState.isSsl());

            if (mediaState.getAnalyticsRsid() != null) {
                params.put(
                        MediaCollectionConstants.Session.ANALYTICS_RSID.key,
                        mediaState.getAnalyticsRsid());
            }

            if (mediaState.getVid() != null) {
                params.put(
                        MediaCollectionConstants.Session.ANALYTICS_VISITOR_ID.key,
                        mediaState.getVid());
            }

            if (mediaState.getAid() != null) {
                params.put(MediaCollectionConstants.Session.ANALYTICS_AID.key, mediaState.getAid());
            }

            if (mediaState.getMcOrgId() != null) {
                params.put(
                        MediaCollectionConstants.Session.VISITOR_MCORG_ID.key,
                        mediaState.getMcOrgId());
            }

            if (mediaState.getMcid() != null) {
                params.put(
                        MediaCollectionConstants.Session.VISITOR_MCUSER_ID.key,
                        mediaState.getMcid());
            }

            Integer locHintValue = mediaState.getLocHint();

            if (locHintValue != null) {
                params.put(MediaCollectionConstants.Session.VISITOR_AAM_LOC_HINT.key, locHintValue);
            }

            List<VisitorID> customerIDs = mediaState.getVisitorCustomerIDs();
            if (customerIDs != null && customerIDs.size() > 0) {
                params.put(
                        MediaCollectionConstants.Session.VISITOR_CUSTOMER_IDS.key,
                        serializeCustomerIDs(customerIDs));
            }

            if (!params.containsKey(MediaCollectionConstants.Session.MEDIA_CHANNEL.key)) {
                params.put(
                        MediaCollectionConstants.Session.MEDIA_CHANNEL.key,
                        mediaState.getMediaChannel());
            }

            params.put(
                    MediaCollectionConstants.Session.MEDIA_PLAYER_NAME.key,
                    mediaState.getMediaPlayerName());

            String appVersion = mediaState.getMediaAppVersion();

            if (appVersion != null && appVersion.length() > 0) {
                params.put(MediaCollectionConstants.Session.SDK_VERSION.key, appVersion);
            }

            params.put(
                    MediaCollectionConstants.Session.MEDIA_VERSION.key, Media.extensionVersion());

            // Remove sessionID from params
            params.remove(MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID);

        } else if (eventType.equals(MediaCollectionConstants.EventType.AD_START)) {
            params.put(
                    MediaCollectionConstants.Ad.PLAYER_NAME.key, mediaState.getMediaPlayerName());
        }

        if (params.size() > 0) {
            eventData.put(MediaCollectionConstants.Report.PARAMS.key, params);
        }

        return eventData;
    }

    static String generateHitReport(final MediaState mediaState, final MediaHit hit) {

        if (hit == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "generateHitReport - hit null or empty");
            return "";
        }

        if (mediaState == null) {
            Log.debug(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "generateHitReport - MediaState not available");
            return "";
        }

        Map<String, Object> eventData = mediaHitToEventData(mediaState, hit);
        return new JSONObject(eventData).toString();
    }

    static String generateDownloadReport(
            final MediaState mediaState, final List<MediaHit> mediaHits) {

        if (mediaHits == null || mediaHits.isEmpty()) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "generateDownloadReport - hits list null or empty");
            return "";
        }

        if (mediaState == null) {
            Log.error(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "generateDownloadReport - MediaState not available");
            return "";
        }

        JSONArray report = new JSONArray();
        boolean sessionStart = false;
        boolean sessionEnd = false;
        double lastPlayhead = 0;
        long lastRefTS = 0;

        for (MediaHit hit : mediaHits) {
            if (hit == null) {
                continue;
            }

            // Detect session start
            if (!sessionStart) {
                sessionStart =
                        MediaCollectionConstants.EventType.SESSION_START.equals(hit.getEventType());
            }

            // Drop all out of order hits before session start
            if (!sessionStart) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "generateDownloadReport - Dropping event %s as we have not yet gotten"
                                + " session_start.",
                        hit.getEventType());
                continue;
            }

            // Detect session end
            if (!sessionEnd) {
                sessionEnd =
                        (MediaCollectionConstants.EventType.SESSION_COMPLETE.equals(
                                        hit.getEventType()))
                                || (MediaCollectionConstants.EventType.SESSION_END.equals(
                                        hit.getEventType()));
            }

            Map<String, Object> eventData = mediaHitToEventData(mediaState, hit);
            report.put(new JSONObject(eventData));

            lastPlayhead = hit.getPlayhead();
            lastRefTS = hit.getTimeStamp();

            // Stop processing after we have already reached sessionEnd.
            if (sessionEnd) {
                Log.trace(
                        MediaInternalConstants.EXTENSION_LOG_TAG,
                        LOG_TAG,
                        "generateDownloadReport - Dropping all remaining events as we have"
                                + " completed the session.");
                break;
            }
        }

        if (!sessionStart) {
            return "";
        }

        // If we don't have a session_end or session_complete, add one.
        if (sessionStart && !sessionEnd) {
            MediaHit hit =
                    new MediaHit(
                            MediaCollectionConstants.EventType.SESSION_END,
                            new HashMap<>(),
                            new HashMap<>(),
                            new HashMap<>(),
                            lastPlayhead,
                            lastRefTS);
            Map<String, Object> eventData = mediaHitToEventData(mediaState, hit);
            report.put(new JSONObject(eventData));
        }

        return report.toString();
    }

    static boolean hasNetworkConnection(DeviceInforming deviceInforming) {
        DeviceInforming.ConnectionStatus connStatus = deviceInforming.getNetworkConnectionStatus();
        // Todo -: Should we try sending a ping and fail if it returns ConnectionStatus.UNKNOWN
        return connStatus == DeviceInforming.ConnectionStatus.CONNECTED;
    }

    static ReturnTuple hasTrackingParams(final MediaState mediaState) {

        String mediaCollectionServer = mediaState.getMediaCollectionServer();

        if (mediaCollectionServer == null || mediaCollectionServer.length() == 0) {
            return new ReturnTuple(
                    false, MediaInternalConstants.Configuration.MEDIA_COLLECTION_SERVER);
        }

        String analyticsServer = mediaState.getAnalyticsTrackingServer();

        if (analyticsServer == null || analyticsServer.length() == 0) {
            return new ReturnTuple(
                    false, MediaInternalConstants.Configuration.ANALYTICS_TRACKING_SERVER);
        }

        String analyticsRsid = mediaState.getAnalyticsRsid();

        if (analyticsRsid == null || analyticsRsid.length() == 0) {
            return new ReturnTuple(false, MediaInternalConstants.Configuration.ANALYTICS_RSID);
        }

        String mcOrgId = mediaState.getMcOrgId();

        if (mcOrgId == null || mcOrgId.length() == 0) {
            return new ReturnTuple(
                    false, MediaInternalConstants.Configuration.EXPERIENCE_CLOUD_ORGID);
        }

        String mcid = mediaState.getMcid();

        if (mcid == null || mcid.length() == 0) {
            return new ReturnTuple(false, MediaInternalConstants.Identity.MARKETING_VISITOR_ID);
        }

        return new ReturnTuple(true, null);
    }

    // Extract the session ID and API version from backend response
    static String extractSessionID(final String sessionResponseFragment) {
        String sessionID = null;
        Pattern pattern = Pattern.compile("^/api/(.*)/sessions/(.*)");
        Matcher matcher = pattern.matcher(sessionResponseFragment);

        if (matcher.find()) {
            sessionID = matcher.group(2);
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "extractSessionID - Extracted session ID :%s successfully.",
                    sessionID);
        } else {
            Log.trace(
                    MediaInternalConstants.EXTENSION_LOG_TAG,
                    LOG_TAG,
                    "extractSessionID - Failed to extract session ID from response: %s",
                    sessionResponseFragment);
        }

        return sessionID;
    }

    // Get URL to send the SessionStart ping
    static String getTrackingURL(final String server) {
        URLBuilder u = new URLBuilder();
        u.enableSSL(true).setServer(server).addPath("api").addPath("v1").addPath("sessions");
        return u.build();
    }

    // Get URL for sending media events which includes session ID
    static String getTrackingUrlForEvents(final String server, final String sessionID) {
        URLBuilder u = new URLBuilder();
        u.enableSSL(true)
                .setServer(server)
                .addPath("api")
                .addPath("v1")
                .addPath("sessions")
                .addPath(sessionID)
                .addPath("events");
        return u.build();
    }

    static Map<String, Object> serializeCustomerIDs(final List<VisitorID> customerIDs) {
        Map<String, Object> ret = new HashMap<>();

        for (VisitorID customerID : customerIDs) {
            Map<String, Object> customerIDMap = new HashMap<>();
            customerIDMap.put(
                    MediaCollectionConstants.Session.VISITOR_CUSTOMER_KEY_ID.key,
                    customerID.getId());
            customerIDMap.put(
                    MediaCollectionConstants.Session.VISITOR_CUSTOMER_KEY_AUTHSTATE.key,
                    customerID.getAuthenticationState().getValue());
            ret.put(customerID.getIdType(), customerIDMap);
        }

        return ret;
    }

    // Extract the client session ID
    static String extractClientSessionId(final MediaHit hit) {
        String ret = null;

        if (MediaCollectionConstants.EventType.SESSION_START.equals(hit.getEventType())) {
            ret =
                    DataReader.optString(
                            hit.getParams(),
                            MediaInternalConstants.EventDataKeys.Tracker.SESSION_ID,
                            "");
        }

        return ret;
    }
}

class ReturnTuple {
    private final boolean success;
    private final String error;

    ReturnTuple(final boolean success, final String error) {
        this.success = success;
        this.error = error;
    }

    boolean isSuccess() {
        return success;
    }

    String getError() {
        return error;
    }
}
