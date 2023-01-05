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

import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.adobe.marketing.mobile.VisitorID;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class MediaState {
    private static final String LOG_TAG = "MediaState";
    private MobilePrivacyStatus privacyStatus;
    private final boolean ssl;

    private String mcOrgId;

    // Media Config
    private String mediaTrackingServer;
    private String mediaCollectionServer;
    private String mediaChannel;
    private String mediaOvp;
    private String mediaPlayerName;
    private String mediaAppVersion;
    private boolean mediaDebugLogging;

    // Analytics Config
    private String analyticsRsid;
    private String analyticsTrackingServer;

    // Analytics State
    private String aid;
    private String vid;

    // Identity State
    private String mcid;
    private Integer locHint;
    private String blob;

    private List<VisitorID> visitorCustomerIDs;
    private final Object mutex;

    // Assurance State
    private String assuranceIntegrationId;

    MediaState() {
        privacyStatus = MobilePrivacyStatus.UNKNOWN;
        ssl = true;
        mediaDebugLogging = false;
        mediaChannel = "unknown";
        mediaPlayerName = "unknown";
        visitorCustomerIDs = new ArrayList<VisitorID>();
        mutex = new Object();
    }

    public void notifyMobileStateChanges(
            final String moduleName, final Map<String, Object> states) {
        synchronized (mutex) {
            if (states == null) {
                return;
            }

            if (moduleName.equals(MediaInternalConstants.Configuration.SHARED_STATE_NAME)) {
                String privacyString =
                        DataReader.optString(
                                states, MediaInternalConstants.Configuration.GLOBAL_PRIVACY, null);
                if (isValidString(privacyString)) {
                    privacyStatus = MobilePrivacyStatus.fromString(privacyString);
                }

                String orgId =
                        DataReader.optString(
                                states,
                                MediaInternalConstants.Configuration.EXPERIENCE_CLOUD_ORGID,
                                null);
                if (isValidString(orgId)) {
                    mcOrgId = orgId;
                }

                String rsid =
                        DataReader.optString(
                                states, MediaInternalConstants.Configuration.ANALYTICS_RSID, null);
                if (isValidString(rsid)) {
                    analyticsRsid = rsid;
                }

                String analyticsServer =
                        DataReader.optString(
                                states,
                                MediaInternalConstants.Configuration.ANALYTICS_TRACKING_SERVER,
                                null);
                if (isValidString(analyticsServer)) {
                    analyticsTrackingServer = analyticsServer;
                }

                String mediaServer =
                        DataReader.optString(
                                states,
                                MediaInternalConstants.Configuration.MEDIA_TRACKING_SERVER,
                                null);
                if (isValidString(mediaServer)) {
                    mediaTrackingServer = mediaServer;
                }

                String mediaCollectionServerValue =
                        DataReader.optString(
                                states,
                                MediaInternalConstants.Configuration.MEDIA_COLLECTION_SERVER,
                                null);
                if (isValidString(mediaCollectionServerValue)) {
                    mediaCollectionServer = mediaCollectionServerValue;
                }

                if (!isValidString(mediaCollectionServer)) {
                    Log.warning(
                            MediaInternalConstants.EXTENSION_LOG_TAG,
                            LOG_TAG,
                            "Configuration for media extension received without Collection API"
                                + " server. Configure the media extension in your launch property"
                                + " to provide a Collection API server. Refer to documentation for"
                                + " more information.");
                }

                String channel =
                        DataReader.optString(
                                states, MediaInternalConstants.Configuration.MEDIA_CHANNEL, null);
                if (isValidString(channel)) {
                    mediaChannel = channel;
                }

                String ovp =
                        DataReader.optString(
                                states, MediaInternalConstants.Configuration.MEDIA_OVP, null);
                if (isValidString(ovp)) {
                    mediaOvp = ovp;
                }

                String playerName =
                        DataReader.optString(
                                states,
                                MediaInternalConstants.Configuration.MEDIA_PLAYER_NAME,
                                null);
                if (isValidString(playerName)) {
                    mediaPlayerName = playerName;
                }

                String appVersion =
                        DataReader.optString(
                                states,
                                MediaInternalConstants.Configuration.MEDIA_APP_VERSION,
                                null);
                if (isValidString(appVersion)) {
                    mediaAppVersion = appVersion;
                }

                mediaDebugLogging =
                        DataReader.optBoolean(
                                states,
                                MediaInternalConstants.Configuration.MEDIA_DEBUG_LOGGING,
                                false);
            } else if (moduleName.equals(MediaInternalConstants.Identity.SHARED_STATE_NAME)) {

                String mid =
                        DataReader.optString(
                                states, MediaInternalConstants.Identity.MARKETING_VISITOR_ID, null);
                if (isValidString(mid)) {
                    mcid = mid;
                }

                String locHintValue =
                        DataReader.optString(
                                states, MediaInternalConstants.Identity.LOC_HINT, null);
                if (isValidString(locHintValue)) {
                    try {
                        locHint = Integer.parseInt(locHintValue);
                    } catch (NumberFormatException ne) {
                        Log.trace(
                                MediaInternalConstants.EXTENSION_LOG_TAG,
                                LOG_TAG,
                                "notifyMobileStateChanges - Invalid value:(%s) passed, not"
                                        + " updating locHint",
                                locHintValue);
                    }
                }

                String blobValue =
                        DataReader.optString(states, MediaInternalConstants.Identity.BLOB, null);
                if (isValidString(blobValue)) {
                    blob = blobValue;
                }

                List<Map<String, Object>> visitorIdsList =
                        DataReader.optTypedListOfMap(
                                Object.class,
                                states,
                                MediaInternalConstants.Identity.VISITOR_IDS_LIST,
                                null);
                if (visitorIdsList != null) {
                    visitorCustomerIDs = convertToVisitorIds(visitorIdsList);
                }

            } else if (moduleName.equals(MediaInternalConstants.Analytics.SHARED_STATE_NAME)) {

                String aidValue =
                        DataReader.optString(
                                states,
                                MediaInternalConstants.Analytics.ANALYTICS_VISITOR_ID,
                                null);
                if (isValidString(aidValue)) {
                    aid = aidValue;
                }

                String vidValue =
                        DataReader.optString(
                                states, MediaInternalConstants.Analytics.VISITOR_ID, null);
                if (isValidString(vidValue)) {
                    vid = vidValue;
                }

            } else if (moduleName.equals(MediaInternalConstants.Assurance.SHARED_STATE_NAME)) {
                String assuranceIntegrationIdValue =
                        DataReader.optString(
                                states, MediaInternalConstants.Assurance.INTEGRATION_ID, null);
                if (isValidString(assuranceIntegrationIdValue)) {
                    assuranceIntegrationId = assuranceIntegrationIdValue;
                }
            }
        }
    }

    private boolean isValidString(final String str) {
        return str != null && !str.trim().isEmpty();
    }

    public MobilePrivacyStatus getPrivacyStatus() {
        synchronized (mutex) {
            return privacyStatus;
        }
    }

    public boolean isSsl() {
        synchronized (mutex) {
            return ssl;
        }
    }

    public String getMcOrgId() {
        synchronized (mutex) {
            return mcOrgId;
        }
    }

    public String getMediaTrackingServer() {
        synchronized (mutex) {
            return mediaTrackingServer;
        }
    }

    public String getMediaCollectionServer() {
        synchronized (mutex) {
            return mediaCollectionServer;
        }
    }

    public String getMediaChannel() {
        synchronized (mutex) {
            return mediaChannel;
        }
    }

    public String getMediaOVP() {
        synchronized (mutex) {
            return mediaOvp;
        }
    }

    public String getMediaPlayerName() {
        synchronized (mutex) {
            return mediaPlayerName;
        }
    }

    public String getMediaAppVersion() {
        synchronized (mutex) {
            return mediaAppVersion;
        }
    }

    public boolean isMediaDebugLoggingEnabled() {
        synchronized (mutex) {
            return mediaDebugLogging;
        }
    }

    public String getAnalyticsRsid() {
        synchronized (mutex) {
            return analyticsRsid;
        }
    }

    public String getAnalyticsTrackingServer() {
        synchronized (mutex) {
            return analyticsTrackingServer;
        }
    }

    public String getAid() {
        synchronized (mutex) {
            return aid;
        }
    }

    public String getVid() {
        synchronized (mutex) {
            return vid;
        }
    }

    public String getMcid() {
        synchronized (mutex) {
            return mcid;
        }
    }

    public Integer getLocHint() {
        synchronized (mutex) {
            return locHint;
        }
    }

    public String getBlob() {
        synchronized (mutex) {
            return blob;
        }
    }

    public List<VisitorID> getVisitorCustomerIDs() {
        synchronized (mutex) {
            return visitorCustomerIDs;
        }
    }

    public String getAssuranceIntegrationId() {
        synchronized (mutex) {
            return assuranceIntegrationId;
        }
    }

    public static List<VisitorID> convertToVisitorIds(List<Map<String, Object>> data) {
        List<VisitorID> visitorIDList = new ArrayList<>();
        for (Map item : data) {
            if (item != null) {
                String id =
                        String.valueOf(
                                item.get(MediaInternalConstants.Identity.VISITOR_ID_KEYS_ID));
                String origin =
                        String.valueOf(
                                item.get(
                                        MediaInternalConstants.Identity.VISITOR_ID_KEYS_ID_ORIGIN));
                String type =
                        String.valueOf(
                                item.get(MediaInternalConstants.Identity.VISITOR_ID_KEYS_ID_TYPE));
                int state =
                        Integer.parseInt(
                                String.valueOf(
                                        item.get(
                                                MediaInternalConstants.Identity
                                                        .VISITOR_ID_KEYS_STATE)));
                visitorIDList.add(
                        new VisitorID(
                                origin,
                                type,
                                id,
                                VisitorID.AuthenticationState.fromInteger(state)));
            }
        }
        return visitorIDList;
    }
}
